package com.malliina.jenkinsctrl.http

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.concurrent.Observables
import com.malliina.http.{AsyncHttp, WebResponse}
import com.malliina.jenkinsctrl.JenkinsCredentials
import com.malliina.jenkinsctrl.http.JenkinsClient._
import com.malliina.jenkinsctrl.json.JsonException
import com.malliina.jenkinsctrl.models._
import com.malliina.util.Log
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import play.api.libs.json.Reads
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

object JenkinsClient {
  val DefaultPollInterval = 1.second
  val Accept = "Accept"
  val ContentType = "Content-Type"
  val Location = "Location"
  val XMoreData = "X-More-Data"
  val XTextSize = "X-Text-Size"
  val Xml = "application/xml"

  // Jenkins keywords
  val Api = "api"
  val BuildKey = "build"
  val BuildWithParameters = "buildWithParameters"
  val CreateItem = "createItem"
  val DoDelete = "doDelete"
  val Job = "job"
  val Json = "json"
  val LogText = "logText"
  val Name = "name"
  val Pretty = "pretty"
  val ProgressiveText = "progressiveText"
  val Start = "start"
  val TokenKey = "token"
  val True = "true"
}

/** A Jenkins HTTP client. Builds jobs and provides build progress updates in `Observable`s.
  *
  * This is a streaming API, however, Jenkins does not have a streaming API. So, we manufacture streams by polling the
  * Jenkins HTTP API every `pollInterval` when needed.
  *
  * @param creds        Jenkins credentials
  * @param pollInterval the poll interval used for progress updates
  */
class JenkinsClient(creds: JenkinsCredentials, pollInterval: FiniteDuration = DefaultPollInterval)
  extends AutoCloseable
    with Log {

  val client = new AsyncHttp
  val host: Url = creds.host

  val overviewUrl = jsonUrl(host)
  val createJobUrl = host / CreateItem

  def deleteJobUrl(name: JobName) =
    jobUrl(name) / DoDelete

  def buildJobUrl(name: JobName) =
    jobUrl(name) / s"$BuildKey?$TokenKey=${creds.token}"

  def buildWithParametersUrl(name: JobName) =
    jobUrl(name) / BuildWithParameters

  def apiJobUrl(name: JobName) =
    jsonUrl(jobUrl(name))

  def consoleOutputUrl(name: JobName, number: BuildNumber, start: ByteOffset) =
    buildDetailsUrl(name, number) / LogText / s"$ProgressiveText?$Start=${start.start}"

  def buildDetailsUrl(name: JobName, number: BuildNumber) =
    jobUrl(name) / s"${number.id}"

  def jobUrl(name: JobName): Url =
    host / Job / name.name

  def jsonUrl(base: Url): Url =
    base / Api / s"$Json?$Pretty=$True"

  def overview(): Future[Overview] = runGetAsJson[Overview](overviewUrl)

  def job(name: JobName): Future[VerboseJob] = runGetAsJson[VerboseJob](apiJobUrl(name))

  def createJob(name: JobName, xml: String): Future[WebResponse] = {
    AsyncHttp.withClient { client =>
      val req = client.postRequest(createJobUrl.url, Map(ContentType -> Xml), Map(Name -> name.name))
        .setEntity(new StringEntity(xml, Xml))
        .build()
      client execute req
    }
  }

  def deleteJob(name: JobName) = {
    makeRequest { client =>
      client.postRequest(deleteJobUrl(name).url)
    }
  }

  /** @param job the job name
    * @return the URL to the queued `job`
    */
  def build(job: JobName): Future[Url] = {
    val url = buildJobUrl(job)
    makePost(url) flatMap (response => parseUrl(response, url))
  }

  def buildWithParameters(order: BuildOrder): Future[Url] = {
    val url = buildWithParametersUrl(order.job)
    log debug s"POST $url"
    val request = makeRequest(_.postRequest(url.url, params = order.parameters))
    request flatMap (response => parseUrl(response, url))
  }

  def consoleOutput(name: JobName, number: BuildNumber, offset: ByteOffset): Future[ConsoleProgress] =
    runParsed(consoleOutputUrl(name, number, offset)) { response =>
      val size = Option(response.inner.getFirstHeader(XTextSize))
        .flatMap(s => Try(s.getValue.toLong).toOption)
        .map(ByteOffset.apply)
        .getOrElse(ByteOffset.Zero)
      val isOngoing = Option(response.inner.getFirstHeader(XMoreData)).exists(_.getValue == True)
      Try(response.asString).map(body => ConsoleProgress(body, size, !isOngoing))
    }

  def buildDetails(job: JobName, number: BuildNumber): Future[BuildDetails] =
    runGetAsJson[BuildDetails](jsonUrl(buildDetailsUrl(job, number)))

  /** Builds `job` and returns a stream of console output.
    *
    * @param order build order
    * @return a stream of console output
    */
  def buildWithConsole(order: BuildOrder): Observable[ConsoleProgress] = {
    buildWithProgressTask(order).consoleUpdates
  }

  def buildWithProgressTask(order: BuildOrder): BuildTask = {
    val result = Promise[BuildResult]()
    val queueTask = enqueueUntilBuildingTask(order)
    val consoleUpdates = ReplaySubject[ConsoleProgress]()
    val buildUpdates = ReplaySubject[BuildDetails]()
    buildUpdates.lastOption.subscribe(
      last => {
        val lastResultOrFailure: BuildResult = last.flatMap(_.result) getOrElse BuildFailure
        result.trySuccess(lastResultOrFailure)
      },
      err => {
        result.tryFailure(err)
      },
      () => {
        result.trySuccess(BuildFailure)
      })
    // 1) Waits for the queueing to complete, then,
    // 2) If a build was started, subscribes to build and console output events
    queueTask.updates.lastOption.subscribe(
      queueingUpdate => {
        queueingUpdate.flatMap(_.executable) map { build =>
          // done queueing, got a build
          val job = order.job
          val number = build.number
          consoleStream(job, number).subscribe(consoleUpdates)
          follow(job, number).subscribe(buildUpdates)
        } getOrElse {
          // done queueing, but no build was started (can this happen?)
          consoleUpdates.onCompleted()
          buildUpdates.onCompleted()
        }
      },
      queueingError => {
        consoleUpdates.onError(queueingError)
        buildUpdates.onError(queueingError)
      },
      () => {
        // queueing has completed
        ()
      }
    )
    BuildTask(order, queueTask, consoleUpdates, buildUpdates, result.future)
  }

  def enqueueUntilBuildingTask(order: BuildOrder): QueueTask = {
    val params = order.parameters
    val urlJob =
      if (params.isEmpty) build(order.job)
      else buildWithParameters(order)
    val subject = ReplaySubject[QueueProgress]()
    urlJob map { url =>
      val queueUrl = jsonUrl(url)

      def queueInfo = runGetAsJson[QueueProgress](queueUrl)

      // do I after completion need to unsubscribe the returned subscription?
      Observables.pollUntilComplete(pollInterval)(queueInfo).subscribe(subject)
    } onFailure {
      case t => subject.onError(t)
    }
    QueueTask(urlJob, subject)
  }

  protected def follow(job: JobName, build: BuildNumber): Observable[BuildDetails] =
    Observables.pollUntilComplete(pollInterval)(buildDetails(job, build))

  protected def consoleStream(name: JobName, number: BuildNumber): Observable[ConsoleProgress] =
    consoleStream(name, number, ByteOffset.Zero, pollInterval).distinctUntilChanged(_.offset)

  protected def consoleStream(name: JobName,
                              number: BuildNumber,
                              offset: ByteOffset,
                              pollInterval: FiniteDuration): Observable[ConsoleProgress] =
    Observable.from(consoleOutput(name, number, offset)) concatMap { consoleOut =>
      val next =
        if (consoleOut.isCompleted) Observable.empty
        else Observable.timer(pollInterval).concatMap(_ => consoleStream(name, number, consoleOut.offset, pollInterval))
      Observable.just(consoleOut) ++ next
    }

  protected def enqueue(job: JobName): Future[QueueProgress] =
    build(job).flatMap(url => runGetAsJson[QueueProgress](jsonUrl(url)))

  private def parseUrl(response: WebResponse, url: Url) =
    Future.fromTry(parseUrlFromResponse(response, url))

  private def parseUrlFromResponse(response: WebResponse, url: Url): Try[Url] = {
    def fail = Failure[Url](new ResponseException(response, url))

    if (response.code == StatusCodes.Accepted) {
      Option(response.inner.getFirstHeader(Location))
        .map(location => Success(locationUrl(location.getValue)))
        .getOrElse(fail)
    } else {
      fail
    }
  }

  /** Returns an `Url` of `location`, which has the same protocol as this client's `host`.
    *
    * Hack around situations where we use https, but the Jenkins API returns http resources.
    *
    * @param location location url
    * @return a possibly https url of `location`
    */
  def locationUrl(location: String): Url = {
    val url = Url.build(location)
    if (host.isHttps) url.toHttps else url
  }

  protected def runGetAsJson[T](url: Url)(implicit r: Reads[T]): Future[T] =
    runParsed[T](url)(parse(url, _))

  protected def runParsed[T](url: Url)(parse: WebResponse => Try[T]): Future[T] =
    makeGet(url) flatMap { response =>
      if (response.isSuccess) Future.fromTry(parse(response))
      else Future.failed[T](new ResponseException(response, url))
    }

  def makeGet(url: Url): Future[WebResponse] = {
    log debug s"GET $url"
    makeRequest(_ => RequestBuilder.get(url.url))
  }

  def makePost(url: Url): Future[WebResponse] = {
    log debug s"POST $url"
    makeRequest(_.postRequest(url.url))
  }

  def makeRequest(f: AsyncHttp => RequestBuilder): Future[WebResponse] = {
    import AsyncHttp.RichRequest
    val req = f(client)
      .setHeader(Accept, AsyncHttp.MimeTypeJson)
      .build()
    req.setBasicAuth(creds.user, creds.token.token)
    client execute req
  }

  def parse[T: Reads](url: Url, response: WebResponse): Try[T] = {
    response.json
      .flatMap(json => Try(json.as[T]))
      .recoverWith {
        case e: Exception => Failure(new JsonException(url, response, e))
      }
  }

  def close(): Unit = {
    client.close()
  }
}

object StatusCodes {
  val Ok = 200
  val Accepted = 201
}

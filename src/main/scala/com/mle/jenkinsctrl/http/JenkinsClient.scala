package com.mle.jenkinsctrl.http

import com.mle.concurrent.{Observables, Completable}
import com.mle.concurrent.ExecutionContexts.cached
import com.mle.http.AsyncHttp
import com.mle.http.AsyncHttp.RichRequestBuilder
import com.mle.jenkinsctrl.JenkinsCredentials
import com.mle.jenkinsctrl.http.JenkinsClient._
import com.mle.jenkinsctrl.json.JsonException
import com.mle.jenkinsctrl.models._
import com.mle.util.Log
import play.api.libs.json.Reads
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Try}

object JenkinsClient {
  val DefaultPollInterval = 1.second
  val Accept = "Accept"
  val Location = "Location"
  val XMoreData = "X-More-Data"
  val XTextSize = "X-Text-Size"
  val ContentType = "Content-Type"
  val Xml = "application/xml"

  // Jenkins keywords
  val Api = "api"
  val BuildKey = "build"
  val CreateItem = "createItem"
  val DoDelete = "doDelete"
  val Json = "json"
  val Job = "job"
  val Pretty = "pretty"
  val TokenKey = "token"
  val Name = "name"
  val LogText = "logText"
  val ProgressiveText = "progressiveText"
  val Start = "start"
  val True = "true"
}

/**
  * @author mle
  */
class JenkinsClient(creds: JenkinsCredentials) extends AutoCloseable with Log {
  val client = new AsyncHttp
  val host: Url = creds.host

  val overviewUrl = jsonUrl(host)
  val createJobUrl = host / CreateItem

  def deleteJobUrl(name: JobName) =
    jobUrl(name) / DoDelete

  def buildJobUrl(name: JobName) =
    jobUrl(name) / s"$BuildKey?$TokenKey=${creds.token}"

  def apiJobUrl(name: JobName) =
    jsonUrl(jobUrl(name))

  def jobUrl(name: JobName): Url =
    host / Job / name.name

  def buildDetailsUrl(name: JobName, number: BuildNumber) =
    jobUrl(name) / s"${number.id}"

  def consoleOutputUrl(name: JobName, number: BuildNumber, start: ByteOffset) =
    buildDetailsUrl(name, number) / LogText / s"$ProgressiveText?$Start=${start.start}"

  def jsonUrl(base: Url): Url =
    base / Api / s"$Json?$Pretty=$True"

  def overview(): Future[Overview] = runGetAsJson[Overview](overviewUrl)

  def job(name: JobName): Future[VerboseJob] = runGetAsJson[VerboseJob](apiJobUrl(name))

  def createJob(name: JobName, xml: String): Future[RichResponse] =
    makeRequest(_.client
      .preparePost(createJobUrl.url)
      .setBody(xml)
      .addQueryParam(Name, name.name)
      .setHeader(ContentType, Xml))

  def deleteJob(name: JobName) = makeRequest(_.client.preparePost(deleteJobUrl(name).url))

  def consoleOutput(name: JobName, number: BuildNumber, offset: ByteOffset): Future[ConsoleProgress] =
    runParsed(consoleOutputUrl(name, number, offset)) { response =>
      val size = response.firstHeaderValue(XTextSize)
        .flatMap(s => Try(s.toLong).toOption)
        .map(ByteOffset.apply)
        .getOrElse(ByteOffset.Zero)
      val isOngoing = response.firstHeaderValue(XMoreData).contains(True)
      response.body.map(body => ConsoleProgress(body, size, !isOngoing))
    }

  /** Builds `job` and returns a stream of console output.
    *
    * @param job job to build
    * @param pollInterval implementation detail
    * @return a stream of console output
    */
  def buildWithConsole(job: JobName, pollInterval: FiniteDuration = DefaultPollInterval): Observable[ConsoleProgress] =
    buildWithProgress(job, pollInterval) concatMap {
      case ConsoleUpdate(console) => Observable.just(console)
      case _ => Observable.empty
    }

  /** Builds `job` and returns queue, build and console output progress.
    *
    *
    * @param job
    * @param pollInterval
    * @return
    */
  def buildWithProgress(job: JobName, pollInterval: FiniteDuration = DefaultPollInterval): Observable[BuildProgress] =
    enqueueUntilBuilding(job, pollInterval) concatMap { queueItem =>
      queueItem.executable
        .map { build =>
          val consoleUpdates = consoleStream(job, build.number, pollInterval).map(ConsoleUpdate.apply)
          val buildUpdates = follow(job, build.number, pollInterval).map(BuildUpdate.apply)
          consoleUpdates merge buildUpdates
        }
        .getOrElse(Observable.just(QueueUpdate(queueItem)))
    }

  def enqueueUntilBuilding(job: JobName, pollInterval: FiniteDuration = DefaultPollInterval): Observable[QueueProgress] =
    Observable.from(build(job)).concatMap { url =>
      val queueUrl = jsonUrl(url)
      def queueInfo = runGetAsJson[QueueProgress](queueUrl)
      Observables.pollUntilComplete(pollInterval)(queueInfo)
    }

  def follow(job: JobName, build: BuildNumber, pollInterval: FiniteDuration = DefaultPollInterval): Observable[BuildDetails] =
    Observables.pollUntilComplete(pollInterval)(buildDetails(job, build))

  def consoleStream(name: JobName, number: BuildNumber, pollInterval: FiniteDuration = DefaultPollInterval): Observable[ConsoleProgress] =
    consoleStream(name, number, ByteOffset.Zero, pollInterval).distinctUntilChanged(_.offset)

  def consoleStream(name: JobName,
                    number: BuildNumber,
                    offset: ByteOffset,
                    pollInterval: FiniteDuration): Observable[ConsoleProgress] =
    Observable.from(consoleOutput(name, number, offset)) concatMap { consoleOut =>
      val next =
        if (consoleOut.isCompleted) Observable.empty
        else Observable.timer(pollInterval).concatMap(_ => consoleStream(name, number, consoleOut.offset, pollInterval))
      Observable.just(consoleOut) ++ next
    }

  def buildDetails(job: JobName, number: BuildNumber): Future[BuildDetails] =
    runGetAsJson[BuildDetails](jsonUrl(buildDetailsUrl(job, number)))

  def enqueue(job: JobName): Future[QueueProgress] =
    build(job).flatMap(url => runGetAsJson[QueueProgress](jsonUrl(url)))

  /** @param job the job name
    * @return the URL to the queued `job`
    */
  def build(job: JobName): Future[Url] = {
    val url = buildJobUrl(job)
    makePost(url) flatMap { response =>
      def fail = Future.failed[Url](new ResponseException(response, url))
      if (response.status == StatusCodes.Accepted) {
        val maybeLocation = for {
          locationHeaderValue <- response.firstHeaderValue(Location)
        } yield Url.build(locationHeaderValue)
        maybeLocation
          .map(url => Future.successful(url))
          .getOrElse(fail)
      } else {
        fail
      }
    }
  }

  protected def runGetAsJson[T](url: Url)(implicit r: Reads[T]): Future[T] =
    runParsed[T](url)(parse(url, _))

  protected def runParsed[T](url: Url)(parse: RichResponse => Try[T]) =
    makeGet(url) flatMap { response =>
      if (response.isSuccess) Future.fromTry(parse(response))
      else Future.failed[T](new ResponseException(response, url))
    }

  def makeGet(url: Url): Future[RichResponse] = {
    log info s"GET $url"
    makeRequest(_.get(url.url))
  }

  def makePost(url: Url): Future[RichResponse] = {
    log info s"POST $url"
    makeRequest(_.client.preparePost(url.url))
  }

  def makeRequest(f: AsyncHttp => AsyncHttp.RequestBuilder): Future[RichResponse] = {
    val builder = f(client)
      .setBasicAuth(creds.user, creds.pass)
      .setHeader(Accept, AsyncHttp.JSON)
    builder.run().map(r => RichResponse(r))
  }

  def parse[T](url: Url, response: RichResponse)(implicit r: Reads[T]): Try[T] = {
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

package com.mle.jenkinsctrl.http

import com.mle.concurrent.Completable
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
}

/**
  * @author mle
  */
class JenkinsClient(creds: JenkinsCredentials) extends AutoCloseable with Log {
  val client = new AsyncHttp
  val host: Url = creds.host

  val overviewUrl = jsonUrl(host)
  val createJobUrl = host / CreateItem

  def deleteJobUrl(name: JobName) = jobUrl(name) / DoDelete

  def buildJobUrl(name: JobName) = jobUrl(name) / s"$BuildKey?$TokenKey=${creds.token}"

  def apiJobUrl(name: JobName) = jsonUrl(jobUrl(name))

  def jobUrl(name: JobName): Url = host / Job / name.name

  def buildDetailsUrl(name: JobName, number: BuildNumber) = host / Job / name.name / s"${number.id}"

  def jsonUrl(base: Url): Url = base / Api / s"$Json?$Pretty=true"

  def overview(): Future[Overview] = runGet[Overview](overviewUrl)

  def job(name: JobName): Future[VerboseJob] = runGet[VerboseJob](apiJobUrl(name))

  def createJob(name: JobName, xml: String): Future[RichResponse] = {
    makeRequest(_.client
      .preparePost(createJobUrl.url)
      .setBody(xml)
      .addQueryParam(Name, name.name)
      .setHeader(ContentType, Xml))
  }

  def deleteJob(name: JobName) = makeRequest(_.client.preparePost(deleteJobUrl(name).url))

  def buildWithProgress(job: JobName, pollInterval: FiniteDuration = DefaultPollInterval): Observable[BuildProgress] = {
    enqueueUntilBuilding(job, pollInterval).concatMap { queueItem =>
      queueItem.executable
        .map(build => follow(job, build.number, pollInterval).map(BuildUpdate))
        .getOrElse(Observable.just(QueueUpdate(queueItem)))
    }
  }

  def follow(job: JobName, build: BuildNumber, pollInterval: FiniteDuration = DefaultPollInterval): Observable[BuildDetails] = {
    pollUntilComplete(pollInterval)(buildDetails(job, build))
  }

  def enqueueUntilBuilding(job: JobName, pollInterval: FiniteDuration = DefaultPollInterval): Observable[QueueItem] = {
    Observable.from(build(job)).concatMap { url =>
      val queueUrl = jsonUrl(url)
      def queueInfo = runGet[QueueItem](queueUrl)
      pollUntilComplete(pollInterval)(queueInfo)
    }
  }

  def pollUntilComplete[T <: Completable](pollInterval: FiniteDuration)(f: => Future[T]): Observable[T] = {
    Observable.interval(pollInterval)
      .concatMap(_ => Observable.from(f))
      .takeUntil(_.isCompleted)
  }

  def buildDetails(job: JobName, number: BuildNumber): Future[BuildDetails] = {
    runGet[BuildDetails](jsonUrl(buildDetailsUrl(job, number)))
  }

  def enqueue(job: JobName): Future[QueueItem] =
    build(job).flatMap(url => runGet[QueueItem](jsonUrl(url)))

  /** @param job the job name
    * @return the URL to the queued `job`
    */
  def build(job: JobName): Future[Url] = {
    val url = buildJobUrl(job)
    makePost(url) flatMap { response =>
      def fail = Future.failed[Url](new ResponseException(response, url))
      if (response.status == StatusCodes.Accepted) {
        val maybeLocation = for {
          locationHeaderValues <- response.headers.get(Location)
          locationHeaderValue <- locationHeaderValues.headOption
        } yield Url.build(locationHeaderValue)
        maybeLocation
          .map(url => Future.successful(url))
          .getOrElse(fail)
      } else {
        fail
      }
    }
  }

  protected def runGet[T](url: Url)(implicit r: Reads[T]): Future[T] = {
    makeGet(url) flatMap { response =>
      if (response.isSuccess) Future.fromTry(parse(url, response))
      else Future.failed[T](new ResponseException(response, url))
    }
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
  val Accepted = 201
}

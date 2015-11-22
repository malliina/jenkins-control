package com.mle.jenkinsctrl.http

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.http.AsyncHttp
import com.mle.http.AsyncHttp.RichRequestBuilder
import com.mle.jenkinsctrl.JenkinsCredentials
import com.mle.jenkinsctrl.http.JenkinsClient.{Accept, Location}
import com.mle.jenkinsctrl.json.JsonException
import com.mle.jenkinsctrl.models._
import com.mle.util.Log
import play.api.libs.json.Reads
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.{Failure, Try}

/**
  * @author mle
  */
class JenkinsClient(creds: JenkinsCredentials) extends AutoCloseable with Log {
  val client = new AsyncHttp
  val host: Url = creds.host

  val overviewUrl = apiUrl(host)

  def buildJobUrl(name: JobName) = jobUrl(name) / s"build?token=${creds.token}"

  def apiJobUrl(name: JobName) = apiUrl(jobUrl(name))

  def jobUrl(name: JobName): Url = host / "job" / name.name

  def buildDetailsUrl(name: JobName, number: BuildNumber) = host / "job" / name.name / s"${number.id}"

  def apiUrl(base: Url): Url = base / "api" / "json?pretty=true"

  def overview(): Future[Overview] = {
    runGet[Overview](overviewUrl)
  }

  def job(name: JobName): Future[VerboseJob] = {
    runGet[VerboseJob](apiJobUrl(name))
  }

  def buildDetails(job: JobName, number: BuildNumber): Future[BuildDetails] = {
    runGet[BuildDetails](apiUrl(buildDetailsUrl(job, number)))
  }

  def buildWithProgress(job: JobName, pollInterval: Duration = 1.second): Observable[BuildProgress] = {
    val observable = enqueueUntilBuilding(job, pollInterval).concatMap { queueItem =>
      queueItem.executable
        .map(build => follow(job, build.number, pollInterval).map(BuildUpdate))
        .getOrElse(Observable.just(QueueUpdate(queueItem)))
    }
    asHot(observable)
  }

  def follow(job: JobName, build: BuildNumber, pollInterval: Duration = 1.second): Observable[BuildDetails] = {
    val observable = Observable.interval(pollInterval)
      .concatMap(i => Observable.from(buildDetails(job, build)))
      .takeUntil(_.isComplete)
    asHot(observable)
  }

  def enqueueUntilBuilding(job: JobName, pollInterval: Duration = 1.second): Observable[QueueItem] = {
    val observable = Observable.from(build(job)).concatMap { url =>
      val queueUrl = apiUrl(url)
      def queueInfo = runGet[QueueItem](queueUrl)
      Observable.interval(pollInterval)
        .concatMap(_ => Observable.from(queueInfo))
        .takeUntil(queueItem => !queueItem.isInProgress)
    }
    asHot(observable)
  }

  // Does this even work?
  def asHot[T](o: Observable[T]): Observable[T] = {
    val hot = o.publish
    hot.connect
    hot
  }

  def enqueue(job: JobName): Future[QueueItem] = {
    build(job).flatMap(url => runGet[QueueItem](apiUrl(url)))
  }

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
    makeRequest(_.client.preparePost(url.url))
  }

  def makeRequest(f: AsyncHttp => AsyncHttp.RequestBuilder) = {
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

object JenkinsClient {
  val Accept = "Accept"
  val Location = "Location"
}

object StatusCodes {
  val Accepted = 201
}

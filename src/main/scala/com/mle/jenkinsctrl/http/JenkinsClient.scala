package com.mle.jenkinsctrl.http

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.http.AsyncHttp
import com.mle.http.AsyncHttp.RichRequestBuilder
import com.mle.jenkinsctrl.JenkinsCredentials
import com.mle.jenkinsctrl.http.JenkinsClient.Accept
import com.mle.jenkinsctrl.json.JsonException
import com.mle.jenkinsctrl.models.{JobName, Overview, Url, VerboseJob}
import com.mle.util.Log
import com.ning.http.client.Response
import play.api.libs.json.Reads

import scala.concurrent.Future
import scala.util.{Failure, Try}

/**
  * @author mle
  */
class JenkinsClient(creds: JenkinsCredentials) extends AutoCloseable with Log {
  val client = new AsyncHttp

  val overviewUrl = url("")

  protected def jobUrl(name: JobName) = url(s"/job/${name.name}")

  def overview(): Future[Overview] = runGet[Overview](overviewUrl)

  def job(name: JobName): Future[VerboseJob] = runGet[VerboseJob](jobUrl(name))

  protected def runGet[T](url: Url)(implicit r: Reads[T]): Future[T] = {
    val builder = client.get(url.url)
      .setBasicAuth(creds.user, creds.pass)
      .setHeader(Accept, AsyncHttp.JSON)
    builder.run().map(RichResponse.apply) flatMap { response =>
      if (response.isSuccess) Future.fromTry(parse(url, response))
      else Future.failed[T](new ResponseException(response, url))
    }
  }

  def parse[T](url: Url, response: RichResponse)(implicit r: Reads[T]): Try[T] = {
    response.json.flatMap(json => Try(json.as[T])).recoverWith {
      case e: Exception => Failure(new JsonException(url, response, e))
    }
  }

  def isSuccess(response: Response): Boolean = {
    val code = response.getStatusCode
    code >= 200 && code < 300
  }

  private def url(path: String): Url = Url.fromUrl(creds.host, s"$path/api/json?pretty=true")

  def close(): Unit = {
    client.close()
  }
}

object JenkinsClient {
  val Accept = "Accept"
}

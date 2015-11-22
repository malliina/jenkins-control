package com.mle.jenkinsctrl.http

import com.ning.http.client.{FluentCaseInsensitiveStringsMap, Response}
import play.api.libs.json.Json

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.util.Try

/**
  * @author mle
  */
case class RichResponse(response: Response) {
  val status = response.getStatusCode
  val isSuccess = status >= 200 && status <= 300
  lazy val body = Try(response.getResponseBody)
  lazy val json = body.flatMap(b => Try(Json.parse(b)))
  lazy val headers: Map[String, Seq[String]] = RichResponse.ningHeadersToMap(response.getHeaders)
}

object RichResponse {
  // from play-ws. consider using that lib directly instead. not doing it for now, seems too big a dependency
  private def ningHeadersToMap(headers: FluentCaseInsensitiveStringsMap): Map[String, Seq[String]] =
    mapAsScalaMapConverter(headers).asScala.map(e => e._1 -> e._2.toSeq).toMap
}

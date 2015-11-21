package com.mle.jenkinsctrl.http

import com.ning.http.client.Response
import play.api.libs.json.Json

import scala.util.Try

/**
  * @author mle
  */
case class RichResponse(response: Response) {
  val status = response.getStatusCode
  val isSuccess = status >= 200 && status <= 300
  lazy val body = Try(response.getResponseBody)
  lazy val json = body.flatMap(b => Try(Json.parse(b)))
}

package com.mle.jenkinsctrl.json

import com.mle.jenkinsctrl.http.RichResponse
import com.mle.jenkinsctrl.models.Url

import scala.util.Try

/**
  * @author mle
  */
class JsonException(val url: Url, val response: RichResponse, val inner: Exception)
  extends Exception(s"Unable to parse response to url $url as JSON ${response.body.getOrElse("")}", inner) {
  def bodyAsString: Try[String] = response.body
}

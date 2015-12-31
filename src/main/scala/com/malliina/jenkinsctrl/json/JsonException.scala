package com.malliina.jenkinsctrl.json

import com.malliina.jenkinsctrl.http.RichResponse
import com.malliina.jenkinsctrl.models.Url

import scala.util.Try

/**
  * @author mle
  */
class JsonException(val url: Url, val response: RichResponse, val inner: Exception)
  extends Exception(s"Unable to parse response to url $url as JSON ${response.body.getOrElse("")}", inner) {
  def bodyAsString: Try[String] = response.body
}

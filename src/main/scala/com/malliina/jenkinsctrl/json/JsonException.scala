package com.malliina.jenkinsctrl.json

import com.malliina.http.WebResponse
import com.malliina.jenkinsctrl.models.Url

import scala.util.Try

class JsonException(val url: Url, val response: WebResponse, val inner: Exception)
  extends Exception(s"Unable to parse response to URL $url as JSON, failed with $inner", inner) {
  def bodyAsString: Try[String] = Try(response.asString)
}

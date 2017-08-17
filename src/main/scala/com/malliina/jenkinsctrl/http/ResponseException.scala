package com.malliina.jenkinsctrl.http

import com.malliina.http.WebResponse
import com.malliina.jenkinsctrl.models.Url

class ResponseException(val response: WebResponse, val url: Url)
  extends Exception(s"Invalid response code ${response.code} to url $url") {

  def statusCode = response.code

  def bodyAsString = response.asString
}

package com.malliina.jenkinsctrl.http

import com.malliina.jenkinsctrl.models.Url

/**
  * @author mle
  */
class ResponseException(val response: RichResponse, val url: Url)
  extends Exception(s"Invalid response code ${response.status} to url $url") {

  def statusCode = response.status

  def bodyAsString = response.body
}

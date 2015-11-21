package com.mle.jenkinsctrl.json

import com.mle.jenkinsctrl.http.RichResponse
import com.mle.jenkinsctrl.models.Url

/**
  * @author mle
  */
class JsonException(val url: Url, val response: RichResponse, val inner: Exception)
  extends Exception(Option(inner.getMessage).getOrElse("JSON parse error"), inner) {
  def bodyAsString = response.body
}

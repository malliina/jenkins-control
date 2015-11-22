package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class Author(absoluteUrl: Url, fullName: String)

object Author {
  implicit val json = Json.format[Author]
}

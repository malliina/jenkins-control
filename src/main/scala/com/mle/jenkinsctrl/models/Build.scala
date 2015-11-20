package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class Build(number: Int, url: Url)

object Build {
  implicit val json = Json.format[Build]
}

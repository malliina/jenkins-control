package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class BuildParameter(name: String, value: String)

object BuildParameter {
  implicit val json = Json.format[BuildParameter]
}

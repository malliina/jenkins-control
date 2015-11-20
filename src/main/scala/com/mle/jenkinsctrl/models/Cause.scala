package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class Cause(shortDescription: String, userId: String, userName: String)

object Cause {
  implicit val json = Json.format[Cause]
}

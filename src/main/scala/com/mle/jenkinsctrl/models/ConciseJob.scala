package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class ConciseJob(name: JobName, url: Url, color: Color) extends Job

object ConciseJob {
  implicit val json = Json.format[ConciseJob]
}

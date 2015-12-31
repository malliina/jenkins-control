package com.malliina.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class Overview(mode: Mode, jobs: Seq[ConciseJob], numExecutors: Int, quietingDown: Boolean)

object Overview {
  implicit val json = Json.format[Overview]
}

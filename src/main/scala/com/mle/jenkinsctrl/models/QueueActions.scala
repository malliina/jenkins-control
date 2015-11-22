package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class QueueActions(causes: Seq[Cause])

object QueueActions {
  implicit val json = Json.format[QueueActions]
}

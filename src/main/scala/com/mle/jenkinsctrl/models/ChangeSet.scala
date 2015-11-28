package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class ChangeSet(items: Seq[ChangeSetItem], kind: Option[String])

object ChangeSet {
  implicit val json = Json.format[ChangeSet]
}

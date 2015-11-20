package com.mle.jenkinsctrl.models

import play.api.libs.json.{JsValue, Json}

/**
  * @author mle
  */
case class ChangeSet(items: Seq[JsValue], kind: String)

object ChangeSet {
  implicit val json = Json.format[ChangeSet]
}

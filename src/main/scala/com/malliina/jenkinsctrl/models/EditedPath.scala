package com.malliina.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class EditedPath(editType: String, file: String)

object EditedPath {
  implicit val json = Json.format[EditedPath]
}

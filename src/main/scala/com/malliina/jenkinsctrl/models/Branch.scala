package com.malliina.jenkinsctrl.models

import play.api.libs.functional.syntax._
import play.api.libs.json._


/**
  * @author mle
  */
case class Branch(sha1: SHA1, name: String)

object Branch {
  val JsonKey = "branch"
  implicit val json = (
    (JsPath \ SHA1.JsonKey).format[SHA1] and
      (JsPath \ "name").format[String]
    ) (apply, unlift(unapply))
}

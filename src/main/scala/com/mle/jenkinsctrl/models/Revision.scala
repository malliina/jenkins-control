package com.mle.jenkinsctrl.models

import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath

/**
  * @author mle
  */
case class Revision(sha1: SHA1, branch: Seq[Branch])

object Revision {
  implicit val json = (
    (JsPath \ SHA1.JsonKey).format[SHA1] and
      (JsPath \ "branch").format[Seq[Branch]]
    ) (apply, unlift(unapply))
}

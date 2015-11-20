package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class Revision(sha1: SHA1, branch: Seq[Branch])

object Revision {
  implicit val json = Json.format[Revision]
}

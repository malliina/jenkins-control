package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class BuildByBranch(buildNumber: Int, marked: Revision, revision: Revision)

object BuildByBranch {
  implicit val json = Json.format[BuildByBranch]
}

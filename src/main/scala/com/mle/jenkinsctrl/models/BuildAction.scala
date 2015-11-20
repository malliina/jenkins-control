package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class BuildAction(buildByBranchName: Map[String, BuildByBranch],
                       lastBuiltRevision: BuildByBranch,
                       remoteUrls: Seq[Url],
                       scmName: String)

object BuildAction {
  implicit val json = Json.format[BuildAction]
}

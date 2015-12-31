package com.malliina.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class BuildAction(buildsByBranchName: Map[String, BuildByBranch],
                       lastBuiltRevision: Revision,
                       remoteUrls: Seq[Url],
                       scmName: String)

object BuildAction {
  implicit val json = Json.format[BuildAction]
}

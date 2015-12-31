package com.malliina.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class ChangeSetItem(affectedPaths: Seq[String],
                         commitId: String,
                         timestamp: Long,
                         author: Author,
                         comment: String,
                         date: String, // TODO, but note that this is not unix time
                         id: String,
                         msg: String,
                         paths: Seq[EditedPath])

object ChangeSetItem {
  implicit val json = Json.format[ChangeSetItem]
}

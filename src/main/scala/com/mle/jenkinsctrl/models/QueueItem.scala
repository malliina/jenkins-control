package com.mle.jenkinsctrl.models

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * @author mle
  */
case class QueueItem(actions: Seq[QueueActions],
                     blocked: Boolean,
                     buildable: Boolean,
                     id: QueueId,
                     inQueueSince: DateTime,
                     params: String,
                     stuck: Boolean,
                     task: ConciseJob,
                     url: String,
                     why: Option[String],
                     cancelled: Option[Boolean],
                     executable: Option[Build],
                     timestamp: Option[DateTime]) {
  def isInQueue = why.isDefined

  def isInProgress = !(buildExists || cancelled.isDefined)

  def buildExists = executable.isDefined
}

object QueueItem {
  implicit val json = Json.format[QueueItem]
}

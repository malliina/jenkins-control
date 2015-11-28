package com.mle.jenkinsctrl.models

import com.mle.concurrent.Completable
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * @author mle
  */
case class QueueProgress(actions: Seq[QueueActions],
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
                         timestamp: Option[DateTime]) extends Completable {
  def isInQueue = why.isDefined

  def isInProgress = !isCompleted

  def isCompleted = buildExists || cancelled.isDefined

  def buildExists = executable.isDefined
}

object QueueProgress {
  implicit val json = Json.format[QueueProgress]
}

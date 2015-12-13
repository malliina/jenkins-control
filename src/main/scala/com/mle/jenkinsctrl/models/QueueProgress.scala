package com.mle.jenkinsctrl.models

import com.mle.concurrent.Completable
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * @author mle
  */
case class QueueProgress(actions: Actions,
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
  def isCompleted = executable.isDefined || cancelled.isDefined
}

object QueueProgress {
  implicit val json = Json.format[QueueProgress]
}

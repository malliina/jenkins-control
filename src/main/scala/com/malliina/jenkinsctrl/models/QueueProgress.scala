package com.malliina.jenkinsctrl.models

import java.time.Instant

import com.malliina.concurrent.Completable
import play.api.libs.json.Json

case class QueueProgress(actions: Actions,
                         blocked: Boolean,
                         buildable: Boolean,
                         id: QueueId,
                         inQueueSince: Instant,
                         params: String,
                         stuck: Boolean,
                         task: ConciseJob,
                         url: String,
                         why: Option[String],
                         cancelled: Option[Boolean],
                         executable: Option[Build],
                         timestamp: Option[Instant]) extends Completable {
  def isCompleted = executable.isDefined || cancelled.isDefined
}

object QueueProgress {
  implicit val json = Json.format[QueueProgress]
}

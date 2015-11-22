package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.Formats
import org.joda.time.DateTime
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

/**
  * @author mle
  */
case class BuildDetails(actions: Actions,
                        building: Boolean,
                        displayName: String,
                        duration: Duration,
                        estimatedDuration: Duration,
                        fullDisplayName: String,
                        id: String,
                        keepLog: Boolean,
                        number: Int,
                        queueId: QueueId,
                        result: Option[BuildResult],
                        timestamp: DateTime,
                        url: Url,
                        builtOn: String,
                        changeSet: ChangeSet,
                        culprits: Seq[Author]) {
  def isComplete = result.isDefined
}

object BuildDetails {
  implicit val (dateTime, duration) = (Formats.dateTime, Formats.duration)
  implicit val json = Json.format[BuildDetails]
}

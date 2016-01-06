package com.malliina.jenkinsctrl.models

import com.malliina.concurrent.Completable
import com.malliina.jenkinsctrl.json.Formats
import org.joda.time.DateTime
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

/**
  * @author mle
  */
case class BuildDetails(actions: Actions,
                        building: Boolean,
                        displayName: Option[String],
                        description: Option[String],
                        duration: Duration,
                        estimatedDuration: Duration,
                        fullDisplayName: String,
                        id: String,
                        keepLog: Boolean,
                        number: BuildNumber,
                        queueId: Option[QueueId],
                        result: Option[BuildResult],
                        timestamp: DateTime,
                        url: Url,
                        builtOn: String,
                        changeSet: ChangeSet,
                        culprits: Seq[Author]) extends Completable {
  def isCompleted: Boolean = result.isDefined
}

object BuildDetails {
  implicit val (dateTime, duration) = (Formats.dateTime, Formats.duration)
  implicit val json = Json.format[BuildDetails]
}

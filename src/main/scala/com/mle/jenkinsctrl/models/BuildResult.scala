package com.mle.jenkinsctrl.models

import com.mle.json.JsonFormats
import org.joda.time.DateTime
import play.api.libs.json.{Writes, Reads, Format, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationLong

/**
  * @author mle
  */
case class BuildResult(actions: Actions,
                       building: Boolean,
                       displayName: String,
                       duration: Duration,
                       estimatedDuration: Duration,
                       fullDisplayName: String,
                       id: String,
                       keepLog: Boolean,
                       number: Int,
                       queueId: Int,
                       result: String,
                       timestamp: DateTime,
                       url: Url,
                       builtOn: String,
                       changeSet: ChangeSet)

object BuildResult {
  implicit val duration = Format(
    Reads[Duration](_.validate[Long].map(_.millis)),
    Writes[Duration](d => Json.toJson(d.toMillis))
  )
  implicit val dateTime = Format(
    Reads[DateTime](json => json.validate[Long].map(ms => new DateTime(ms))),
    Writes[DateTime](date => Json.toJson(date.getMillis))
  )
  implicit val json = Json.format[BuildResult]
}

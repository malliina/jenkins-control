package com.mle.jenkinsctrl.json

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, Reads, Writes}

import scala.concurrent.duration.{Duration, DurationLong}

/**
  * @author mle
  */
object Formats {
  implicit val dateTime: Format[DateTime] = Format(
    Reads[DateTime](json => json.validate[Long].map(ms => new DateTime(ms))),
    Writes[DateTime](date => Json.toJson(date.getMillis))
  )
  implicit val duration: Format[Duration] = Format(
    Reads[Duration](_.validate[Long].map(_.millis)),
    Writes[Duration](d => Json.toJson(d.toMillis))
  )
}

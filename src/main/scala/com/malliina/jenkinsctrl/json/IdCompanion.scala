package com.malliina.jenkinsctrl.json

import com.malliina.jenkinsctrl.models.IdEntity
import play.api.libs.json.{Format, Json, Reads, Writes}

/**
  * @author mle
  */
trait IdCompanion[T <: IdEntity] {
  def apply(id: Long): T

  implicit val json = Format[T](
    Reads[T](_.validate[Long].map(apply)),
    Writes[T](t => Json.toJson(t.id))
  )
}

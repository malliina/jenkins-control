package com.mle.jenkinsctrl.json

import com.mle.jenkinsctrl.models.NamedEntity
import play.api.libs.json.JsSuccess

/**
  * @author mle
  */
trait NamedCompanion[T <: NamedEntity] extends JsonUtils {
  def apply(name: String): T

  implicit val json = stringFormat[T](s => JsSuccess(apply(s)), _.name)
}

package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.JsonUtils
import play.api.libs.json.JsSuccess

/**
  * @author mle
  */
case class SHA1(hash: String)

object SHA1 extends JsonUtils {
  val JsonKey = "SHA1"
  implicit val json = stringFormat[SHA1](s => JsSuccess(apply(s)), _.hash)
}

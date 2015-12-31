package com.malliina.jenkinsctrl.json

import play.api.libs.json._

/**
  * @author mle
  */
trait JsonUtils {
  def stringFormat[T](read: String => JsResult[T], write: T => String): Format[T] = {
    Format(Reads(_.validate[String].flatMap(read)), Writes[T](t => Json.toJson(write(t))))
  }
}

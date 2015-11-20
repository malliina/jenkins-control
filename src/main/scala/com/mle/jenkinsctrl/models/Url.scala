package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.JsonUtils
import play.api.libs.json.{JsError, JsSuccess}

/**
  * @author mle
  */
case class Url private(url: String)

object Url extends JsonUtils {
  implicit val json = stringFormat[Url](
    s => build(s).map(u => JsSuccess(u)).getOrElse(JsError(s"Not a valid URL: $s")),
    u => u.url)
  val baseUrlRegex = """(https?://.+)$""".r

  def build(url: String): Option[Url] = {
    baseUrlRegex.findFirstIn(url).map(apply)
  }
}

package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.JsonUtils
import play.api.libs.json.JsSuccess

/**
  * @author mle
  */
case class Url private(url: String) {
  override def toString = url

  def ?(q: String) = append(s"?$q")

  def /(next: String) = {
    val prefix = if (url endsWith "/") "" else "/"
    val suffix = if (next startsWith "/") next.drop(1) else next
    append(s"$prefix$suffix")
  }

  def append(path: String) = Url(s"$url$path")
}

object Url extends JsonUtils {
  implicit val json = stringFormat[Url](s => JsSuccess(apply(s)), u => u.url)
  val Http = "http"
  val supportedProtocols = Seq(Http, "https", "ws", "wss")
  val defaultProtocol = Http

  def build(url: String): Url = {
    if (supportedProtocols.exists(proto => url.startsWith(s"$proto://"))) Url(url)
    else Url(s"$defaultProtocol://$url")
  }
}

package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.JsonUtils
import play.api.libs.json.JsSuccess

case class Url private(url: String) {
  override def toString = url

  def ?(q: String) = append(s"?$q")

  def /(next: String) = {
    val prefix = if (url endsWith "/") "" else "/"
    val suffix = if (next startsWith "/") next.drop(1) else next
    append(s"$prefix$suffix")
  }

  def append(path: String) = Url(s"$url$path")

  def protocol = url.takeWhile(_ != ':')

  def toHttps: Url = {
    val suffix = url.drop(protocol.length)
    Url.build(s"${Url.Https}$suffix")
  }

  def isHttps = protocol == Url.Https
}

object Url extends JsonUtils {
  implicit val json = stringFormat[Url](s => JsSuccess(apply(s)), u => u.url)
  val Http = "http"
  val Https = "https"
  val supportedProtocols = Seq(Http, Https, "ws", "wss")
  val defaultProtocol = Http

  def build(url: String): Url = {
    if (supportedProtocols.exists(proto => url.startsWith(s"$proto://"))) Url(url)
    else Url(s"$defaultProtocol://$url")
  }
}

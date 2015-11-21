package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.JsonUtils
import play.api.libs.json.JsSuccess

/**
  * @author mle
  */
case class Url private(url: String) {
  override def toString = url
}

object Url extends JsonUtils {
  implicit val json = stringFormat[Url](s => JsSuccess(apply(s)), u => u.url)
  val supportedProtocols = Seq("http", "https", "ws", "wss")

  def fromUrl(host: Url, path: String): Url = {
    val prefixedPath = if (path startsWith "/") path else s"/$path"
    Url(s"${host.url}$prefixedPath")
  }

  def build(url: String): Url = {
    if (supportedProtocols.exists(proto => url.startsWith(s"$proto://"))) Url(url)
    else Url(s"http://$url")
  }
}

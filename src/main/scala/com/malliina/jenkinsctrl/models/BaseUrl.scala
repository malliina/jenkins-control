package com.malliina.jenkinsctrl.models

/**
  * @author mle
  */
case class BaseUrl private(base: String)

object BaseUrl {
  val baseUrlRegex = """(https?://.+)""".r
  val ipRegex = """\\d+\\.\\d+\\.\\d+\\.\\d+""".r

  def build(url: String): Option[BaseUrl] = {
    baseUrlRegex.findFirstIn(url).map(apply)
  }
}

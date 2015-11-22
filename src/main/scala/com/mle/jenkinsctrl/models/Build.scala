package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @param number build number
  * @param url this URL is not to be trusted as the IP is incorrect in tests; instead use `number`
  */
case class Build(number: BuildNumber, url: Url)

object Build {
  implicit val json = Json.format[Build]
}

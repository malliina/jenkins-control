package com.mle.jenkinsctrl.models

import play.api.libs.json.Json

/**
  * @author mle
  */
case class VerboseJob(name: JobName,
                      description: String,
                      url: Url,
                      color: Color,
                      builds: Seq[Build],
                      nextBuildNumber: Int,
                      buildable: Boolean,
                      inqueue: Boolean,
                      concurrentBuild: Boolean) extends Job

object VerboseJob {
  implicit val json = Json.format[VerboseJob]
}

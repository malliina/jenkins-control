package com.malliina.jenkinsctrl.models

import com.malliina.json.JsonEnum

/**
  * @author mle
  */
case object BuildSuccess extends BuildResult("SUCCESS")

case object BuildUnstable extends BuildResult("UNSTABLE")

case object BuildFailure extends BuildResult("FAILURE")

case object BuildNotBuilt extends BuildResult("NOT_BUILT")

case object BuildAborted extends BuildResult("ABORTED")

sealed abstract class BuildResult(val name: String) extends NamedEntity

object BuildResult extends JsonEnum[BuildResult] {
  override lazy val all: Seq[BuildResult] = Seq(BuildSuccess, BuildUnstable, BuildFailure, BuildNotBuilt, BuildAborted)

  override def resolveName(item: BuildResult): String = item.name
}

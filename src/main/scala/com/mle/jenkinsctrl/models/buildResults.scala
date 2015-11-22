package com.mle.jenkinsctrl.models

import com.mle.json.JsonEnum

/**
  * @author mle
  */
case object Success extends BuildResult("SUCCESS")

case object Unstable extends BuildResult("UNSTABLE")

case object Failure extends BuildResult("FAILURE")

case object NotBuilt extends BuildResult("NOT_BUILT")

case object Aborted extends BuildResult("ABORTED")

sealed abstract class BuildResult(val name: String) extends NamedEntity

object BuildResult extends JsonEnum[BuildResult] {
  override lazy val all: Seq[BuildResult] = Seq(Success, Unstable, Failure, NotBuilt, Aborted)

  override def resolveName(item: BuildResult): String = item.name
}

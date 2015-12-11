package com.mle.jenkinsctrl.models

/**
  * @author mle
  */
case class BuildOrder(job: JobName, parameters: Map[String, String])

object BuildOrder {
  def simple(job: JobName) = BuildOrder(job, Map.empty[String, String])
}

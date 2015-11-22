package com.mle.jenkinsctrl.models

/**
  * @author mle
  */
sealed trait BuildProgress

case class QueueUpdate(info: QueueItem) extends BuildProgress

case class BuildUpdate(info: BuildDetails) extends BuildProgress

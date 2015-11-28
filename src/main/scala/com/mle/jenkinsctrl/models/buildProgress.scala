package com.mle.jenkinsctrl.models

import com.mle.concurrent.Completable

/**
  * @author mle
  */
sealed trait BuildProgress {
  def info: Completable
}

case class QueueUpdate(info: QueueProgress) extends BuildProgress

case class BuildUpdate(info: BuildDetails) extends BuildProgress

case class ConsoleUpdate(info: ConsoleProgress) extends BuildProgress

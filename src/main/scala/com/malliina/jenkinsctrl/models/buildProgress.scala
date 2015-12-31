package com.malliina.jenkinsctrl.models

import com.malliina.concurrent.Completable

/**
  * @author mle
  */
sealed trait BuildProgress {
  def info: Completable
}

case class QueueUpdate(info: QueueProgress) extends BuildProgress

case class BuildUpdate(info: BuildDetails) extends BuildProgress

case class ConsoleUpdate(info: ConsoleProgress) extends BuildProgress

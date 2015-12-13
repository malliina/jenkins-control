package com.mle.jenkinsctrl.http

import com.mle.jenkinsctrl.models._
import rx.lang.scala.Observable

/**
  * @author mle
  */
case class BuildTask(order: BuildOrder,
                     queueTask: QueueTask,
                     consoleUpdates: Observable[ConsoleProgress],
                     buildUpdates: Observable[BuildDetails]) {
  val job = order.job
  val queueUpdates = queueTask.updates
  val materialized: Observable[BuildProgress] =
    queueUpdates.map(QueueUpdate.apply) merge
      buildUpdates.map(BuildUpdate.apply) merge
      consoleUpdates.map(ConsoleUpdate.apply)
}

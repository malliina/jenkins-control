package com.malliina.jenkinsctrl.http

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.jenkinsctrl.models._
import rx.lang.scala.Observable

import scala.concurrent.Future

/**
  * @author mle
  */
case class BuildTask(order: BuildOrder,
                     queueTask: QueueTask,
                     consoleUpdates: Observable[ConsoleProgress],
                     buildUpdates: Observable[BuildDetails],
                     result: Future[BuildResult]) {
  val isSuccess: Future[Boolean] = result.map(_ == BuildSuccess)
  val job = order.job
  val queueUpdates = queueTask.updates
  val materialized: Observable[BuildProgress] =
    queueUpdates.map(QueueUpdate.apply) merge
      buildUpdates.map(BuildUpdate.apply) merge
      consoleUpdates.map(ConsoleUpdate.apply)

  // how about map, flatMap?
}

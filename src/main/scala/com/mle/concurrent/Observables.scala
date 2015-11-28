package com.mle.concurrent

import rx.lang.scala.Observable

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author mle
  */
object Observables {
  def pollUntilComplete[T <: Completable](pollInterval: FiniteDuration)(f: => Future[T])(implicit ec: ExecutionContext): Observable[T] = {
    Observable.interval(pollInterval)
      .concatMap(_ => Observable.from(f))
      .takeUntil(_.isCompleted)
  }
}

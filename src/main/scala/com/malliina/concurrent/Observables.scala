package com.malliina.concurrent

import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author mle
  */
object Observables {
  def replayablePoll[T <: Completable](pollInterval: FiniteDuration)(f: => Future[T])(implicit ec: ExecutionContext): Observable[T] = {
    toReplay(pollUntilComplete(pollInterval)(f))
  }

  def toReplay[T](o: Observable[T]): Observable[T] = {
    val subject = ReplaySubject[T]()
    o.subscribe(subject)
    subject
  }

  def pollUntilComplete[T <: Completable](pollInterval: FiniteDuration)(f: => Future[T])(implicit ec: ExecutionContext): Observable[T] = {
    Observable.interval(pollInterval)
      .concatMap(_ => Observable.from(f))
      .takeUntil(_.isCompleted)
  }
}

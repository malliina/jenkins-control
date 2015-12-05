package com.mle.jenkinsctrl.http

import com.mle.jenkinsctrl.models.{QueueProgress, Url}
import rx.lang.scala.Observable

import scala.concurrent.Future

/**
  * @author mle
  */
case class QueueTask(url: Future[Url], updates: Observable[QueueProgress])

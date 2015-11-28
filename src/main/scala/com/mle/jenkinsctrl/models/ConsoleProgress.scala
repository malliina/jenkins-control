package com.mle.jenkinsctrl.models

import com.mle.concurrent.Completable

/**
  * @author mle
  */
case class ConsoleProgress(response: String,
                           offset: ByteOffset,
                           isCompleted: Boolean) extends Completable {
  def +(next: ConsoleProgress) = append(next)

  def append(next: ConsoleProgress) = ConsoleProgress(response + next.response, next.offset, next.isCompleted)
}

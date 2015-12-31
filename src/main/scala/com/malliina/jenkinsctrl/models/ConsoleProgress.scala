package com.malliina.jenkinsctrl.models

import com.malliina.concurrent.Completable

/**
  * @author mle
  */
case class ConsoleProgress(response: String,
                           offset: ByteOffset,
                           isCompleted: Boolean) extends Completable {
  def +(next: ConsoleProgress) = append(next)

  def append(next: ConsoleProgress) = ConsoleProgress(response + next.response, next.offset, next.isCompleted)
}

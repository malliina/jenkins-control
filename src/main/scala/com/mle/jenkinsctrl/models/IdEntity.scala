package com.mle.jenkinsctrl.models

/**
  * @author mle
  */
trait IdEntity {
  def id: Long

  override def toString: String = s"$id"
}

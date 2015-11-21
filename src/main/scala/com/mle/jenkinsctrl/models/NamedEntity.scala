package com.mle.jenkinsctrl.models

/**
  * @author mle
  */
trait NamedEntity {
  def name: String

  override def toString = name
}

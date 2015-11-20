package com.mle.jenkinsctrl.models

/**
  * @author mle
  */
trait Job {
  def name: JobName

  def url: Url

  def color: Color
}

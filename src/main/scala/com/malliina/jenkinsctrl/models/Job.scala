package com.malliina.jenkinsctrl.models

/**
  * @author mle
  */
trait Job {
  def name: JobName

  def url: Url

  def color: Color
}

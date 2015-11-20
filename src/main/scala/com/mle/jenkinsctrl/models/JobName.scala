package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class JobName(name: String) extends NamedEntity

object JobName extends NamedCompanion[JobName]

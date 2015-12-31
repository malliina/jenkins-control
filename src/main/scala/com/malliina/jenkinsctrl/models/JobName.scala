package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class JobName(name: String) extends NamedEntity

object JobName extends NamedCompanion[JobName]

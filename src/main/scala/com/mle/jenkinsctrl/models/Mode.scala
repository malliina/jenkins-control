package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class Mode(name: String) extends NamedEntity

object Mode extends NamedCompanion[Mode]

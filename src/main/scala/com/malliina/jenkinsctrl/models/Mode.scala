package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class Mode(name: String) extends NamedEntity

object Mode extends NamedCompanion[Mode]

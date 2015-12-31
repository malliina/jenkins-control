package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class Color(name: String) extends NamedEntity

object Color extends NamedCompanion[Color]

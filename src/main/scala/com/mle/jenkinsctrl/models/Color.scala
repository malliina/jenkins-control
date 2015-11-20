package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.NamedCompanion

/**
  * @author mle
  */
case class Color(name: String) extends NamedEntity

object Color extends NamedCompanion[Color]

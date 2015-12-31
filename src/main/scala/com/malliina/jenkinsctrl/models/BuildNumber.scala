package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.IdCompanion

/**
  * @author mle
  */
case class BuildNumber(id: Long) extends IdEntity

object BuildNumber extends IdCompanion[BuildNumber]

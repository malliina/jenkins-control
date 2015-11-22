package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.IdCompanion

/**
  * @author mle
  */
case class BuildNumber(id: Long) extends IdEntity

object BuildNumber extends IdCompanion[BuildNumber]

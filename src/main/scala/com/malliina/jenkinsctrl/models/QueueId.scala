package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.IdCompanion

/**
  * @author mle
  */
case class QueueId(id: Long) extends IdEntity

object QueueId extends IdCompanion[QueueId]

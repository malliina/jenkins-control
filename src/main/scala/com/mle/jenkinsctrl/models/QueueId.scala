package com.mle.jenkinsctrl.models

import com.mle.jenkinsctrl.json.IdCompanion

/**
  * @author mle
  */
case class QueueId(id: Long) extends IdEntity

object QueueId extends IdCompanion[QueueId]

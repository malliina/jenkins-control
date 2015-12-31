package com.malliina.jenkinsctrl.models

import com.malliina.jenkinsctrl.json.IdCompanion

/**
  * @author mle
  */
case class ByteOffset(start: Long) extends IdEntity {
  override def id: Long = start
}

object ByteOffset extends IdCompanion[ByteOffset] {
  val Zero = ByteOffset(0L)
}

package com.mle.jenkinsctrl.models

import play.api.libs.json._

/**
  * @author mle
  */
case class Actions(causes: Seq[Cause],
                   parameters: Seq[BuildParameter],
                   buildAction: Option[BuildAction])

object Actions {
  val empty = Actions(Nil, Nil, None)

  val CausesKey = "causes"
  val ParametersKey = "parameters"

  val writer = Json.writes[Actions]
  val reader = Reads[Actions](json => json.validate[Seq[JsValue]].map(fromActions))

  implicit val json: Format[Actions] = Format(reader, writer)

  def fromActions(actions: Seq[JsValue]): Actions = {
    fromActions(actions.filter(_ != Json.obj()), empty)
  }

  def fromActions(actions: Seq[JsValue], acc: Actions): Actions = {
    actions.toList match {
      case Nil =>
        acc
      case head :: tail =>
        def params = (head \ ParametersKey).asOpt[Seq[BuildParameter]]
        def causes = (head \ CausesKey).asOpt[Seq[Cause]]
        def buildAction = head.asOpt[BuildAction]
        val newAcc =
          params.map(bps => acc.copy(parameters = bps)) orElse
            causes.map(cs => acc.copy(causes = cs)) orElse
            buildAction.map(ba => acc.copy(buildAction = Option(ba))) getOrElse
            acc
        fromActions(tail, newAcc)
    }
  }
}

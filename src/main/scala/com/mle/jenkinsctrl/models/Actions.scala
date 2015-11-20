package com.mle.jenkinsctrl.models

import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * @author mle
  */
case class Actions(causes: Seq[Cause],
                   buildAction: BuildAction)

object Actions {
  val writer = Json.writes[Actions]
  val reader = Reads[Actions](json => json.validate[Seq[JsValue]].flatMap(fromActions))
  implicit val json: Format[Actions] = Format(reader, writer)

  def fromActions(actions: Seq[JsValue]): JsResult[Actions] = {
    fromActions(actions, JsError("No causes"), JsError("No build actions"))
  }

  def fromActions(actions: Seq[JsValue],
                  causesAction: JsResult[Seq[Cause]],
                  buildAction: JsResult[BuildAction]): JsResult[Actions] = {
    (causesAction, buildAction) match {
      case (JsSuccess(ca, _), JsSuccess(ba, _)) =>
        JsSuccess(Actions(ca, ba))
      case (ca, ba) =>
        actions.toList match {
          case head :: tail =>
            fromActions(tail, ca.orElse(head.validate[Seq[Cause]]), ba.orElse(head.validate[BuildAction]))
          case _ =>
            mergeErrors(Seq(ca, ba)) getOrElse JsError("Unknown actions")
        }
    }
  }

  def mergeErrors(results: Seq[JsResult[_]]): Option[JsError] = {
    val jsErrors = results.flatMap {
      case JsError(es) => Option(es)
      case _ => None
    }
    if (jsErrors.isEmpty) {
      None
    } else {
      val hm = jsErrors.foldLeft[Seq[(JsPath, Seq[ValidationError])]](Nil)((acc, errs) => JsError.merge(acc, errs))
      Option(JsError(hm))
    }
  }
}

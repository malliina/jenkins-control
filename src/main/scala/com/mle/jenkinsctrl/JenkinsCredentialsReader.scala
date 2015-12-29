package com.mle.jenkinsctrl

import java.nio.file.Path

import com.mle.file.{FileUtilities, StorageFile}
import com.mle.jenkinsctrl.JenkinsCredentialsReader._
import com.mle.jenkinsctrl.models.{Token, Url}
import com.mle.util.BaseConfigReader

/**
  * @author mle
  */
class JenkinsCredentialsReader extends BaseConfigReader[JenkinsCredentials] {
  override def filePath: Option[Path] = Some(FileUtilities.userHome / "keys" / "jenkins.key")

  override def fromMapOpt(map: Map[String, String]): Option[JenkinsCredentials] = {
    for {
      host <- map get Host
      user <- map get User
      pass <- map get Pass
      token <- map get TokenKey
    } yield JenkinsCredentials(Url.build(host), user, pass, Token(token))
  }
}

object JenkinsCredentialsReader {
  val Host = "host"
  val User = "user"
  val Pass = "pass"
  val TokenKey = "token"
}

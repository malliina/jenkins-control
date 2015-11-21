package com.mle.jenkinsctrl

import java.nio.file.Path

import com.mle.file.{FileUtilities, StorageFile}
import com.mle.jenkinsctrl.CredentialsReader._
import com.mle.jenkinsctrl.models.Url
import com.mle.util.BaseConfigReader

/**
  * @author mle
  */
class CredentialsReader extends BaseConfigReader[JenkinsCredentials] {
  override def userHomeConfPath: Path = FileUtilities.userHome / "keys" / "jenkins.key"

  override def resourceCredential: String = "unused"

  override def fromMapOpt(map: Map[String, String]): Option[JenkinsCredentials] = {
    for {
      host <- map get Host
      user <- map get User
      pass <- map get Pass
    } yield JenkinsCredentials(Url.build(host), user, pass)
  }
}

object CredentialsReader {
  val Host = "host"
  val User = "user"
  val Pass = "pass"
}

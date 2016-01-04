package com.malliina.jenkinsctrl

import com.malliina.jenkinsctrl.models.{Token, Url}

/**
  * @author mle
  */
case class JenkinsCredentials(host: Url, user: String, token: Token)

package com.mle.jenkinsctrl

import com.mle.jenkinsctrl.models.{Token, Url}

/**
  * @author mle
  */
case class JenkinsCredentials(host: Url, user: String, pass: String, token: Token)

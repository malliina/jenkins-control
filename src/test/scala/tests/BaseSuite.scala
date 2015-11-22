package tests

import org.scalatest.FunSuite

/**
  * @author mle
  */
class BaseSuite extends FunSuite {
  org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.WARN)
}

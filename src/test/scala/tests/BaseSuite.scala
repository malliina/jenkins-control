package tests

import org.scalatest.FunSuite

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class BaseSuite extends FunSuite {
  org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.WARN)

  def await[T](f: Future[T]): T = Await.result(f, 5.seconds)

  def awaitForLong[T](f: Future[T]): T = Await.result(f, 2.minutes)
}

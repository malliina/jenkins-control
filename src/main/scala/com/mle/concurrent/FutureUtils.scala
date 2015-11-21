package com.mle.concurrent

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Monad

/**
  * @author mle
  */
object FutureUtils {
  implicit val defaultMonad = futureMonad(ExecutionContext.Implicits.global)
  implicit val cachedMonad = futureMonad(ExecutionContexts.cached)

  def futureMonad(ec: ExecutionContext) = new Monad[Future] {
    override def point[A](a: => A): Future[A] = {
      Future.successful(a)
    }

    override def bind[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = {
      fa.flatMap(a => f(a))(ec)
    }
  }
}

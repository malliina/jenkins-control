package tests

import rx.lang.scala.{Observable, Subject}

import scala.concurrent.Promise

class RxTests extends BaseSuite {
  test("Observable.error propagates to subscriber") {
    val o = Observable.error(new Exception("boom"))
    val p = Promise[Int]()
    o.subscribe(n => p.trySuccess(0), e => p.trySuccess(1), () => p.trySuccess(2))
    val result = await(p.future)
    assert(result === 1)
  }

  test("Observable.onError propagates to subscriber") {
    val s = Subject[Int]
    s.onError(new Exception("Boom"))
    val p = Promise[Int]()
    s.subscribe(n => p.trySuccess(0), e => p.trySuccess(1), () => p.trySuccess(2))
    val result = await(p.future)
    assert(result === 1)
  }

  test("Observable.lastOption does not emit Option in case of failure") {
    val s = Subject[Int]()
    val p = Promise[Option[Int]]()
    s.onNext(42)
    s.onError(new Exception("Boom"))
    s.lastOption.subscribe(n => p.trySuccess(n), e => p.trySuccess(Some(1)), () => p.trySuccess(Some(2)))
    val result = await(p.future)
    assert(result === Option(1))
  }
}

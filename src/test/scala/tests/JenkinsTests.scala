package tests

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.jenkinsctrl.CredentialsReader
import com.mle.jenkinsctrl.http.JenkinsClient
import org.scalatest.FunSuite

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
  * @author mle
  */
class JenkinsTests extends FunSuite {
  val creds = new CredentialsReader().load

  test("GET overview") {
    val client = new JenkinsClient(creds)
    val request = client.overview()
    val response = Await.result(request, 5.seconds)
    assert(response.numExecutors > 0)
    client.close()
  }

  test("GET job details") {
    val client = new JenkinsClient(creds)
    val verboseJobs = for {
      overview <- client.overview()
      jobNames = overview.jobs.map(_.name)
      verboseJobs <- Future.traverse(jobNames)(name => client.job(name))
    } yield verboseJobs
    val response = Await.result(verboseJobs, 5.seconds)
    assert(response.size >= 0)
    client.close()
  }
}

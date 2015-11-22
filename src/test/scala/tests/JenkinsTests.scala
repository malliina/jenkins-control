package tests

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.jenkinsctrl.CredentialsReader
import com.mle.jenkinsctrl.http.JenkinsClient
import com.mle.jenkinsctrl.models.VerboseJob

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
  * @author mle
  */
class JenkinsTests extends BaseSuite {
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
    val verboseJobs: Future[Seq[VerboseJob]] = for {
      overview <- client.overview()
      jobNames = overview.jobs.map(_.name)
      verboseJobs <- Future.traverse(jobNames)(name => client.job(name))
    } yield verboseJobs
    val response = Await.result(verboseJobs, 5.seconds)
    assert(response.size >= 0)
    client.close()
  }

  test("GET build details") {
    val client = new JenkinsClient(creds)
    val jobsRequest: Future[Seq[VerboseJob]] = for {
      overview <- client.overview()
      jobNames = overview.jobs.map(_.name)
      verboseJobs <- Future.traverse(jobNames)(name => client.job(name))
    } yield verboseJobs
    val jobs = Await.result(jobsRequest, 5.seconds)
    assert(jobs.size >= 0)
    val job = jobs.head
    val builds = job.builds
    assert(builds.size >= 0)
    val detailsRequest = client.buildDetails(job.name, builds.head.number)
    Await.result(detailsRequest, 5.seconds)
    client.close()
  }

  //  val testJob = JobName("testjob")
  //
  //  test("POST to run job, wait until removed from queue") {
  //    val client = new JenkinsClient(creds)
  //    val request = client.enqueueUntilBuilding(testJob)
  //    println(request.toBlocking.last)
  //    client.close()
  //  }
  //
  //  test("follow build") {
  //    val client = new JenkinsClient(creds)
  //    val request = client.follow(testJob, BuildNumber(29))
  //    Thread sleep 12000
  //    //    val details: VerboseJob = Await.result(client.job(buildJob), 100.seconds)
  //    //    println(details)
  //    client.close()
  //  }
  //
  //  test("POST to run job") {
  //    val client = new JenkinsClient(creds)
  //    val p = Promise[Unit]()
  //    val f = p.future
  //    client.buildWithProgress(testJob).subscribe(
  //      update => println(update),
  //      error => println(s"Error $error"),
  //      () => {
  //        println("Completed.")
  //        p.trySuccess(())
  //      })
  //    Await.result(f, 200.seconds)
  //    client.close()
  //  }
}

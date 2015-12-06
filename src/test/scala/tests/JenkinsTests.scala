package tests

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.jenkinsctrl.CredentialsReader
import com.mle.jenkinsctrl.http.JenkinsClient
import com.mle.jenkinsctrl.models._
import com.mle.util.Util
import org.scalatest.BeforeAndAfter

import scala.concurrent.Future

/**
  * @author mle
  */
class JenkinsTests extends BaseSuite with BeforeAndAfter {
  val testJobConfig =
    """
      |<project>
      |  <actions/>
      |  <description></description>
      |  <keepDependencies>false</keepDependencies>
      |  <properties/>
      |  <scm class="hudson.scm.NullSCM"/>
      |  <canRoam>true</canRoam>
      |  <disabled>false</disabled>
      |  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
      |  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
      |  <triggers/>
      |  <concurrentBuild>false</concurrentBuild>
      |  <builders/>
      |  <publishers/>
      |  <buildWrappers/>
      |</project>
    """.stripMargin

  val testJob = JobName("testjob")

  val creds = new CredentialsReader().load

  test("GET overview") {
    withClient { client =>
      val response = await(client.overview())
      assert(response.numExecutors > 0)
    }
  }

  test("GET job details") {
    withClient { client =>
      val verboseJobs: Future[Seq[VerboseJob]] = for {
        overview <- client.overview()
        jobNames = overview.jobs.map(_.name)
        verboseJobs <- Future.traverse(jobNames)(name => client.job(name))
      } yield verboseJobs
      val response = await(verboseJobs)
      assert(response.size >= 0)
    }
  }

  test("GET build details") {
    withClient { client =>
      val jobsRequest: Future[Seq[VerboseJob]] = for {
        overview <- client.overview()
        jobNames = overview.jobs.map(_.name)
        verboseJobs <- Future.traverse(jobNames)(name => client.job(name))
      } yield verboseJobs
      val jobs = await(jobsRequest)
      assert(jobs.size >= 0)
      val job = jobs.head
      val builds = job.builds
      if (builds.nonEmpty) {
        val detailsRequest = client.buildDetails(job.name, builds.head.number)
        await(detailsRequest)
      }
    }
  }

  test("POST to run job task") {
    withClient { client =>
      def followWork() = client.buildWithProgressTask(testJob)
      val work = for {
        creation <- client.createJob(testJob, testJobConfig)
        task = followWork().materialized.toBlocking.lastOption
        deletion <- client.deleteJob(testJob)
      } yield task
      val lastProgress = awaitForLong(work)
      assert(lastProgress.exists(_.info.isCompleted))
    }
  }

//  test("POST to build with parameters") {
//    withClient { client =>
//      val request = client.buildWithParameters(testJob, "foo" -> "bar")
//      val url = await(request)
//      assert(url.url startsWith "http")
//    }
//  }

  test("create/delete job") {
    withClient { client =>
      val creation = await(client.createJob(testJob, testJobConfig))
      assert(creation.status === 200)
      val deletion = await(client.deleteJob(testJob))
      assert(deletion.status === 302)
    }
  }

  override protected def before(fun: => Any): Unit = {
    withClient(client => await(client.createJob(testJob, testJobConfig)))
  }

  override protected def after(fun: => Any): Unit = {
    withClient(client => await(client.deleteJob(testJob)))
  }

  def withClient[T](f: JenkinsClient => T): T = Util.using(new JenkinsClient(creds))(f)
}

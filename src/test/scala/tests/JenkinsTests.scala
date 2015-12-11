package tests

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.jenkinsctrl.CredentialsReader
import com.mle.jenkinsctrl.http.{JenkinsClient, ResponseException}
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

  val parameterizedBuildConfig =
    """
      |<project>
      |  <actions/>
      |  <description></description>
      |  <keepDependencies>false</keepDependencies>
      |  <properties>
      |    <hudson.model.ParametersDefinitionProperty>
      |      <parameterDefinitions>
      |        <hudson.model.StringParameterDefinition>
      |          <name>VERSION</name>
      |          <description></description>
      |          <defaultValue></defaultValue>
      |        </hudson.model.StringParameterDefinition>
      |      </parameterDefinitions>
      |    </hudson.model.ParametersDefinitionProperty>
      |  </properties>
      |  <scm class="hudson.scm.NullSCM"/>
      |  <canRoam>true</canRoam>
      |  <disabled>false</disabled>
      |  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
      |  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
      |  <triggers/>
      |  <concurrentBuild>false</concurrentBuild>
      |  <builders>
      |    <hudson.tasks.Shell>
      |      <command>env</command>
      |    </hudson.tasks.Shell>
      |    <hudson.tasks.Shell>
      |      <command>echo the value of VERSION is $VERSION</command>
      |    </hudson.tasks.Shell>
      |  </builders>
      |  <publishers/>
      |  <buildWrappers/>
      |</project>
    """.stripMargin

  val testJob = JobName("testjob")
  val parameterizedJobName = JobName("para-test")

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

  test("POST to build job") {
    withClient { client =>
      def followWork() = client.buildWithProgressTask(BuildOrder.simple(testJob))
      val work = for {
        creation <- client.createJob(testJob, testJobConfig)
        task = followWork().materialized.toBlocking.lastOption
        deletion <- client.deleteJob(testJob)
      } yield task
      val lastProgress = awaitForLong(work)
      assert(lastProgress.exists(_.info.isCompleted))
    }
  }

  test("supplying no parameters to a parameterized build returns 400") {
    withClient { client =>
      val exception = for {
        creation <- client.createJob(parameterizedJobName, parameterizedBuildConfig)
        responseException = intercept[ResponseException](await(client.build(JobName("para"))))
        deletion <- client.deleteJob(parameterizedJobName)
      } yield responseException
      assert(await(exception).statusCode === 400)
    }
  }

  test("follow a parameterized build") {
    withClient { client =>
      val work = for {
        creation <- client.createJob(parameterizedJobName, parameterizedBuildConfig)
        task = client.buildWithProgressTask(BuildOrder(parameterizedJobName, Map("VERSION" -> "1.0.0"))).consoleUpdates.toBlocking.lastOption
        deletion <- client.deleteJob(parameterizedJobName)
      } yield task
      val maybeProgress = awaitForLong(work)
      assert(maybeProgress.exists(_.isCompleted))
    }
  }

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

import com.malliina.sbtutils.SbtProjects
import sbt._
import sbt.Keys._

/**
  * A scala build file template.
  */
object JenkinsControlBuild {

  lazy val jenkinsControl = SbtProjects.mavenPublishProject("jenkins-control")
    .settings(projectSettings: _*)

  val malliinaGroup = "com.malliina"

  lazy val projectSettings = Seq(
    version := "0.4.0",
    scalaVersion := "2.11.8",
    organization := malliinaGroup,
    crossScalaVersions := Seq("2.10.6", scalaVersion.value),
    fork in Test := true,
    libraryDependencies ++= Seq(
      malliinaGroup %% "util" % "2.1.0"
    )
  )
}

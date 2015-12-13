import com.mle.sbtutils.SbtProjects
import sbt._
import sbt.Keys._

/**
  * A scala build file template.
  */
object TemplateBuild extends Build {

  lazy val jenkinsControl = SbtProjects.testableProject("jenkins-control")
    .enablePlugins(bintray.BintrayPlugin)
    .settings(projectSettings: _*)

  val mleGroup = "com.github.malliina"

  lazy val projectSettings = Seq(
    version := "0.1.1",
    scalaVersion := "2.11.7",
    organization := mleGroup,
    crossScalaVersions := Seq("2.10.6", scalaVersion.value),
    fork in Test := true,
    libraryDependencies ++= Seq(
      mleGroup %% "util" % "2.0.0"
    ),
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}

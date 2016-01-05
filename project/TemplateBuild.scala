import com.malliina.sbtutils.SbtProjects
import sbt._
import sbt.Keys._

/**
  * A scala build file template.
  */
object TemplateBuild extends Build {

  lazy val jenkinsControl = SbtProjects.testableProject("jenkins-control")
    .enablePlugins(bintray.BintrayPlugin)
    .settings(projectSettings: _*)

  val malliinaGroup = "com.malliina"

  lazy val projectSettings = Seq(
    version := "0.3.2",
    scalaVersion := "2.11.7",
    organization := malliinaGroup,
    crossScalaVersions := Seq("2.10.6", scalaVersion.value),
    fork in Test := true,
    libraryDependencies ++= Seq(
      malliinaGroup %% "util" % "2.1.0"
    ),
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}

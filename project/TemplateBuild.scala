import com.mle.sbtutils.SbtProjects
import sbt._
import sbt.Keys._

/**
  * A scala build file template.
  */
object TemplateBuild extends Build {

  lazy val template = SbtProjects.testableProject("jenkins-control")
    .enablePlugins(bintray.BintrayPlugin)
    .settings(projectSettings: _*)

  lazy val projectSettings = Seq(
    version := "0.0.2",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.4", scalaVersion.value),
    fork in Test := true,
    libraryDependencies ++= Seq(
      "com.github.malliina" %% "util" % "2.0.0",
      "org.scalaz" %% "scalaz-core" % "7.1.5"
    ),
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}

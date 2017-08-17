import com.malliina.sbtutils.SbtProjects
import com.malliina.sbtutils.SbtUtils.{developerName, gitUserName}

lazy val jenkinsControl = SbtProjects.mavenPublishProject("jenkins-control")

version := "0.5.0"
scalaVersion := "2.12.3"
organization := "com.malliina"
gitUserName := "malliina"
developerName := "Michael Skogberg"
crossScalaVersions := Seq("2.10.6", "2.11.11", scalaVersion.value)
resolvers += Resolver.bintrayRepo("malliina", "maven")
libraryDependencies += "com.malliina" %% "util" % "2.8.0"

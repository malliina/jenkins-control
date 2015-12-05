import sbt.Keys._
import sbt._

object BuildBuild extends Build {

  override lazy val settings = super.settings ++ sbtPlugins ++ Seq(
    resolvers += ivyResolver("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins/"))
  )

  def ivyResolver(name: String, repoUrl: sbt.URL) =
    Resolver.url(name, repoUrl)(Resolver.ivyStylePatterns)

  def sbtPlugins = Seq(
    "com.github.malliina" %% "sbt-utils" % "0.2.1",
    "me.lessis" % "bintray-sbt" % "0.3.0"
  ) map addSbtPlugin

  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}

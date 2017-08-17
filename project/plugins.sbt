resolvers ++= Seq(
  ivyResolver("bintray-sbt-plugin-releases", "http://dl.bintray.com/content/sbt/sbt-plugin-releases"),
  ivyResolver("malliina bintray sbt", "https://dl.bintray.com/malliina/sbt-plugins/")
)

addSbtPlugin("com.malliina" %% "sbt-utils" % "0.6.3")

def ivyResolver(name: String, repoUrl: String) =
  Resolver.url(name, url(repoUrl))(Resolver.ivyStylePatterns)

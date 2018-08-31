import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "indicators"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory): _*)
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(PlayKeys.playDefaultPort := 8456)
  .settings(libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test)
  .settings(resolvers += Resolver.jcenterRepo)

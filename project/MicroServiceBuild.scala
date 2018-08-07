import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "indicators"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies.compile ++ AppDependencies.test
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  resolvers += "Sonatype OSS Snapshots" at "http://jcenter.bintray.com"

  val compile = Seq(
    "uk.gov.hmrc"      %% "bootstrap-play-25" % "1.7.0",
    "uk.gov.hmrc"      %% "play-url-binders"  % "2.1.0",
    "uk.gov.hmrc"      %% "git-client"        % "0.6.0",
    "uk.gov.hmrc"      %% "github-client"     % "1.21.0",
    "uk.gov.hmrc"      %% "domain"            % "5.2.0",
    "com.google.guava" % "guava"              % "18.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "hmrctest"           % "3.0.0"   % "test",
    "org.mockito"            % "mockito-all"         % "1.10.19" % "test",
    "org.pegdown"            % "pegdown"             % "1.4.2"   % "test",
    "com.github.tomakehurst" % "wiremock"            % "1.52"    % "test",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"   % "test"
  )
}

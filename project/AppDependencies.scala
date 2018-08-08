import sbt._

private object AppDependencies {

  import play.core.PlayVersion.current

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
    "com.typesafe.play"      %% "play-test"          % current   % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"   % "test"
  )
}

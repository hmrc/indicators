import sbt._

private object AppDependencies {

  import play.core.PlayVersion.current

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.9.0",
    // github-client uses play-ws 2.6.x and we want 2.5.x
    "uk.gov.hmrc"      %% "github-client" % "2.8.0" exclude ("com.typesafe.play", "play-ws_2.11"),
    "uk.gov.hmrc"      %% "domain"        % "5.2.0",
    "com.google.guava" % "guava"          % "18.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "hmrctest"           % "3.5.0-play-25" % "test",
    "org.mockito"            % "mockito-all"         % "1.10.19"       % "test",
    "org.pegdown"            % "pegdown"             % "1.4.2"         % "test",
    "com.github.tomakehurst" % "wiremock"            % "1.52"          % "test",
    "com.typesafe.play"      %% "play-test"          % current         % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"         % "test"
  )
}

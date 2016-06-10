import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "indicators"

  // override lazy val plugins: Seq[Plugins] = Seq(
  //   SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  // )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "4.2.1"
  private val playAuthVersion = "3.1.0"
  private val playHealthVersion = "1.1.0"
  private val playJsonLoggerVersion = "2.1.1"
  private val playUrlBindersVersion = "1.0.0"
  private val playConfigVersion = "2.0.1"
  private val domainVersion = "3.4.0"
  private val hmrcTestVersion = "1.4.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
    "uk.gov.hmrc" %% "git-client" % "0.6.0",
    "uk.gov.hmrc" %% "github-client" % "1.3.0",
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "com.google.guava" % "guava" % "18.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.52" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus" %% "play" % "1.2.0" % scope
      )
    }.test
  }


  def apply() = compile ++ Test()
}


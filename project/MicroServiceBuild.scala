import sbt.Keys._
import sbt._


object MicroServiceBuild extends Build with MicroService {

  val appName = "indicators"

  // override lazy val plugins: Seq[Plugins] = Seq(
  //   SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  // )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
import play.sbt.PlayImport._


  private val microserviceBootstrapVersion = "5.1.0"
  private val playAuthVersion = "4.0.0"
  private val playHealthVersion = "2.0.0"
  private val playJsonLoggerVersion = "5460c48e17aaea6fc83395a993fab1ab2cc90cbb"
  private val playLogbackJsonLoggerVersion = "3.0.0"
  private val playUrlBindersVersion = "0.2.0"
  private val playConfigVersion = "3.0.0"
  private val domainVersion = "3.7.0"
  private val hmrcTestVersion = "2.0.0"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"




  resolvers += "Sonatype OSS Snapshots" at "http://jcenter.bintray.com"


  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % playLogbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "git-client" % "0.6.0",
    "uk.gov.hmrc" %% "github-client" % "1.4.0",
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
        "org.mockito" % "mockito-all" % "1.10.19",
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.52" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
      )
    }.test
  }


  def apply() = compile ++ Test()
}


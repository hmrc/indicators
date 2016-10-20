import sbt._


object MicroServiceBuild extends Build with MicroService {

  val appName = "indicators"

  // override lazy val plugins: Seq[Plugins] = Seq(
  //   SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  // )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

//  import play.PlayImport._
  import play.core.PlayVersion
import play.sbt.PlayImport._

//  private val microserviceBootstrapVersion = "4.2.1"
//  private val playAuthVersion = "3.1.0"
//  private val playHealthVersion = "1.1.0"
//  private val playJsonLoggerVersion = "2.1.1"
//  private val playUrlBindersVersion = "1.0.0"
//  private val playConfigVersion = "2.0.1"
//  private val domainVersion = "3.4.0"
//  private val hmrcTestVersion = "1.4.0"

  private val microserviceBootstrapVersion = "beea4d092dadc2269102221e614da758b675e83a"
  private val playAuthVersion = "f6f8c52b0a23d0347fc685680f81a78e423563ce"
  private val playHealthVersion = "108d64e4fc18e3a9f2fe6379379d68ce82e0ef9e"
  private val playJsonLoggerVersion = "5460c48e17aaea6fc83395a993fab1ab2cc90cbb"
  private val playUrlBindersVersion = "0.2.0"
  private val playConfigVersion = "2c3c5543d868cc27679fa79dbb30e96d43712112"
  private val domainVersion = "3.7.0"
  private val hmrcTestVersion = "0.3.0"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"




//  private val microserviceBootstrapVersion = "9be71ea44ce70a914166613e7330b64e7c49a49e"              private val microserviceBootstrapVersion = "4.2.1"
//  private val playAuthVersion = "f6f8c52b0a23d0347fc685680f81a78e423563ce"                           private val playAuthVersion = "3.1.0"
//  private val playHealthVersion = "108d64e4fc18e3a9f2fe6379379d68ce82e0ef9e"                         private val playHealthVersion = "1.1.0"
//  private val playJsonLoggerVersion = "5460c48e17aaea6fc83395a993fab1ab2cc90cbb"                     private val playJsonLoggerVersion = "2.1.1"
//  private val playUrlBindersVersion = "0.2.0"                                                        private val playUrlBindersVersion = "1.0.0"
//  private val playConfigVersion = "2c3c5543d868cc27679fa79dbb30e96d43712112"                         private val playConfigVersion = "2.0.1"
//  private val hmrcTestVersion = "0.3.0"                                                              private val domainVersion = "3.4.0"



  val compile = Seq(
    ws,
//    "uk.gov.hmrc" %% "microservice-bootstrap-25" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-authorisation-25" % playAuthVersion,
    "uk.gov.hmrc" %% "play-health-25" % playHealthVersion,
    "uk.gov.hmrc" %% "play-url-binders-25" % playUrlBindersVersion,
    "uk.gov.hmrc" %% "play-config-25" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger-25" % playJsonLoggerVersion,


//    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
//    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
//    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
//    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion,
//    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
//    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
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
//        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "uk.gov.hmrc" %% "hmrctest-25" % hmrcTestVersion % scope,

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


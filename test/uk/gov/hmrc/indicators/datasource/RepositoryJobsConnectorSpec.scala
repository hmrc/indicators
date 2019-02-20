/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.indicators.datasource

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws.WSHttp

class RepositoryJobsConnectorSpec
    extends WordSpec
    with Matchers
    with WireMockSpec
    with ScalaFutures
    with DefaultPatienceConfig
    with GuiceOneAppPerSuite {

  implicit val hc = HeaderCarrier()
  private val httpClient = new HttpClient with WSHttp {
    override val hooks: Seq[HttpHook] = Seq.empty
  }

  val configuration = Configuration(
    "microservice.services.repository-jobs.port" -> Port,
    "microservice.services.repository-jobs.host" -> Host)
  val repositoryJobsConnector = new RepositoryJobsConnector(httpClient, configuration, Environment.simple())

  "Repository jobs connector" should {

    "Return a list of builds for a given repository" in {

      stubFor(
        get(urlEqualTo("/api/builds/test-repo"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(s"""
                 |[
                 |    {
                 |        "repositoryName":"test-repo",
                 |        "jobName":"repository-abcd",
                 |        "jobUrl":"job.url",
                 |        "buildNumber":1,
                 |        "result":"SUCCESS",
                 |        "timestamp":1486571562000,
                 |        "duration":218869,
                 |        "buildUrl":"build.url",
                 |        "builtOn":"built-on"
                 |    },
                 |    {
                 |        "repositoryName":"test-repo",
                 |        "jobName":"repository-abcd",
                 |        "jobUrl":"job.url",
                 |        "buildNumber":5,
                 |        "result":"SUCCESS",
                 |        "timestamp":1486571562000,
                 |        "duration":218869,
                 |        "buildUrl":"build.url",
                 |        "builtOn":"built-on"
                 |    }
                 |]
               """.stripMargin))
      )

      val results = repositoryJobsConnector.getBuildsForRepository("test-repo").futureValue

      val build1 = Build(
        repositoryName = "test-repo",
        jobName        = "repository-abcd",
        jobUrl         = "job.url",
        buildNumber    = 1,
        result         = Some("SUCCESS"),
        timestamp      = 1486571562000l,
        duration       = 218869,
        buildUrl       = "build.url",
        builtOn        = "built-on"
      )
      val build2 = Build(
        repositoryName = "test-repo",
        jobName        = "repository-abcd",
        jobUrl         = "job.url",
        buildNumber    = 5,
        result         = Some("SUCCESS"),
        timestamp      = 1486571562000l,
        duration       = 218869,
        buildUrl       = "build.url",
        builtOn        = "built-on"
      )

      results.length shouldBe 2
      results.head   shouldBe build1
      results.last   shouldBe build2

    }
  }

}

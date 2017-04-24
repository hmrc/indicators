/*
 * Copyright 2017 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers.running
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}

class RepositoryJobsConnectorSpec extends WordSpec with Matchers with WireMockSpec with ScalaFutures with DefaultPatienceConfig with OneAppPerSuite {

  "Repository jobs connector" should {

    "Return a list of builds for a given repository" in {

      running(app) {
        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/builds/test-repo",
          willRespondWith = (200,
            Some(
              s"""
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
               """.stripMargin
            )))

        val connector = new RepositoryJobsConnector(endpointMockUrl)
        val results = connector.getBuildsForRepository("test-repo").futureValue

        val build1 = Build("test-repo", "repository-abcd", "job.url", 1, "SUCCESS", 1486571562000l, 218869, "build.url", "built-on")
        val build2 = Build("test-repo", "repository-abcd", "job.url", 5, "SUCCESS", 1486571562000l, 218869, "build.url", "built-on")

        results.length shouldBe 2
        results.head shouldBe build1
        results.last shouldBe build2

      }
    }

  }

}

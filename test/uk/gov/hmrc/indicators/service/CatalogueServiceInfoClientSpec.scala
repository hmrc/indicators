/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.indicators.service

import java.time.ZonedDateTime

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}

class CatalogueServiceInfoClientSpec extends WordSpec with Matchers with WireMockSpec with ScalaFutures with DefaultPatienceConfig {

  val catalogueClient = new CatalogueServiceClient(endpointMockUrl)

  "getService" should {
    "return service with gitEnterPriseOrg and gitOpenOrg" in {
      running(FakeApplication()) {

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/services",
          willRespondWith = (200,
            Some(
              """|{"data":[{
                |			"name": "serviceName",
                |			"githubUrls": [
                |				{
                |					"name": "github",
                |					"url": "https://someGitHubHost/org1/serviceName"
                |				},
                |    {
                |					"name": "github-open",
                |					"url": "https://someOtherGitHubHost/org2/serviceName"
                |				}
                |			]
                |		}]}
              """.stripMargin

            )
            )
        )
        catalogueClient.getServiceRepoInfo("serviceName").futureValue shouldBe List(
          ServiceRepositoryInfo("serviceName", "org1", RepoType.Enterprise),
          ServiceRepositoryInfo("serviceName", "org2", RepoType.Open)
        )

      }

    }
    "return None if service is not found" in {
      running(FakeApplication()) {

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/services",
          willRespondWith = (400, None)
        )
        catalogueClient.getServiceRepoInfo("serviceName").futureValue shouldBe List()
      }
    }
  }
}



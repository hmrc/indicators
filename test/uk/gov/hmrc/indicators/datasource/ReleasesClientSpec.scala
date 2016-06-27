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

package uk.gov.hmrc.indicators.datasource

import java.time.{LocalDate, LocalDateTime, ZoneOffset, ZonedDateTime}

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.{Configs, DefaultPatienceConfig, IndicatorsConfigProvider, WireMockSpec}

class ReleasesClientSpec extends WordSpec with Matchers with WireMockSpec with ScalaFutures with DefaultPatienceConfig{

  val releasesClient = new ReleasesClient(endpointMockUrl)

  "ReleasesClient.getForService" should {

    "get all releases from the releases api and return ones for the given serviceName" in {
      running(FakeApplication()) {
        val serviceName = "test-service"

        val `release 8.3.0 production date` = LocalDateTime.now().minusMonths(2).toEpochSecond(ZoneOffset.UTC)
        val `release 11.0.0 creation date` = LocalDateTime.now().minusDays(5).toEpochSecond(ZoneOffset.UTC)
        val `release 11.0.0 production date` = LocalDateTime.now().minusDays(4).toEpochSecond(ZoneOffset.UTC)

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/$serviceName",
          willRespondWith = (200,
            Some(
              s"""
              |[
              |    {
              |        "name": "$serviceName",
              |        "version": "11.0.0",
              |        "creationDate": ${`release 11.0.0 creation date`},
              |        "productionDate": ${`release 11.0.0 production date`}
              |    },
              |    {
              |        "name": "$serviceName",
              |        "version": "8.3.0",
              |        "productionDate": ${`release 8.3.0 production date`}
              |    }
              |]
            """.stripMargin
          )))

        val results = releasesClient.getForService(serviceName).futureValue
        results.size shouldBe 2

        results.head shouldBe Release(
          serviceName,
          "11.0.0",
          Some(LocalDateTime.ofEpochSecond(`release 11.0.0 creation date`, 0, ZoneOffset.UTC)),
          LocalDateTime.ofEpochSecond(`release 11.0.0 production date`, 0, ZoneOffset.UTC))

        results.last shouldBe Release(
          serviceName,
          "8.3.0",
          None,
          LocalDateTime.ofEpochSecond(`release 8.3.0 production date`, 0, ZoneOffset.UTC))
      }
    }
  }
}

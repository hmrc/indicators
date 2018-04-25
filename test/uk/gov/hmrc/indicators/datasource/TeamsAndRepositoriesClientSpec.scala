/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.{LocalDateTime, ZoneOffset}

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}

class TeamsAndRepositoriesClientSpec
    extends WordSpec
    with Matchers
    with WireMockSpec
    with ScalaFutures
    with DefaultPatienceConfig
    with OneAppPerSuite {

  val teamsAndRepositoriesClient = new TeamsAndRepositoriesClient(endpointMockUrl)

  "getServicesForTeam" should {
    "return all services of a team" in {

      running(app) {
        val teamName = "test-team"

        givenRequestExpects(
          method = GET,
          url    = s"$endpointMockUrl/teams/test-team",
          willRespondWith = (
            200,
            Some(
              s"""
                 |{
                 |   "Service" : [
                 |        "Service1",
                 |        "Service2",
                 |        "Service3"
                 |    ],
                 |    "Library" : [
                 |         "Library1",
                 |         "Library2",
                 |         "Library3"
                 |     ],
                 |  "Other" : [
                 |     "Other1",
                 |     "Other2",
                 |     "Other3"
                 | ]
                 |}
            """.stripMargin
            ))
        )

        val results: List[String] = teamsAndRepositoriesClient.getServicesForTeam(teamName).futureValue
        results shouldBe List("Service1", "Service2", "Service3")

      }

    }
  }

}

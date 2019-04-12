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

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global

class TeamsAndRepositoriesConnectorSpec
    extends WordSpec
    with Matchers
    with WireMockSpec
    with ScalaFutures
    with DefaultPatienceConfig
    with GuiceOneAppPerSuite {

  implicit val hc = HeaderCarrier()
  private val httpClient = new HttpClient with WSHttp {
    override val hooks: Seq[HttpHook]                    = Seq.empty
    override protected def actorSystem: ActorSystem      = ActorSystem("test-actor-system")
    override protected def configuration: Option[Config] = None
  }

  val configuration = Configuration(
    "microservice.services.teams-and-repositories.port" -> Port,
    "microservice.services.teams-and-repositories.host" -> Host)
  val teamsAndRepositoriesConnector = new TeamsAndRepositoriesConnector(httpClient, configuration, Environment.simple())

  "getServicesForTeam" should {
    "return all services of a team" in {

      val teamName = "test-team"

      stubFor(
        get(urlEqualTo("/api/teams/test-team"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
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

      val results: List[String] = teamsAndRepositoriesConnector.getServicesForTeam(teamName).futureValue
      results shouldBe List("Service1", "Service2", "Service3")

    }

  }

}

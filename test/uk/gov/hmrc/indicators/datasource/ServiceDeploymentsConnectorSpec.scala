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

import java.time.{LocalDateTime, ZoneOffset}

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock._
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

class ServiceDeploymentsConnectorSpec
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
    "microservice.services.service-deployments.port" -> Port,
    "microservice.services.service-deployments.host" -> Host)
  val serviceDeploymentsConnector = new ServiceDeploymentsConnector(httpClient, configuration, Environment.simple())

  "DeploymentsClient.getForService" should {

    "get all deployments from the deployments api and return ones for the given serviceName" in {
      val serviceName = "test-service"

      val deployment_8_3_0_production_date  = LocalDateTime.now().minusMonths(2).toEpochSecond(ZoneOffset.UTC)
      val deployment_11_0_0_creation_date   = LocalDateTime.now().minusDays(5).toEpochSecond(ZoneOffset.UTC)
      val deployment_11_0_0_production_date = LocalDateTime.now().minusDays(4).toEpochSecond(ZoneOffset.UTC)
      stubFor(
        get(urlEqualTo("/api/deployments/test-service"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(s"""
                             |[
                             |    {
                             |        "name": "$serviceName",
                             |        "version": "11.0.0",
                             |        "creationDate": $deployment_11_0_0_creation_date,
                             |        "productionDate": $deployment_11_0_0_production_date,
                             |        "leadTime": 5
                             |    },
                             |    {
                             |        "name": "$serviceName",
                             |        "version": "8.3.0",
                             |        "productionDate": $deployment_8_3_0_production_date,
                             |        "leadTime": 20,
                             |        "interval": 10
                             |    }
                             |]
            """.stripMargin)))

      val results = serviceDeploymentsConnector.getForService(serviceName).futureValue
      results.size shouldBe 2

      results.head shouldBe Deployment(
        name           = serviceName,
        version        = "11.0.0",
        productionDate = LocalDateTime.ofEpochSecond(deployment_11_0_0_production_date, 0, ZoneOffset.UTC),
        leadTime       = Some(5))

      results.last shouldBe Deployment(
        name           = serviceName,
        version        = "8.3.0",
        productionDate = LocalDateTime.ofEpochSecond(`deployment_8_3_0_production_date`, 0, ZoneOffset.UTC),
        leadTime       = Some(20),
        interval       = Some(10)
      )

    }

    "get all deployments should not fail if 404" in {

      val serviceName = "test-service"
      stubFor(
        get(urlEqualTo("/api/deployments/test-service"))
          .willReturn(aResponse().withStatus(404)))

      val results = serviceDeploymentsConnector.getForService(serviceName).futureValue
      results.size shouldBe 0
    }
  }
}

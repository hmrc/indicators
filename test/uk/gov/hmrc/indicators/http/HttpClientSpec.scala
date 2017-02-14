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

package uk.gov.hmrc.indicators.http

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.indicators.{WireMockSpec, DefaultPatienceConfig}

import play.api.test._
import play.api.test.Helpers._


class HttpClientSpec extends WireMockSpec with ScalaFutures with Matchers with DefaultPatienceConfig {

  case class Response(success: Boolean)

  object Response {
    implicit val formats = Json.format[Response]
  }


  "HttpClientSpec.get" should {

    "report success with string body" in {
      running(FakeApplication()) {

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/resource/1",
          willRespondWith = (200, Some( """{"success" : true}""")),
          headers = List(("content-type", "application/json"))
        )

        printMappings()

        HttpClient.get[Response](s"$endpointMockUrl/resource/1", List(("content-type", "application/json"))).futureValue should be(Response(success = true))
      }

    }

    "report exception if correct http status is not returned" in {
      running(FakeApplication()) {

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/resource/1",
          willRespondWith = (500, None)
        )

        a[RuntimeException] should be thrownBy HttpClient.get[Response](s"$endpointMockUrl/resource/1").futureValue
      }
    }


  }
}

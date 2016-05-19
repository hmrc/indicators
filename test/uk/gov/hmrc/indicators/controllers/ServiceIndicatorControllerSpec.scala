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

package uk.gov.hmrc.indicators.controllers

import java.time.YearMonth

import akka.util.Timeout
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.iteratee.Iteratee
import play.api.mvc.{Result, Results, SimpleResult}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.TestImplicits._
import uk.gov.hmrc.indicators.service.{IndicatorsService, LeadTimeResult}

import scala.Predef
import scala.concurrent.{Await, Future}

class ServiceIndicatorControllerSpec extends PlaySpec with MockitoSugar {

  private val mockIndicatorsService = mock[IndicatorsService]

  val controller = new ServiceIndicatorController {
    override val indicatorsService: IndicatorsService = mockIndicatorsService
  }


  "ServiceIndicatorController" should {
    "have correct UrlMapping for lead time json" in {
      uk.gov.hmrc.indicators.controllers.routes.ServiceIndicatorController.frequentProdRelease("serviceName").url mustBe "/indicators/api/service/serviceName/fpr"
    }
  }


  "ServiceIndicatorController.frequentProdRelease" should {

    "return deployment lead time in json by default" in {
      when(mockIndicatorsService.getProductionDeploymentLeadTime("serviceName")).thenReturn(Future.successful(List()))

      val result = controller.frequentProdRelease("serviceName")(FakeRequest())

      contentAsJson(result) mustBe """[]""".toJson

      header("content-type", result).get mustBe "application/json"

    }

    "return deployment lead time in json when application/json is requested" in {
      when(mockIndicatorsService.getProductionDeploymentLeadTime("serviceName")).thenReturn(Future.successful(List()))

      val result = controller.frequentProdRelease("serviceName")(FakeRequest().withHeaders("Accepts" -> "application/json"))

      contentAsJson(result) mustBe """[]""".toJson

      header("content-type", result).get mustBe "application/json"

    }

    "return deployment lead time in csv when accept header is test/csv" in {

      when(mockIndicatorsService.getProductionDeploymentLeadTime("serviceName")).thenReturn(Future.successful(List()))

      val result = controller.frequentProdRelease("serviceName")(FakeRequest().withHeaders("Accept" -> "text/csv"))

      new String(contentAsBytes(result)) mustBe
        """|Name,
          |serviceName,""".stripMargin

      header("content-type", result).get mustBe "text/csv"

    }

    "returns unsupported statu when accepts header not recognized" in {

      when(mockIndicatorsService.getProductionDeploymentLeadTime("serviceName")).thenReturn(Future.successful(List()))

      val result = controller.frequentProdRelease("serviceName")(FakeRequest().withHeaders("Accept" -> "application/pdf"))

      status(result) mustBe 406

    }


    "return deployment lead times for a given service in json format" in {

      when(mockIndicatorsService.getProductionDeploymentLeadTime("serviceName")).thenReturn(Future.successful(
        List(
          LeadTimeResult(YearMonth.of(2016, 4), Some(5)),
          LeadTimeResult(YearMonth.of(2016, 5), Some(6))
        ))
      )

      val result = controller.frequentProdRelease("serviceName")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "median" : 5},
          |{"period" : "2016-05", "median" : 6}
          |]""".stripMargin.toJson

      header("content-type", result).get mustBe "application/json"
    }




  }

  /**
   * http://stackoverflow.com/questions/28461877/is-there-a-bug-in-play2-testing-with-fakerequests-and-chunked-responses-enumera
   */
  def contentAsBytes(of: Future[Result])(implicit timeout: Timeout): Array[Byte] = {
    val r = Await.result(of, timeout.duration)
    val e = r.header.headers.get(TRANSFER_ENCODING) match {
      case Some("chunked") => {
        r.body &> Results.dechunk
      }
      case _ => r.body
    }
    Await.result(e |>>> Iteratee.consume[Array[Byte]](), timeout.duration)
  }


}

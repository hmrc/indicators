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

package uk.gov.hmrc.indicators.controllers

import java.time.{LocalDate, YearMonth}

import org.mockito.Matchers.{any, eq => is}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.indicators.TestImplicits._
import uk.gov.hmrc.indicators.service._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future

class ServiceIndicatorControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private val httpClient            = mock[HttpClient]
  private val mockIndicatorsService = mock[IndicatorsService]
  private implicit val hc           = HeaderCarrier()

  val controller = new ServiceIndicatorController(httpClient, mockIndicatorsService)

  "ServiceIndicatorController.serviceDeploymentMetrics" should {

    "return Frequent deployment metric for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getServiceDeploymentMetrics(is("serviceName"), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(List(
          DeploymentsMetricResult(
            YearMonth.of(2016, 4),
            from = date,
            to   = date,
            Some(Throughput(Some(MeasureResult(5)), Some(MeasureResult(4)))),
            None),
          DeploymentsMetricResult(
            YearMonth.of(2016, 5),
            from = date,
            to   = date,
            Some(Throughput(Some(MeasureResult(6)), None)),
            None)
        ))))

      val result = controller.serviceDeploymentMetrics("serviceName")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 5}, "interval" : {"median" : 4}}},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 6}}}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }

    "return NotFound if None lead times returned" in {
      when(mockIndicatorsService.getServiceDeploymentMetrics(is("serviceName"), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = controller.serviceDeploymentMetrics("serviceName")(FakeRequest())

      status(result) mustBe NOT_FOUND

    }

  }

  "ServiceIndicatorController.teamDeploymentMetrics" should {

    "return Frequent deployment metric for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getTeamDeploymentMetrics(is("teamName"), any())(any()))
        .thenReturn(Future.successful(Some(List(
          DeploymentsMetricResult(
            YearMonth.of(2016, 4),
            from = date,
            to   = date,
            Some(Throughput(Some(MeasureResult(5)), Some(MeasureResult(4)))),
            None),
          DeploymentsMetricResult(
            YearMonth.of(2016, 5),
            from = date,
            to   = date,
            Some(Throughput(Some(MeasureResult(6)), None)),
            None)
        ))))

      val result = controller.teamDeploymentMetrics("teamName")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 5}, "interval" : {"median" : 4}}},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 6}}}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }

    "return NotFound if None lead times returned" in {
      when(mockIndicatorsService.getTeamDeploymentMetrics(is("teamName"), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = controller.teamDeploymentMetrics("teamName")(FakeRequest())

      status(result) mustBe NOT_FOUND

    }

  }

  "ServiceIndicatorController.jobMetrics" should {

    "return job metrics for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getJobMetrics(is("test-repo"), any())(any[HeaderCarrier])).thenReturn(
        Future.successful(Some(List(
          JobMetric(YearMonth.of(2016, 4), from = date, to = date, Some(MeasureResult(4)), Some(0.01)),
          JobMetric(YearMonth.of(2016, 5), from = date, to = date, Some(MeasureResult(5)), Some(0.02))
        ))))

      val result = controller.jobMetrics("test-repo")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "duration": {"median" : 4}, "successRate": 0.01},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "duration": {"median" : 5}, "successRate": 0.02}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }

    "return NotFound if None builds returned" in {
      when(mockIndicatorsService.getJobMetrics(is("test-repo"), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = controller.jobMetrics("test-repo")(FakeRequest())

      status(result) mustBe NOT_FOUND

    }

  }

}

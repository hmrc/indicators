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

package uk.gov.hmrc.indicators.controllers

import java.time.{LocalDate, YearMonth}

import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.TestImplicits._
import uk.gov.hmrc.indicators.service._

import scala.concurrent.Future

class ServiceIndicatorControllerSpec extends PlaySpec with MockitoSugar {

  private val mockIndicatorsService = mock[IndicatorsService]

  val controller = new ServiceIndicatorController {
    override val indicatorsService: IndicatorsService = mockIndicatorsService
  }

  "ServiceIndicatorController.serviceDeploymentMetrics" should {


    "return Frequent deployment metric for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getServiceDeploymentMetrics("serviceName")).thenReturn(Future.successful(
        Some(List(
          DeploymentsMetricResult(YearMonth.of(2016, 4), from = date, to = date, Some(Throughput(Some(MeasureResult(5)), Some(MeasureResult(4)))), None),
          DeploymentsMetricResult(YearMonth.of(2016, 5), from = date, to = date, Some(Throughput(Some(MeasureResult(6)), None)), None)
        )))
      )

      val result = controller.serviceDeploymentMetrics("serviceName")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 5}, "interval" : {"median" : 4}}},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 6}}}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }


    "return NotFound if None lead times returned" in {
      when(mockIndicatorsService.getServiceDeploymentMetrics("serviceName")).thenReturn(Future.successful(None))

      val result = controller.serviceDeploymentMetrics("serviceName")(FakeRequest())

      status(result) mustBe NOT_FOUND


    }

  }

  "ServiceIndicatorController.teamDeploymentMetrics" should {


    "return Frequent deployment metric for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getTeamDeploymentMetrics("teamName")).thenReturn(Future.successful(
        Some(List(
          DeploymentsMetricResult(YearMonth.of(2016, 4), from = date, to = date, Some(Throughput(Some(MeasureResult(5)), Some(MeasureResult(4)))), None),
          DeploymentsMetricResult(YearMonth.of(2016, 5), from = date, to = date, Some(Throughput(Some(MeasureResult(6)), None)), None)
        )))
      )

      val result = controller.teamDeploymentMetrics("teamName")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 5}, "interval" : {"median" : 4}}},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "throughput":{"leadTime" : {"median" : 6}}}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }


    "return NotFound if None lead times returned" in {
      when(mockIndicatorsService.getTeamDeploymentMetrics("teamName")).thenReturn(Future.successful(None))

      val result = controller.teamDeploymentMetrics("teamName")(FakeRequest())

      status(result) mustBe NOT_FOUND


    }

  }

  "ServiceIndicatorController.jobExecutionTimeMetrics" should {

    "return job execution time metrics for a given service in json format" in {

      val date = LocalDate.of(2016, 9, 13)

      when(mockIndicatorsService.getJobExecutionTimeMetrics("test-repo")).thenReturn(Future.successful(
        Some(List(
          JobExecutionTimeMetricResult(YearMonth.of(2016, 4), from = date, to = date, Some(MeasureResult(4))),
          JobExecutionTimeMetricResult(YearMonth.of(2016, 5), from = date, to = date, Some(MeasureResult(5)))
        )))
      )

      val result = controller.jobExecutionTimeMetrics("test-repo")(FakeRequest())

      contentAsJson(result) mustBe
        """[
          |{"period" : "2016-04", "from" : "2016-09-13", "to" : "2016-09-13", "duration": {"median" : 4}},
          |{"period" : "2016-05", "from" : "2016-09-13", "to" : "2016-09-13", "duration": {"median" : 5}}
          |]""".stripMargin.toJson

      contentType(result).value mustBe "application/json"
    }


    "return NotFound if None builds returned" in {
      when(mockIndicatorsService.getJobExecutionTimeMetrics("test-repo")).thenReturn(Future.successful(None))

      val result = controller.jobExecutionTimeMetrics("test-repo")(FakeRequest())

      status(result) mustBe NOT_FOUND

    }

  }


}

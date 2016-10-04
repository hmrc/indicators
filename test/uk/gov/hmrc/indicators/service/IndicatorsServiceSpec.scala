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

import java.time.{LocalDate, LocalDateTime, YearMonth}

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.datasource._
import uk.gov.hmrc.indicators.{DateHelper, DefaultPatienceConfig}
import uk.gov.hmrc.play.http.HttpException

import scala.concurrent.Future


class IndicatorsServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig {

  val releasesClient = mock[ReleasesDataSource]

  val Feb_1st = LocalDateTime.of(2000, 2, 1, 0, 0, 0)
  val Feb_4th = LocalDateTime.of(2000, 2, 4, 0, 0, 0)
  val Feb_5th = LocalDateTime.of(2000, 2, 5, 0, 0, 0)
  val Feb_6th = LocalDateTime.of(2000, 2, 6, 0, 0, 0)

  val Feb_18th = LocalDateTime.of(2000, 2, 18, 0, 0, 0)
  val now = DateHelper.clockFrom(Feb_18th)

  val indicatorsService = new IndicatorsService(releasesClient, now)

  val serviceName = "test-service"

  def release(name: String, creationDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None, version: String = "version"): Release = {
    Release(name, version, creationDate, leadTime, interval)
  }

  "IndicatorService getDeploymentMetrics" should {

    "calculates DeploymentsMetricResult" in {
      val releases = List(
        release(serviceName, Feb_4th, leadTime = Some(3)),
        release(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))

      val expectedFrom = LocalDate.of(1999, 12, 1) // 3 months before the required period

      indicatorsService.getDeploymentMetrics("test-service", 1).futureValue.get shouldBe
        List(
          DeploymentsMetricResult(YearMonth.from(Feb_1st), from = expectedFrom, to = Feb_18th.toLocalDate, throughput = Some(Throughput(Some(MeasureResult(2)), Some(MeasureResult(2)))), stability = Some(Stability(Some(1.0), Some(MeasureResult(2))))))
    }

    "return only the release interval if no tag creation dates are available" in {
      val releases = List(
        release(serviceName, Feb_4th),
        release(serviceName, Feb_6th, interval = Some(2)))

      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))

      val expectedFrom = LocalDate.of(1999, 12, 1) // 3 months before the required period

      indicatorsService.getDeploymentMetrics("test-service", 1).futureValue.get shouldBe
        List(DeploymentsMetricResult(period = YearMonth.from(Feb_1st), from = expectedFrom, to = Feb_18th.toLocalDate, throughput = Some(Throughput(None, Some(MeasureResult(2)))), stability = Some(Stability(Some(1.0), None))))
    }

    "returns None if the service is not found" in {
      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.failed(new RuntimeException("404")))

      indicatorsService.getDeploymentMetrics("test-service", 1).futureValue shouldBe None
    }
  }
}

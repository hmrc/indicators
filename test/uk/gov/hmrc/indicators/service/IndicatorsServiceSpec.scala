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

import java.time.{LocalDateTime, YearMonth}

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
  val fixedClock = DateHelper.clockFrom(Feb_1st)

  val indicatorsService = new IndicatorsService(releasesClient, fixedClock)

  "IndicatorService getFrequentReleaseMetric" should {
    val serviceName = "test-service"

    "calculates FrequentReleaseMetricResult" in {
      val releases = List(
        Release(serviceName, "1.0.0", Some(Feb_1st), Feb_4th),
        Release(serviceName, "2.0.0", Some(Feb_5th), Feb_6th))

      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))

      indicatorsService.getFrequentReleaseMetric("test-service", 1).futureValue.get shouldBe
        List(FrequentReleaseMetricResult(YearMonth.from(Feb_1st), Some(MeasureResult(2)), Some(MeasureResult(2))))
    }

    "return only the release interval if no tag creation dates are available" in {
      val releases = List(
        Release(serviceName, "1.0.0", None, Feb_4th),
        Release(serviceName, "2.0.0", None, Feb_6th))

      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))

      indicatorsService.getFrequentReleaseMetric("test-service", 1).futureValue.get shouldBe
        List(FrequentReleaseMetricResult(YearMonth.from(Feb_1st), None, Some(MeasureResult(2))))
    }

    "returns None if the service is not found" in {
      Mockito.when(releasesClient.getForService("test-service")).thenReturn(Future.failed(new RuntimeException("404")))

      indicatorsService.getFrequentReleaseMetric("test-service", 1).futureValue shouldBe None
    }
  }
}

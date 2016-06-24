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

import java.time.{YearMonth, LocalDate, LocalDateTime}

import org.scalatest.{Matchers, WordSpec, FunSuite}

class FrequentReleaseMetricResultSpec extends WordSpec with Matchers {


  val Jan_2016 = YearMonth.of(2016, 1)
  val Feb_2016 = YearMonth.of(2016, 2)
  val March_2016 = YearMonth.of(2016, 3)
  val April_2016 = YearMonth.of(2016, 4)
  val May_2016 = YearMonth.of(2016, 5)
  val June_2016 = YearMonth.of(2016, 6)

  "FrequentReleaseMetricResult.of" should {
    "be able to construct FrequentReleaseMetricResult given List of ReleaseLeadTimeResult and ReleaseIntervalResult" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(Jan_2016, Some(1)),
        ReleaseLeadTimeResult(Feb_2016, Some(2)),
        ReleaseLeadTimeResult(March_2016, Some(3)),
        ReleaseLeadTimeResult(April_2016, Some(4))
      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(Jan_2016, Some(5)),
        ReleaseIntervalResult(Feb_2016, Some(6)),
        ReleaseIntervalResult(March_2016, Some(7)),
        ReleaseIntervalResult(April_2016, Some(8))
      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, Some(1), Some(5)),
        FrequentReleaseMetricResult(Feb_2016, Some(2), Some(6)),
        FrequentReleaseMetricResult(March_2016, Some(3), Some(7)),
        FrequentReleaseMetricResult(April_2016, Some(4), Some(8))
      )
    }

    "be able to construct FrequentReleaseMetricResult given List of ReleaseLeadTimeResult and ReleaseIntervalResult with missing month in either metrics" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(Jan_2016, None),
        ReleaseLeadTimeResult(Feb_2016, Some(2)),
        ReleaseLeadTimeResult(March_2016, Some(3)),
        ReleaseLeadTimeResult(April_2016, Some(4))
      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(Jan_2016, Some(5)),
        ReleaseIntervalResult(Feb_2016, Some(6)),
        ReleaseIntervalResult(March_2016, None),
        ReleaseIntervalResult(April_2016, Some(8)),
        ReleaseIntervalResult(May_2016, Some(9))
      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, None, Some(5)),
        FrequentReleaseMetricResult(Feb_2016, Some(2), Some(6)),
        FrequentReleaseMetricResult(March_2016, Some(3), None),
        FrequentReleaseMetricResult(April_2016, Some(4), Some(8)),
        FrequentReleaseMetricResult(May_2016, None, Some(9))
      )
    }

  }

}

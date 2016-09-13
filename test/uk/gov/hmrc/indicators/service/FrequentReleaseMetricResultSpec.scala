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

import java.time.{LocalDate, YearMonth}

import org.scalatest.{Matchers, WordSpec}

class FrequentReleaseMetricResultSpec extends WordSpec with Matchers {


  val Nov_2015 = YearMonth.of(2015, 11)
  val Dec_2015 = YearMonth.of(2015, 12)
  val Jan_2016 = YearMonth.of(2016, 1)
  val Feb_2016 = YearMonth.of(2016, 2)
  val March_2016 = YearMonth.of(2016, 3)
  val April_2016 = YearMonth.of(2016, 4)
  val May_2016 = YearMonth.of(2016, 5)
  val June_2016 = YearMonth.of(2016, 6)

  "FrequentReleaseMetricResult.of" should {
    "be able to construct FrequentReleaseMetricResult given List of ReleaseLeadTimeResult and ReleaseIntervalResult" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(period = Jan_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(1)),
        ReleaseLeadTimeResult(period = Feb_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(2)),
        ReleaseLeadTimeResult(period = March_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(3)),
        ReleaseLeadTimeResult(period = April_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(4))
      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(period = Jan_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(5)),
        ReleaseIntervalResult(period = Feb_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(6)),
        ReleaseIntervalResult(period = March_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(7)),
        ReleaseIntervalResult(period = April_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(8))
      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(1)), Some(MeasureResult(5))),
        FrequentReleaseMetricResult(Feb_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(2)), Some(MeasureResult(6))),
        FrequentReleaseMetricResult(March_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(3)), Some(MeasureResult(7))),
        FrequentReleaseMetricResult(April_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(4)), Some(MeasureResult(8)))
      )
    }


    "be able to construct FrequentReleaseMetricResult with correct from and to date given List of ReleaseLeadTimeResult and ReleaseIntervalResult" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(period = Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), median = Some(1)),
        ReleaseLeadTimeResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(2))

      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(period = Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), median = Some(5)),
        ReleaseIntervalResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(6))

      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), Some(MeasureResult(1)), Some(MeasureResult(5))),
        FrequentReleaseMetricResult(Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), Some(MeasureResult(2)), Some(MeasureResult(6)))
      )
    }


    "be able to construct FrequentReleaseMetricResult with correct from and to date given List of ReleaseLeadTimeResult and ReleaseIntervalResult when ReleaseIntervalResult is missing for a period" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(period = Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), median = Some(1)),
        ReleaseLeadTimeResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(2))

      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(6))

      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), Some(MeasureResult(1)), None),
        FrequentReleaseMetricResult(Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), Some(MeasureResult(2)), Some(MeasureResult(6)))
      )
    }

    "be able to construct FrequentReleaseMetricResult with correct from and to date given List of ReleaseLeadTimeResult and ReleaseIntervalResult when ReleaseLeadTimeResult is missing for a period" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(2))

      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(period = Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), median = Some(5)),
        ReleaseIntervalResult(period = Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), median = Some(6))

      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, from = Nov_2015.atDay(1), to = Jan_2016.atEndOfMonth(), None, Some(MeasureResult(5))),
        FrequentReleaseMetricResult(Feb_2016, from = Dec_2015.atDay(1), to = Feb_2016.atDay(25), Some(MeasureResult(2)), Some(MeasureResult(6)))
      )
    }



    "be able to construct FrequentReleaseMetricResult given List of ReleaseLeadTimeResult and ReleaseIntervalResult with missing month in either metrics" in {

      val releaseLeadTimeResults = List(
        ReleaseLeadTimeResult(period = Jan_2016, from = LocalDate.now(), to = LocalDate.now(), median = None),
        ReleaseLeadTimeResult(period = Feb_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(2)),
        ReleaseLeadTimeResult(period = March_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(3)),
        ReleaseLeadTimeResult(period = April_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(4))
      )

      val releaseIntervalResults = List(
        ReleaseIntervalResult(period = Jan_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(5)),
        ReleaseIntervalResult(period = Feb_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(6)),
        ReleaseIntervalResult(period = March_2016, from = LocalDate.now(), to = LocalDate.now(), median = None),
        ReleaseIntervalResult(period = April_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(8)),
        ReleaseIntervalResult(period = May_2016, from = LocalDate.now(), to = LocalDate.now(), median = Some(9))
      )


      FrequentReleaseMetricResult.from(releaseLeadTimeResults, releaseIntervalResults) shouldBe Seq(
        FrequentReleaseMetricResult(Jan_2016, from = LocalDate.now(), to = LocalDate.now(), None, Some(MeasureResult(5))),
        FrequentReleaseMetricResult(Feb_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(2)), Some(MeasureResult(6))),
        FrequentReleaseMetricResult(March_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(3)), None),
        FrequentReleaseMetricResult(April_2016, from = LocalDate.now(), to = LocalDate.now(), Some(MeasureResult(4)), Some(MeasureResult(8))),
        FrequentReleaseMetricResult(May_2016, from = LocalDate.now(), to = LocalDate.now(), None, Some(MeasureResult(9)))
      )
    }


  }

}

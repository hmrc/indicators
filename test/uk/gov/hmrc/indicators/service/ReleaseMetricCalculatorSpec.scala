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

import java.time._

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.DateHelper._
import uk.gov.hmrc.indicators.datasource.Release


class ReleaseMetricCalculatorSpec extends WordSpec with Matchers {

  trait SetUp {
    private val midNight: LocalTime = LocalTime.of(0, 0)
    val Dec_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)

    val Jan_1st = LocalDateTime.of(LocalDate.of(2016, 1, 1), midNight)

    val Jan_10th = LocalDateTime.of(LocalDate.of(2016, 1, 10), midNight)


    val Feb_1st = LocalDateTime.of(LocalDate.of(2016, 2, 1), midNight)
    val Feb_4th = LocalDateTime.of(LocalDate.of(2016, 2, 4), midNight)
    val Feb_6th = LocalDateTime.of(LocalDate.of(2016, 2, 6), midNight)
    val Feb_9th = LocalDateTime.of(LocalDate.of(2016, 2, 9), midNight)
    val Feb_10th = LocalDateTime.of(LocalDate.of(2016, 2, 10), midNight)
    val Feb_12th = LocalDateTime.of(LocalDate.of(2016, 2, 12), midNight)
    val Feb_16th = LocalDateTime.of(LocalDate.of(2016, 2, 16), midNight)
    val Feb_18th = LocalDateTime.of(LocalDate.of(2016, 2, 18), midNight)
    val Feb_20st = LocalDateTime.of(LocalDate.of(2016, 2, 20), midNight)

    val Mar_1st = LocalDateTime.of(LocalDate.of(2016, 3, 1), midNight)
    val Mar_4th = LocalDateTime.of(LocalDate.of(2016, 3, 4), midNight)
    val March_10th = LocalDateTime.of(LocalDate.of(2016, 3, 10), midNight)
    val Mar_27th = LocalDateTime.of(LocalDate.of(2016, 3, 27), midNight)

    val Apr_1st = LocalDateTime.of(LocalDate.of(2016, 4, 1), midNight)
    val Apr_4th = LocalDateTime.of(LocalDate.of(2016, 4, 4), midNight)
    val Apr_10th = LocalDateTime.of(LocalDate.of(2016, 4, 10), midNight)
    val Apr_11th = LocalDateTime.of(LocalDate.of(2016, 4, 11), midNight)
    val May_1st = LocalDateTime.of(LocalDate.of(2016, 5, 1), midNight)
    val May_10th = LocalDateTime.of(LocalDate.of(2016, 5, 10), midNight)
    val May_11th = LocalDateTime.of(LocalDate.of(2016, 5, 11), midNight)
    val June_1st = LocalDateTime.of(LocalDate.of(2016, 6, 1), midNight)
    val June_5th = LocalDateTime.of(LocalDate.of(2016, 6, 5), midNight)

    implicit val clock = clockFrom(May_10th)
  }

  "ReleaseMetricCalculator.calculateLeadTime" should {

    "calculate the correct median lead time for one tag and release in the same month 3 days apart" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_4th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_4th, Some(3)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }

    "calculate the correct median lead time for two tags" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_16th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_4th, Some(3)),
        Release("test-service", "2.0.0", Some(Feb_10th), Feb_16th, Some(6)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "calculate the correct median lead time for releases that spans two months" in new SetUp {
      override implicit val clock: Clock = clockFrom(Apr_10th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Mar_1st), Mar_4th, Some(3)),
        Release("test-service", "2.0.0", Some(Apr_4th), Apr_10th, Some(6))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 2) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), Some(3)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), Some(5)))
    }

    "calculate the correct median lead time for 3 releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_6th, Some(5)),
        Release("test-service", "2.0.0", Some(Feb_6th), Feb_12th, Some(6)),
        Release("test-service", "3.0.0", Some(Feb_12th), Feb_20st, Some(8)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe
        List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(6)))
    }

    "calculate the correct median lead time for 3 releases with one missing tag date" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_6th, Some(5)),
        Release("test-service", "2.0.0", None, Feb_12th, None), // N/A
        Release("test-service", "3.0.0", Some(Feb_12th), Feb_20st, Some(8)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe
        List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(7)))
    }

    "calculate the correct median lead time for 4 releases (3, 6, 6, 2)" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_4th, Some(3)),
        Release("test-service", "2.0.0", Some(Feb_4th), Feb_10th, Some(6)),
        Release("test-service", "3.0.0", Some(Feb_10th), Feb_16th, Some(6)),
        Release("test-service", "4.0.0", Some(Feb_16th), Feb_18th, Some(2))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "calculate the rolling lead time for 7 months (3 months sliding window) when provided tags and releases are not ordered" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(
        Release("test-service", "7.0.0", Some(Mar_27th), Apr_1st, Some(5)),
        Release("test-service", "8.0.0", Some(Apr_4th), Apr_11th, Some(7)),
        Release("test-service", "1.0.0", Some(Feb_1st), Feb_4th, Some(3)),
        Release("test-service", "2.0.0", Some(Feb_4th), Feb_10th, Some(6)),
        Release("test-service", "3.0.0", Some(Feb_10th), Feb_16th, Some(6)),
        Release("test-service", "4.0.0", Some(Feb_16th), Feb_18th, Some(2)),
        Release("test-service", "5.0.0", Some(Feb_18th), Mar_1st, Some(12)),
        Release("test-service", "6.0.0", Some(Mar_4th), Mar_27th, Some(23)),
        Release("test-service", "9.0.0", Some(May_1st), May_11th, Some(10)),
        Release("test-service", "10.0.0", Some(June_1st), June_5th, Some(4))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 7) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Dec_1st_2015), None),
        ReleaseLeadTimeResult(YearMonth.from(Jan_1st), None),
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)),
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(May_1st), Some(10)),
        ReleaseLeadTimeResult(YearMonth.from(June_1st), Some(6)))
    }
  }

  "ReleaseMetricCalculator.calculateReleaseInterval" should {

    "calculate the correct median release interval for release in the same month 3 days apart" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_4th)

      val releases = List(Release("test-service", "1.0.0", None, Feb_4th))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe
        List(ReleaseIntervalResult(YearMonth.from(Feb_1st), None))
    }

    "calculate the correct median release interval for two releases" in new SetUp {

      val Jan_29th = LocalDateTime.of(LocalDate.of(2016, 1, 29), LocalTime.of(11, 16, 9))

      override implicit val clock: Clock = clockFrom(Jan_29th)

      val Jan_7th = LocalDateTime.of(LocalDate.of(2016, 1, 7), LocalTime.of(12, 16, 3))
      val Jan_28th = LocalDateTime.of(LocalDate.of(2016, 1, 28),LocalTime.of(11, 16, 3))


      val releases = List(
        Release("test-service", "1.0.0", None, Jan_7th),
        Release("test-service", "2.0.0", None, Jan_28th)
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Jan_1st), Some(21)))
    }

    "calculate the correct median release interval for releases that spans two months" in new SetUp {
      override implicit val clock: Clock = clockFrom(Apr_10th)

      val releases = List(
        Release("test-service", "1.0.0", None, Mar_4th),
        Release("test-service", "2.0.0", None, Apr_10th))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 2) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Mar_1st), None),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), Some(37)))
    }

    "calculate the correctmedian release interval for 3 releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        Release("test-service", "1.0.0", None, Feb_4th),
        Release("test-service", "2.0.0", None, Feb_10th),
        Release("test-service", "3.0.0", None, Feb_18th))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe
        List(ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(7)))
    }

    "calculate the correct median release interval for 4 releases (3, 6, 6, 2)" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      //6,6,2
      val releases = List(
        Release("test-service", "1.0.0", None, Feb_4th),
        Release("test-service", "2.0.0", None, Feb_10th),
        Release("test-service", "3.0.0", None, Feb_16th),
        Release("test-service", "4.0.0", None, Feb_18th))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(6)))
    }

    "calculate the median release interval for 7 months (3 months sliding window) when provided releases are not ordered" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(
        Release("test-service", "9.0.0", None, May_11th), //interval 30
        Release("test-service", "5.0.0", None, Mar_1st), //interval 12
        Release("test-service", "2.0.0", None, Feb_10th), // interval 6
        Release("test-service", "4.0.0", None, Feb_18th), // interval 2
        Release("test-service", "6.0.0", None, Mar_27th), //interval 26
        Release("test-service", "8.0.0", None, Apr_11th), //interval 10
        Release("test-service", "7.0.0", None, Apr_1st), //interval 5
        Release("test-service", "3.0.0", None, Feb_16th), // interval 6
        Release("test-service", "1.0.0", None, Feb_4th), // interval None
        Release("test-service", "10.0.0", None, June_5th) //interval 25
      )

      //dec None
      //jan None
      //feb 2,6,6 = 6
      //march 2,6,6,12,26 =6
      //april 2,6,6,12,26,5,10 => 2,5,6,6,10,12,26 = 6
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 7) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Dec_1st_2015), None),
        ReleaseIntervalResult(YearMonth.from(Jan_1st), None),
        ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Mar_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(May_1st), Some(12)),
        ReleaseIntervalResult(YearMonth.from(June_1st), Some(18)))
    }
  }
}

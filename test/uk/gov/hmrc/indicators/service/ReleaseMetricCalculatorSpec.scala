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
import uk.gov.hmrc.indicators.datasource.{Release, ServiceReleaseTag}


class ReleaseMetricCalculatorSpec extends WordSpec with Matchers {

  trait SetUp {
    private val midNight: LocalTime = LocalTime.of(0, 0)
    val Dec_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)

    val Jan_1st = LocalDateTime.of(LocalDate.of(2016, 1, 1), midNight)
    val Jan_10th = LocalDateTime.of(LocalDate.of(2016, 1, 10), midNight)

    val Feb_1st = LocalDateTime.of(LocalDate.of(2016, 2, 1), midNight)
    val Feb_4th = LocalDateTime.of(LocalDate.of(2016, 2, 4), midNight)
    val Feb_9th = LocalDateTime.of(LocalDate.of(2016, 2, 9), midNight)
    val Feb_10th = LocalDateTime.of(LocalDate.of(2016, 2, 10), midNight)
    val Feb_16th = LocalDateTime.of(LocalDate.of(2016, 2, 16), midNight)
    val Feb_18th = LocalDateTime.of(LocalDate.of(2016, 2, 18), midNight)

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

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st)
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }

    "calculate the correct median lead time for two tags" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_16th)

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st),
        ServiceReleaseTag("2.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_16th)
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "calculate the correct median lead time for releases that spans two months" in new SetUp {

      override implicit val clock: Clock = clockFrom(Apr_10th)

      val tags = List(
        ServiceReleaseTag("1.0.0", Mar_1st),
        ServiceReleaseTag("2.0.0", Apr_4th)

      )

      val releases = List(
        Release("1.0.0", Mar_4th), //3
        Release("2.0.0", Apr_10th) //6
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 2) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), Some(3)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), Some(5)))
    }


    "calculate the correct median lead time for 3 releases" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st),
        ServiceReleaseTag("2.0.0", Feb_4th),
        ServiceReleaseTag("3.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_10th),
        Release("3.0.0", Feb_18th)
      )
      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(6)))
    }


    "calculate the correct median lead time for 4 releases (3, 6, 6, 2)" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st),
        ServiceReleaseTag("2.0.0", Feb_4th),
        ServiceReleaseTag("3.0.0", Feb_10th),
        ServiceReleaseTag("4.0.0", Feb_16th)
      )


      val releases = List(
        Release("1.0.0", Feb_4th), // 3 days
        Release("2.0.0", Feb_10th), //6 days
        Release("3.0.0", Feb_16th), //6 days
        Release("4.0.0", Feb_18th) //2 days
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "ignore tags without any release" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_10th)

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st),
        ServiceReleaseTag("2.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )
      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 1) shouldBe List(ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }


    "calculate the rolling lead time for 7 months (3 months sliding window) when provided tags and releases are not ordered" in new SetUp {

      override implicit val clock: Clock = clockFrom(June_5th)

      val tags = List(
        ServiceReleaseTag("8.0.0", Apr_4th),
        ServiceReleaseTag("6.0.0", Mar_4th),
        ServiceReleaseTag("7.0.0", Mar_27th),
        ServiceReleaseTag("1.0.0", Feb_1st),
        ServiceReleaseTag("2.0.0", Feb_4th),
        ServiceReleaseTag("3.0.0", Feb_10th),
        ServiceReleaseTag("4.0.0", Feb_16th),
        ServiceReleaseTag("5.0.0", Feb_18th),
        ServiceReleaseTag("9.0.0", May_1st),
        ServiceReleaseTag("10.0.0", June_1st)
      )


      val releases = List(
        Release("7.0.0", Apr_1st), //   5 days
        Release("8.0.0", Apr_11th), //   7 days
        Release("1.0.0", Feb_4th), //  3 days
        Release("2.0.0", Feb_10th), //  6 days
        Release("3.0.0", Feb_16th), //  6 days
        Release("4.0.0", Feb_18th), //  2 days
        Release("5.0.0", Mar_1st), //   12 days
        Release("6.0.0", Mar_27th), //  23 days
        Release("9.0.0", May_11th), //  10 days //5,7,10,12,23
        Release("10.0.0", June_5th) //  4 days
      )


      ReleaseMetricCalculator.calculateLeadTimeMetric(tags, releases, 7) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Dec_1st_2015), None),
        ReleaseLeadTimeResult(YearMonth.from(Jan_1st), None),
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), Some(5)),
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(May_1st), Some(10)),
        ReleaseLeadTimeResult(YearMonth.from(June_1st), Some(6))


      )

    }
  }

  "ReleaseMetricCalculator.calculateReleaseInterval" should {

    "calculate the correct median release interval for release in the same month 3 days apart" in new SetUp {


      override implicit val clock: Clock = clockFrom(Feb_4th)

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Feb_1st), None))
    }

    "calculate the correct median release interval for two releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_16th)

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_16th)
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(12)))
    }

    "calculate the correct median release interval for releases that spans two months" in new SetUp {

      override implicit val clock: Clock = clockFrom(Apr_10th)

      val releases = List(
        Release("1.0.0", Mar_4th),
        Release("2.0.0", Apr_10th)
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 2) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Mar_1st), None),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), Some(37)))
    }


    "calculate the correctmedian release interval for 3 releases" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_10th),
        Release("3.0.0", Feb_18th)
      )
      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(7)))
    }


    "calculate the correct median release interval for 4 releases (3, 6, 6, 2)" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      //6,6,2
      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_10th),
        Release("3.0.0", Feb_16th),
        Release("4.0.0", Feb_18th)
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(6)))
    }


    "calculate the median release interval for 7 months (3 months sliding window) when provided releases are not ordered" in new SetUp {

      override implicit val clock: Clock = clockFrom(June_5th)


      val releases = List(
        Release("7.0.0", Apr_1st),
        Release("8.0.0", Apr_11th),
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_10th),
        Release("3.0.0", Feb_16th),
        Release("4.0.0", Feb_18th),
        Release("5.0.0", Mar_1st),
        Release("6.0.0", Mar_27th),
        Release("9.0.0", May_11th),
        Release("10.0.0", June_5th)
      )


      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 7) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Dec_1st_2015), None),
        ReleaseIntervalResult(YearMonth.from(Jan_1st), None),
        ReleaseIntervalResult(YearMonth.from(Feb_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Mar_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(May_1st), Some(18)), // 26,4,10,29 (4,10,26,29)
        ReleaseIntervalResult(YearMonth.from(June_1st), Some(25)) //10,29,25 (10,25,29)


      )

    }
  }

}

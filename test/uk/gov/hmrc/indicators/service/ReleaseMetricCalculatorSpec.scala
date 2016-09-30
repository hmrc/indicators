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

    val Oct_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 10, 1), midNight)
    val Nov_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 11, 1), midNight)
    val Nov_26th_2015 = LocalDateTime.of(LocalDate.of(2015, 11, 26), midNight)
    val Dec_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)
    val Dec_2nd_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 2), midNight)

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

  def release(name: String, creationDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None, version: String = "1.0.0"): Release = {
    Release(name, version, creationDate, leadTime, interval)
  }

  private val serviceName: String = "test-service"

  "ReleaseMetricCalculator.getReleaseStabilityMetrics" should {

    "calculates ReleaseStabilityMetrics's when there has been no release" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)
      val releases = List()

      ReleaseMetricCalculator.calculateReleaseStabilityMetric(releases, 1) shouldBe
        List(ReleaseStabilityMetricResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, None, None))
    }

    "calculates ReleaseStabilityMetrics's hotfix rate when there has been some hotfix releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)
      val releases = List(
        release(serviceName, Feb_4th, leadTime = Some(3), version = "1.0.0"),
        release(serviceName, Feb_4th.plusDays(1), leadTime = Some(1), version = "1.0.1"),
        release(serviceName, Feb_4th.plusDays(2), leadTime = Some(1), version = "1.0.2"),
        release(serviceName, Feb_18th, leadTime = Some(1), interval = Some(12), version = "2.0.0"))

      ReleaseMetricCalculator.calculateReleaseStabilityMetric(releases, 1) shouldBe
        List(ReleaseStabilityMetricResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(50), Some(MeasureResult(1))))
    }

    "calculates ReleaseStabilityMetrics's hotfix rate when there has been no hotfix releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)
      val releases = List(
        release(serviceName, Feb_4th, leadTime = Some(3), version = "1.0.0"),
        release(serviceName, Feb_4th.plusDays(1), leadTime = Some(1), version = "2.0.0"),
        release(serviceName, Feb_18th, leadTime = Some(1), interval = Some(12), version = "3.0.0"))

      ReleaseMetricCalculator.calculateReleaseStabilityMetric(releases, 1) shouldBe
        List(ReleaseStabilityMetricResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(0), None))
    }

    "calculates ReleaseStabilityMetrics's hotfix rate when there has been hotfixes and releases in mulitple months" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(
        release("test-service", Feb_4th, Some(3), version = "3.1.1"),
        release("test-service", Feb_10th, Some(6), version = "4.1.1"),
        release("test-service", Feb_16th, Some(4), version = "5.1.1"),
        release("test-service", Feb_18th, Some(2), version = "6.1.1"),
        release("test-service", Mar_1st, Some(12), version = "7.1.1"),
        release("test-service", Mar_27th, Some(23), version = "8.1.0"),// leadt times of hotfixes = 2,3,4,6,12 = 4 median

        release("test-service", Apr_1st, Some(5), version = "1.1.1"),
        release("test-service", Apr_11th, Some(7), version = "2.1.0"), // leadt times of hotfixes = 2,3,4,5,6,12 = 5 median
        release("test-service", May_11th, Some(10), version = "9.1.1"), // lead times =   5, 7, 10, 12, 23
        release("test-service", June_5th, Some(4), version = "10.1.0") // leadt times of hotfixes = 5 , 10 = 8 median
      )

      ReleaseMetricCalculator.calculateReleaseStabilityMetric(releases, 7) shouldBe List(
        ReleaseStabilityMetricResult(YearMonth.from(Dec_1st_2015), from = Oct_1st_2015.toLocalDate, to = toEndOfMonth(Dec_1st_2015), None, None),
        ReleaseStabilityMetricResult(YearMonth.from(Jan_1st), from = Nov_1st_2015.toLocalDate, to = toEndOfMonth(Jan_1st), None, None),
        ReleaseStabilityMetricResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = toEndOfMonth(Feb_1st), Some(100), Some(MeasureResult(4))),
        ReleaseStabilityMetricResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(83), Some(MeasureResult(4))),
        ReleaseStabilityMetricResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = toEndOfMonth(Apr_1st), Some(75), Some(MeasureResult(5))),
        ReleaseStabilityMetricResult(YearMonth.from(May_1st), from = Mar_1st.toLocalDate, to = toEndOfMonth(May_1st), Some(60), Some(MeasureResult(10))),
        ReleaseStabilityMetricResult(YearMonth.from(June_1st), from = Apr_1st.toLocalDate, to = June_5th.toLocalDate, Some(50), Some(MeasureResult(8)))
      )
    }

  }

  "ReleaseMetricCalculator.calculateLeadTime" should {

    "calculate the correct median lead time for one tag and release in the same month 3 days apart" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_4th)


      val releases = List(
        release(serviceName, Feb_4th, Some(3)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(
        ReleaseLeadTimeResult(period = YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_4th.toLocalDate, median = Some(3))
      )
    }

    "calculate the correct median lead time for two tags" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_16th)

      val releases = List(
        release("test-service", Feb_4th, Some(3)),
        release("test-service", Feb_16th, Some(6)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(
        ReleaseLeadTimeResult(period = YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_16th.toLocalDate, median = Some(5))
      )
    }

    "calculate the correct median lead time for releases that spans two months" in new SetUp {
      override implicit val clock: Clock = clockFrom(Apr_10th)

      val releases = List(
        release("test-service", Mar_4th, Some(3)),
        release("test-service", Apr_10th, Some(6))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 2) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(3)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = Apr_10th.toLocalDate, Some(5)))
    }

    "calculate the correct median lead time for 3 releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        release("test-service", Feb_6th, Some(5)),
        release("test-service", Feb_12th, Some(6)),
        release("test-service", Feb_18th, Some(8)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(6))
      )
    }

    "calculate the correct median lead time for 3 releases with one missing tag date" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        release("test-service", Feb_6th, Some(5)),
        release("test-service", Feb_12th, None), // N/A
        release("test-service", Feb_20st, Some(8)))

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(7))
      )
    }

    "calculate the correct median lead time for 4 releases (3, 6, 6, 2)" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        release("test-service", Feb_4th, Some(3)),
        release("test-service", Feb_10th, Some(6)),
        release("test-service", Feb_16th, Some(6)),
        release("test-service", Feb_18th, Some(2))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 1) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(5))
      )
    }

    "calculate the rolling lead time for 7 months (3 months sliding window) when provided tags and releases are not ordered" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(
        release("test-service", Apr_1st, Some(5)),
        release("test-service", Apr_11th, Some(7)),
        release("test-service", Feb_4th, Some(3)),
        release("test-service", Feb_10th, Some(6)),
        release("test-service", Feb_16th, Some(6)),
        release("test-service", Feb_18th, Some(2)),
        release("test-service", Mar_1st, Some(12)),
        release("test-service", Mar_27th, Some(23)),
        release("test-service", May_11th, Some(10)),
        release("test-service", June_5th, Some(4))
      )

      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 7) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Dec_1st_2015), from = Oct_1st_2015.toLocalDate, to = toEndOfMonth(Dec_1st_2015), None),
        ReleaseLeadTimeResult(YearMonth.from(Jan_1st), from = Nov_1st_2015.toLocalDate, to = toEndOfMonth(Jan_1st), None),
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = toEndOfMonth(Feb_1st), Some(5)),
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = toEndOfMonth(Apr_1st), Some(6)),
        ReleaseLeadTimeResult(YearMonth.from(May_1st), from = Mar_1st.toLocalDate, to = toEndOfMonth(May_1st), Some(10)),
        ReleaseLeadTimeResult(YearMonth.from(June_1st), from = Apr_1st.toLocalDate, to = June_5th.toLocalDate, Some(6))
      )
    }

    "calculate the median release lead time for 5 months (3 months sliding window) looking back 8 months" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(

        release("test-service", Nov_26th_2015, leadTime = Some(10)),
        release("test-service", Dec_2nd_2015, leadTime = Some(7)),
        release("test-service", Jan_10th, leadTime = Some(41)),

        release("test-service", Feb_4th, leadTime = Some(24)), // leadTime 24
        release("test-service", Feb_10th, leadTime = Some(6)), // leadTime 6
        release("test-service", Feb_16th, leadTime = Some(6)), // leadTime 6
        release("test-service", Feb_18th, leadTime = Some(2)), // leadTime 2
        release("test-service", Mar_1st, leadTime = Some(12)), //leadTime 12
        release("test-service", Mar_27th, leadTime = Some(26)), //leadTime 26
        release("test-service", Apr_1st, leadTime = Some(5)), //leadTime 5
        release("test-service", Apr_11th, leadTime = Some(10)), //leadTime 10
        release("test-service", May_11th, leadTime = Some(30)), //leadTime 30
        release("test-service", June_5th, leadTime = Some(25)) //leadTime 25
      )

      //dec None
      //jan None
      //feb 2,6,6,7,24,41   = 7
      //march 2,6,6,12,24,26,41 =12
      //april 2,6,6,24,12,26,5,10 => 2,5,6,6,10,12,24,26 = 8
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      ReleaseMetricCalculator.calculateLeadTimeMetric(releases, 5) shouldBe List(
        ReleaseLeadTimeResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = toEndOfMonth(Feb_1st), Some(7)),
        ReleaseLeadTimeResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(12)),
        ReleaseLeadTimeResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = toEndOfMonth(Apr_1st), Some(8)),
        ReleaseLeadTimeResult(YearMonth.from(May_1st), from = Mar_1st.toLocalDate, to = toEndOfMonth(May_1st), Some(12)),
        ReleaseLeadTimeResult(YearMonth.from(June_1st), from = Apr_1st.toLocalDate, to = June_5th.toLocalDate, Some(18)))
    }

  }

  "ReleaseMetricCalculator.calculateReleaseInterval" should {

    "calculate the correct median release interval for release in the same month 3 days apart" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_4th)

      val releases = List(release("test-service", Feb_4th))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_4th.toLocalDate, None)
      )
    }

    "calculate the correct median release interval for two releases" in new SetUp {

      val Jan_29th = LocalDateTime.of(LocalDate.of(2016, 1, 29), LocalTime.of(11, 16, 9))

      override implicit val clock: Clock = clockFrom(Jan_29th)

      val Jan_7th = LocalDateTime.of(LocalDate.of(2016, 1, 7), LocalTime.of(12, 16, 3))
      val Jan_28th = LocalDateTime.of(LocalDate.of(2016, 1, 28), LocalTime.of(11, 16, 3))


      val releases = List(
        release("test-service", Jan_7th),
        release("test-service", Jan_28th, interval = Some(21))
      )

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Jan_1st), from = Nov_1st_2015.toLocalDate, to = Jan_29th.toLocalDate, Some(21))
      )
    }

    "calculate the correct median release interval for releases that spans two months" in new SetUp {
      override implicit val clock: Clock = clockFrom(Apr_10th)

      val releases = List(
        release("test-service", Mar_4th),
        release("test-service", Apr_10th, interval = Some(37)))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 2) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), None),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = Apr_10th.toLocalDate, Some(37))
      )
    }

    "calculate the correct median release interval for 3 releases" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      val releases = List(
        release("test-service", Feb_4th),
        release("test-service", Feb_10th, interval = Some(6)),
        release("test-service", Feb_18th, interval = Some(8)))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(7))
      )
    }

    "calculate the correct median release interval for 4 releases (3, 6, 6, 2)" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_18th)

      //6,6,2
      val releases = List(
        release("test-service", Feb_4th),
        release("test-service", Feb_10th, interval = Some(6)),
        release("test-service", Feb_16th, interval = Some(6)),
        release("test-service", Feb_18th, interval = Some(2)))

      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 1) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = Feb_18th.toLocalDate, Some(6))
      )
    }

    "calculate the median release interval for 7 months (3 months sliding window) when provided releases are not ordered" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(
        release("test-service", May_11th, interval = Some(30)), //interval 30
        release("test-service", Mar_1st, interval = Some(12)), //interval 12
        release("test-service", Feb_10th, interval = Some(6)), // interval 6
        release("test-service", Feb_18th, interval = Some(2)), // interval 2
        release("test-service", Mar_27th, interval = Some(26)), //interval 26
        release("test-service", Apr_11th, interval = Some(10)), //interval 10
        release("test-service", Apr_1st, interval = Some(5)), //interval 5
        release("test-service", Feb_16th, interval = Some(6)), // interval 6
        release("test-service", Feb_4th, interval = None), // interval None
        release("test-service", June_5th, interval = Some(25)) //interval 25
      )

      //dec None
      //jan None
      //feb 2,6,6 = 6
      //march 2,6,6,12,26 =6
      //april 2,6,6,12,26,5,10 => 2,5,6,6,10,12,26 = 6
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 7) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Dec_1st_2015), from = Oct_1st_2015.toLocalDate, to = toEndOfMonth(Dec_1st_2015), None),
        ReleaseIntervalResult(YearMonth.from(Jan_1st), from = Nov_1st_2015.toLocalDate, to = toEndOfMonth(Jan_1st), None),
        ReleaseIntervalResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = toEndOfMonth(Feb_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = toEndOfMonth(Apr_1st), Some(6)),
        ReleaseIntervalResult(YearMonth.from(May_1st), from = Mar_1st.toLocalDate, to = toEndOfMonth(May_1st), Some(12)),
        ReleaseIntervalResult(YearMonth.from(June_1st), from = Apr_1st.toLocalDate, to = June_5th.toLocalDate, Some(18))
      )
    }

    "calculate the median release interval for 5 months (3 months sliding window) looking back 8 months" in new SetUp {
      override implicit val clock: Clock = clockFrom(June_5th)

      val releases = List(

        release("test-service", Nov_26th_2015, interval = Some(10)),
        release("test-service", Dec_2nd_2015, interval = Some(7)),
        release("test-service", Jan_10th, interval = Some(41)),

        release("test-service", Feb_4th, interval = Some(24)), // interval 24
        release("test-service", Feb_10th, interval = Some(6)), // interval 6
        release("test-service", Feb_16th, interval = Some(6)), // interval 6
        release("test-service", Feb_18th, interval = Some(2)), // interval 2
        release("test-service", Mar_1st, interval = Some(12)), //interval 12
        release("test-service", Mar_27th, interval = Some(26)), //interval 26
        release("test-service", Apr_1st, interval = Some(5)), //interval 5
        release("test-service", Apr_11th, interval = Some(10)), //interval 10
        release("test-service", May_11th, interval = Some(30)), //interval 30
        release("test-service", June_5th, interval = Some(25)) //interval 25
      )

      //dec None
      //jan None
      //feb 2,6,6,7,24,41   = 7
      //march 2,6,6,12,24,26,41 =12
      //april 2,6,6,24,12,26,5,10 => 2,5,6,6,10,12,24,26 = 8
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      ReleaseMetricCalculator.calculateReleaseIntervalMetric(releases, 5) shouldBe List(
        ReleaseIntervalResult(YearMonth.from(Feb_1st), from = Dec_1st_2015.toLocalDate, to = toEndOfMonth(Feb_1st), Some(7)),
        ReleaseIntervalResult(YearMonth.from(Mar_1st), from = Jan_1st.toLocalDate, to = toEndOfMonth(Mar_1st), Some(12)),
        ReleaseIntervalResult(YearMonth.from(Apr_1st), from = Feb_1st.toLocalDate, to = toEndOfMonth(Apr_1st), Some(8)),
        ReleaseIntervalResult(YearMonth.from(May_1st), from = Mar_1st.toLocalDate, to = toEndOfMonth(May_1st), Some(12)),
        ReleaseIntervalResult(YearMonth.from(June_1st), from = Apr_1st.toLocalDate, to = June_5th.toLocalDate, Some(18)))
    }
  }

  def toEndOfMonth(date: LocalDateTime): LocalDate = YearMonth.from(date).atEndOfMonth()
}

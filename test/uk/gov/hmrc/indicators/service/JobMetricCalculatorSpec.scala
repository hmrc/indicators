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

package uk.gov.hmrc.indicators.service

import java.time._

import org.scalactic.{TripleEqualsSupport, TypeCheckedTripleEquals}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.DateHelper.clockFrom
import uk.gov.hmrc.indicators.datasource.Build
import org.scalactic.TypeCheckedTripleEquals._


class JobMetricCalculatorSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {

  private val repositoryName: String = "test-repo"
  private val jobName: String = "some-job"
  private val jobUrl: String = "this.that.com"

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
    val Jun_5th = LocalDateTime.of(LocalDate.of(2016, 6, 5), midNight)
    val Nov_2015 = YearMonth.of(2015, 11)
    val Dec_2015 = YearMonth.of(2015, 12)
    val Jan_2016 = YearMonth.of(2016, 1)
    val Feb_2016 = YearMonth.of(2016, 2)
    val Mar_2016 = YearMonth.of(2016, 3)
    val Apr_2016 = YearMonth.of(2016, 4)
    val May_2016 = YearMonth.of(2016, 5)
    val Jun_2016 = YearMonth.of(2016, 6)
    val July_2016 = YearMonth.of(2016, 7)
    val August_2016 = YearMonth.of(2016, 8)


    def jobMetricCalculator = new JobMetricCalculator(clock)

    def clock = clockFrom(May_10th)

  }

  object ResultExtractor {

    implicit class MeasureExtractor(jobExecutionTimeResult: Seq[JobMetric]) {

      def jobMetricDetails: Seq[(YearMonth, LocalDate, LocalDate, Option[Int], Option[Double])] = {
        jobExecutionTimeResult.map { x =>
          (x.period, x.from, x.to, x.duration.map(_.median), x.successRate)
        }
      }

    }

  }

  def toEpochMillis(localDateTime: LocalDateTime) = {
    localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000
  }

  import ResultExtractor._

  "JobMetricCalculator" should {

    "calculate median build duration" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val builds = List(
        build(toEpochMillis(Feb_4th), 1, Some("SUCCESS")),
        build(toEpochMillis(Feb_4th), 2, Some("SUCCESS")),
        build(toEpochMillis(Feb_4th), 2, Some("SUCCESS")),
        build(toEpochMillis(Feb_18th), 4, Some("SUCCESS")))

      jobMetricCalculator.calculateJobMetrics(builds, 1).jobMetricDetails shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(2), Some(1.0)))
    }

    "calculate median build duration in multiple months, some empty" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val builds = List(
        build(toEpochMillis(Feb_4th), 3, Some("SUCCESS")),
        build(toEpochMillis(Feb_10th), 6, Some("SUCCESS")),
        build(toEpochMillis(Feb_16th), 4, Some("SUCCESS")),
        build(toEpochMillis(Feb_18th), 2, Some("SUCCESS")),
        build(toEpochMillis(Mar_1st), 12, Some("SUCCESS")),
        build(toEpochMillis(Mar_27th), 23, Some("SUCCESS")),

        build(toEpochMillis(Apr_1st), 5, Some("SUCCESS")),
        build(toEpochMillis(Apr_11th), 7, Some("SUCCESS")),
        build(toEpochMillis(May_11th), 10, Some("SUCCESS")), // lead times =   5, 7, 10, 12, 23
        build(toEpochMillis(Jun_5th), 4, Some("SUCCESS")) // leadt times of hotfixes = 5 , 10 = 8 median
      )

      jobMetricCalculator.calculateJobMetrics(builds, 7).jobMetricDetails shouldBe Seq(
        (Dec_2015, Oct_1st_2015.toLocalDate, toEndOfMonth(Dec_1st_2015), None, None),
        (Jan_2016, Nov_1st_2015.toLocalDate, toEndOfMonth(Jan_1st), None, None),
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(4), Some(1.0)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(5), Some(1.0)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(6), Some(1.0)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(10), Some(1.0)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(6), Some(1.0)))
    }

    "calculate the job success rate for a given month" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val builds = List(
        build(toEpochMillis(Feb_4th), 1, Some("SUCCESS")),
        build(toEpochMillis(Feb_4th), 2, Some("FAILURE")),
        build(toEpochMillis(Feb_4th), 2, Some("FAILURE")),
        build(toEpochMillis(Feb_18th), 4, Some("FAILURE")))

      jobMetricCalculator.calculateJobMetrics(builds, 1).jobMetricDetails shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(2), Some(0.25)))
    }

    "calculate the job success rate for a given month when some build results are empty or not valid" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val builds = List(
        build(toEpochMillis(Feb_4th), 1, Some("SUCCESS")),
        build(toEpochMillis(Feb_4th), 2, Some("FAILURE")),
        build(toEpochMillis(Feb_18th), 2, None),
        build(toEpochMillis(Feb_18th), 2, Some("NOT_VALID_RESULT")),
        build(toEpochMillis(Feb_18th), 4, Some("FAILURE")),
      build(toEpochMillis(Feb_18th), 4, Some("FAILURE")))

      jobMetricCalculator.calculateJobMetrics(builds, 1).jobMetricDetails shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(2), Some(0.25)))
    }
  }

  def toEndOfMonth(date: LocalDateTime): LocalDate = YearMonth.from(date).atEndOfMonth()

  def build(epochSecond: Long, duration: Int, result: Option[String]) = Build(repositoryName, jobName, jobUrl, 1234, result, epochSecond, duration, "some.url", "slave-1")


}

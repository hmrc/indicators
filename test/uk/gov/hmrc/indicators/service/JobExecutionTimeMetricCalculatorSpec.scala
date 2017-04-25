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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.DateHelper.clockFrom
import uk.gov.hmrc.indicators.datasource.Build


class JobExecutionTimeMetricCalculatorSpec extends WordSpec with Matchers  {

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


    def jobExecutionTimeMetricCalculator = new JobExecutionTimeMetricCalculator(clock)

    def clock = clockFrom(May_10th)

  }

  object ResultExtractor {

    implicit class MeasureExtractor(jobExecutionTimeResult: Seq[JobExecutionTimeMetricResult]) {

      def stability: Seq[(YearMonth, LocalDate, LocalDate, Option[Int])] = {
        jobExecutionTimeResult.map { x =>
          (x.period, x.from, x.to, x.duration.map(_.median))
        }
      }

    }

  }

  def toEpochMillis(localDateTime: LocalDateTime) = {
    localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000
  }

  import ResultExtractor._

  "DeploymentMetricCalculator for stability" should {

    "calculates median build duration" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val builds = List(
        build(toEpochMillis(Feb_4th), 1),
        build(toEpochMillis(Feb_4th), 2),
        build(toEpochMillis(Feb_4th), 2),
        build(toEpochMillis(Feb_18th), 4))

      jobExecutionTimeMetricCalculator.calculateJobExecutionTimeMetrics(builds, 1).stability shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(2)))
    }

    "calculates median build duration in mulitple months, some empty" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val builds = List(
        build(toEpochMillis(Feb_4th),  3),
        build(toEpochMillis(Feb_10th), 6),
        build(toEpochMillis(Feb_16th), 4),
        build(toEpochMillis(Feb_18th), 2),
        build(toEpochMillis(Mar_1st), 12),
        build(toEpochMillis(Mar_27th), 23),

        build(toEpochMillis(Apr_1st), 5),
        build(toEpochMillis(Apr_11th), 7),
        build(toEpochMillis(May_11th), 10), // lead times =   5, 7, 10, 12, 23
        build(toEpochMillis(Jun_5th), 4) // leadt times of hotfixes = 5 , 10 = 8 median
      )

      jobExecutionTimeMetricCalculator.calculateJobExecutionTimeMetrics(builds, 7).stability shouldBe Seq(
        (Dec_2015, Oct_1st_2015.toLocalDate, toEndOfMonth(Dec_1st_2015), None),
        (Jan_2016, Nov_1st_2015.toLocalDate, toEndOfMonth(Jan_1st), None),
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(4)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(5)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(6)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(10)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(6))
      )
    }

  }

  def toEndOfMonth(date: LocalDateTime): LocalDate = YearMonth.from(date).atEndOfMonth()

  def build(epochSecond: Long, duration: Int) = Build(repositoryName, jobName, jobUrl, 1234, Some("SUCCESS"), epochSecond, duration, "some.url", "slave-1")


}

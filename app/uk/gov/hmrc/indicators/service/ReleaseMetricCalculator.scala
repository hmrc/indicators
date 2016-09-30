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

import java.time.{Clock, LocalDate, YearMonth}

import uk.gov.hmrc.indicators.datasource.Release

import scala.util.Try

object ReleaseMetricCalculator {
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateLeadTimeMetric(releases: Seq[Release], requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseLeadTimeResult] = {

    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      withRollingAverageOf(releases, requiredMonths, _.leadTime)(ReleaseLeadTimeResult.of)
    }

  }

  def calculateReleaseIntervalMetric(releases: Seq[Release],
                                     requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {

    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      withRollingAverageOf(releases, requiredMonths, _.interval)(ReleaseIntervalResult.of)
    }
  }

  import IndicatorTraversable._

  def calculateReleaseStabilityMetric(releases: Seq[Release],
                                      requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseStabilityMetricResult] = {

    withLookBack(requiredPeriodInMonths) { requiredPeriod =>
      val releaseBuckets = getReleaseBuckets(releases, requiredPeriod)

      val releaseBucketSize: Int = releaseBuckets.size

      releaseBuckets.zipWithIndex.map { case (releaseBucket, index) =>
        val dateData = DateData(releaseBucketSize, releaseBucket, index )

        createReleaseStabilityResult(releaseBucket, dateData)
      }

    }
  }

  def createReleaseStabilityResult(releaseBucket: Iterable[(YearMonth, Seq[Release])],
                                   dateData: DateData): ReleaseStabilityMetricResult = {

    val (baseReleases, hotfixReleases) = releaseBucket.flatMap(_._2).partition(_.version.endsWith(".0"))

    val hotfixRate = Try(hotfixReleases.size * 100 / (hotfixReleases.size + baseReleases.size)).toOption

    ReleaseStabilityMetricResult(
      dateData.period,
      dateData.from,
      dateData.to,
      hotfixRate,
      hotfixReleases.flatMap(_.leadTime).median.map(x => MeasureResult(Math.round(x.toDouble).toInt)))
  }


  private case class DateData(period: YearMonth, from: LocalDate, to: LocalDate)
  private object DateData {
    def apply(releaseBucketSize: Int,
                    bucket: Iterable[(YearMonth, Seq[Release])],
                    indx: Int)(implicit clock: Clock): DateData = {

      val (period, _) = bucket.last
      val (from, _) = bucket.head

      val toDate =
        if (indx == (releaseBucketSize - 1))
          period.atDay(LocalDate.now(clock).getDayOfMonth)
        else period.atEndOfMonth()

      val fromDate: LocalDate = from.atDay(1)

      DateData(period, fromDate, toDate)
    }
  }


  private def withRollingAverageOf[T](releases: Seq[Release], requiredPeriod: Int, measure: (Release => Option[Long]))
                                                      (createMetricResult: (YearMonth, LocalDate, LocalDate, Option[BigDecimal]) => T)(implicit clock: Clock): Seq[T] = {

    val releaseBuckets = getReleaseBuckets(releases, requiredPeriod)

    val releaseBucketSize: Int = releaseBuckets.size

    releaseBuckets.zipWithIndex.map { case (bucket, indx) =>

      val dateData = DateData(releaseBucketSize, bucket, indx)

      val windowReleases: Iterable[Release] = bucket.flatMap(_._2)

      createMetricResult(
        dateData.period,
        dateData.from,
        dateData.to,
        windowReleases.flatMap(measure(_)).median
      )

    }
  }

  def getReleaseBuckets[T <: MetricsResult](releases: Seq[Release], requiredPeriod: Int)(implicit clock: Clock): Seq[Iterable[(YearMonth, Seq[Release])]] = {
    val monthlyReleaseIntervalBuckets: YearMonthTimeSeries[Release] = MonthlyBucketBuilder(releases, requiredPeriod)(_.productionDate)

    val releaseBuckets = monthlyReleaseIntervalBuckets.slidingWindow(monthlyWindowSize)
    releaseBuckets
  }

  private def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
  }


}






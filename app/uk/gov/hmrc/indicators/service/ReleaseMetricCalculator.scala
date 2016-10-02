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
import IndicatorTraversable._

import scala.util.Try

object ReleaseMetricCalculator {

  type ReleaseBucket = Iterable[(YearMonth, Seq[Release])]
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateDeploymentMetrics(releases: Seq[Release], requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[DeploymentsMetricResult] = {
    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      val releaseBuckets = getReleaseBuckets(releases, requiredMonths)
      releaseBuckets.zipWithIndex.map { case (bucket, index) =>
        val dateData = DateData(releaseBuckets.size, bucket, index)

        DeploymentsMetricResult(
          dateData.period,
          dateData.from,
          dateData.to,
          calculateThroughputMetric(bucket),
          calculateReleaseStabilityMetric(bucket))
      }

    }
  }

   def calculateThroughputMetric(bucket: Iterable[(YearMonth, Seq[Release])]): Option[Throughput] = {
    val leadTimeMetric = calculateLeadTimeMetric(bucket)
    val releaseIntervalMetric = calculateReleaseIntervalMetric(bucket)
    (leadTimeMetric, releaseIntervalMetric) match {
      case (None, None) => None
      case _ => Some(Throughput(leadTimeMetric, releaseIntervalMetric))
    }
  }

   def calculateLeadTimeMetric(releases: ReleaseBucket): Option[MeasureResult] = {

     val leadTime: (Release) => Iterable[Long] = _.leadTime
     releases.flatMap(_._2).flatMap(leadTime).median.map(MeasureResult.toMeasureResult)

  }

   def calculateReleaseIntervalMetric(releases: ReleaseBucket): Option[MeasureResult] = {

     val interval: (Release) => Iterable[Long] = _.interval
     releases.flatMap(_._2).flatMap(interval).median.map(MeasureResult.toMeasureResult)
  }

  import IndicatorTraversable._

  def calculateReleaseStabilityMetric(releases: ReleaseBucket): Option[Stability] = {

    val (baseReleases, hotfixReleases) = releases.flatMap(_._2).partition(_.version.endsWith(".0"))

    val hotfixRate: Option[Int] = Try(hotfixReleases.size * 100 / (hotfixReleases.size + baseReleases.size)).toOption

    val ms: Option[MeasureResult] = hotfixReleases.flatMap(_.leadTime).median.map(x => MeasureResult(Math.round(x.toDouble).toInt))

    (hotfixRate, ms) match {
      case (None, None) => None
      case _ => Some(Stability(hotfixRate, ms))
    }


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


   def withRollingAverageOfUsingBuckets[T](releaseBuckets: Seq[Iterable[(YearMonth, Seq[Release])]], requiredPeriod: Int, measure: (Release => Option[Long]))
                                                 (createMetricResult: (YearMonth, LocalDate, LocalDate, Option[BigDecimal]) => T)(implicit clock: Clock): Seq[T] = {

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


  def withRollingAverageOf[T](releases: Seq[Release], requiredPeriod: Int, measure: (Release => Option[Long]))
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






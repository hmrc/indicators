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

        val allReleasesInBucket: Seq[Release] = bucket.flatMap(_._2).toSeq
        DeploymentsMetricResult(dateData.period, dateData.from, dateData.to, calculateThroughputMetric(allReleasesInBucket), calculateReleaseStabilityMetric(allReleasesInBucket))
      }

    }
  }

  private def calculateThroughputMetric(releases: Seq[Release]): Option[Throughput] = {
    val leadTimeMetric = calculateMeasureResult(releases, _.leadTime)
    val releaseIntervalMetric = calculateMeasureResult(releases, _.interval)
    (leadTimeMetric, releaseIntervalMetric) match {
      case (None, None) => None
      case _ => Some(Throughput(leadTimeMetric, releaseIntervalMetric))
    }
  }


  private def calculateMeasureResult(releases: Seq[Release], measureReader: (Release) => Option[Long]): Option[MeasureResult] = {

    val measures: Iterable[Long] = for {
      release <- releases
      measure <- measureReader(release)
    } yield measure

    measures.median.map(MeasureResult.toMeasureResult)
  }


  private def calculateReleaseStabilityMetric(releases: Seq[Release]): Option[Stability] = {

    val (baseReleases, hotfixReleases) = releases.partition(_.version.endsWith(".0"))

    val hotfixRate = Try(hotfixReleases.size * 100 / (hotfixReleases.size + baseReleases.size)).toOption

    val leadTimeMeasure: Option[MeasureResult] = calculateMeasureResult(hotfixReleases, _.leadTime)

    (hotfixRate, leadTimeMeasure) match {
      case (None, None) => None
      case _ => Some(Stability(hotfixRate, leadTimeMeasure))
    }

  }


  private def getReleaseBuckets[T <: MetricsResult](releases: Seq[Release], requiredPeriod: Int)(implicit clock: Clock): Seq[Iterable[(YearMonth, Seq[Release])]] = {
    val monthlyReleaseIntervalBuckets: YearMonthTimeSeries[Release] = MonthlyBucketBuilder(releases, requiredPeriod)(_.productionDate)

    val releaseBuckets = monthlyReleaseIntervalBuckets.slidingWindow(monthlyWindowSize)
    releaseBuckets
  }

  private def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
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

}






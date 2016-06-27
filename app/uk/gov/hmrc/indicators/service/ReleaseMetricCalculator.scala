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

import java.time.temporal.ChronoUnit
import java.time.{Clock, LocalDateTime}

import uk.gov.hmrc.indicators.datasource.Release

object ReleaseMetricCalculator {
  val monthlyWindowSize: Int = 3

  def calculateLeadTimeMetric(releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseLeadTimeResult] = {
    import IndicatorTraversable._

    val monthlyReleaseLeadTimeBuckets = ReleaseMonthlyBucketBuilder(releases, periodInMonths).mapBucketItems(releaseLeadTime)

    monthlyReleaseLeadTimeBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (leadTimeYearMonth, _) = window.last
      val map = window.flatMap(_._2.flatten).map(x => x.daysSinceTag)
      ReleaseLeadTimeResult.of(leadTimeYearMonth, map.median)
    }
  }

  def calculateReleaseIntervalMetric(releases: Seq[Release],
                                      periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {
    import IndicatorTraversable._

    val monthlyReleaseBuckets = ReleaseMonthlyBucketBuilder(releases, periodInMonths)

    monthlyReleaseBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (yearMonth, _) = window.last
      val allReleaseDatesInWindow = window.flatMap { case (_, rs) => rs.map(_.productionDate) }

      ReleaseIntervalResult.of(
        yearMonth,
        if (allReleaseDatesInWindow.size > 1)
          allReleaseDatesInWindow.sliding(2).map(x => daysBetween(x.head, x.last)).median
        else None)
    }
  }

  private def releaseLeadTime(r: Release): Option[ReleaseLeadTime] =
     daysBetweenTagAndRelease(r).map { releaseLeadTimeInDays =>
       ReleaseLeadTime(r, releaseLeadTimeInDays) }

  private def daysBetweenTagAndRelease(release: Release): Option[Long] =
    release.creationDate.map { cd =>
      daysBetween(cd, release.productionDate) }

  private def daysBetween(before: LocalDateTime, after: LocalDateTime): Long =
    ChronoUnit.DAYS.between(before, after)

  case class ReleaseLeadTime(release: Release, daysSinceTag: Long)
}






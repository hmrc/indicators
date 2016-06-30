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

import java.time.chrono.ChronoPeriod
import java.time.temporal.ChronoUnit
import java.time.{Duration, Clock, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import uk.gov.hmrc.indicators.datasource.Release
import play.api.Logger

object ReleaseMetricCalculator {
  val monthlyWindowSize: Int = 3

  def calculateLeadTimeMetric(releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseLeadTimeResult] = {
    import IndicatorTraversable._

    val monthlyReleaseLeadTimeBuckets = MonthlyBucketBuilder(releases, periodInMonths)(dateExtractor = _.productionDate).mapBucketItems(releaseLeadTime)

    monthlyReleaseLeadTimeBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (leadTimeYearMonth, _) = window.last
      val releaseLeadTimes = window.flatMap(_._2.flatten).map(x => x.daysSinceTag)

      //Logger.debug(s"$leadTimeYearMonth -> ${window.flatMap(_._2.flatten).toList}")
      ReleaseLeadTimeResult.of(leadTimeYearMonth, releaseLeadTimes.median)
    }
  }

  def calculateReleaseIntervalMetric(releases: Seq[Release],
                                     periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {
    import IndicatorTraversable._

    val monthlyReleaseIntervalBuckets = MonthlyBucketBuilder(
      getReleaseIntervals(releases), periodInMonths)(_.releasedAt)

    monthlyReleaseIntervalBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (yearMonth, _) = window.last
      val windowReleaseIntervals = window.flatMap(_._2)

      //Logger.debug(s"$yearMonth -> ${windowReleaseIntervals.map(ReleaseInterval.unapply).toList.mkString("\n")}")

      ReleaseIntervalResult.of(
        yearMonth,
        windowReleaseIntervals.map(x => x.interval).median
      )
    }
  }

  private def getReleaseIntervals(releases: Seq[Release]): Seq[ReleaseInterval] = {
    val sortedReleases = releases.sortBy(_.productionDate.toEpochSecond(ZoneOffset.UTC))

    (sortedReleases, sortedReleases drop 1).zipped.map { case (r1, r2) =>
      ReleaseInterval(r2, daysBetween(r1.productionDate, r2.productionDate))
    }
  }

  private def releaseLeadTime(r: Release): Option[ReleaseLeadTime] =
    daysBetweenTagAndRelease(r).map { releaseLeadTimeInDays =>
      ReleaseLeadTime(r, releaseLeadTimeInDays)
    }

  private def daysBetweenTagAndRelease(release: Release): Option[Long] =
    release.creationDate.map { cd =>
      daysBetween(cd, release.productionDate)
    }

  private def daysBetween(before: LocalDateTime, after: LocalDateTime): Long = {

    Math.round(Duration.between(before, after).toHours / 24d)
  }

  case class ReleaseLeadTime(release: Release, daysSinceTag: Long)

  case class ReleaseInterval(release: Release, interval: Long) {
    def releasedAt = release.productionDate
  }

}






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

import java.time.{LocalDateTime, LocalDate, YearMonth, Clock}
import java.time.temporal.ChronoUnit

import play.api.Logger
import uk.gov.hmrc.indicators.datasource.{Release, ServiceReleaseTag}


object ReleaseMetricCalculator {

  val monthlyWindowSize: Int = 3


  def calculateLeadTimeMetric(tags: Seq[ServiceReleaseTag], releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseLeadTimeResult] = {

    import IndicatorTraversable._

    val monthlyReleaseLeadTimeBuckets = MonthlyBucketBuilder(releases, periodInMonths)(dateExtractor = _.releasedAt).mapBucketItems(releaseLeadTime(_, tags))

    monthlyReleaseLeadTimeBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (leadTimeYearMonth, _) = window.last
      val map: Iterable[Long] = window.flatMap(_._2.flatten).map(x => x.daysSinceTag)
      ReleaseLeadTimeResult.of(leadTimeYearMonth, map.median)
    }

  }

  def calculateReleaseIntervalMetric(releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {
    import IndicatorTraversable._

    val monthlyReleaseIntervalBuckets = MonthlyBucketBuilder(getReleaseIntervals(releases), periodInMonths)(_.releasedAt)

    monthlyReleaseIntervalBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (yearMonth, _) = window.last
      val windowReleaseIntervals = window.flatMap(_._2)
      Logger.debug(s"$yearMonth -> ${windowReleaseIntervals.map(ReleaseInterval.unapply).toList}")
      ReleaseIntervalResult.of(
        yearMonth,
        windowReleaseIntervals.map(x => x.interval).median
      )
    }
  }

  private def getReleaseIntervals(releases: Seq[Release]): Seq[ReleaseInterval] = {

    (releases, releases drop 1).zipped.map { case (r1, r2) =>
      ReleaseInterval(r2, daysBetween(r1.releasedAt, r2.releasedAt))
    }
  }


  private def releaseLeadTime(r: Release, tags: Seq[ServiceReleaseTag]): Option[ReleaseLeadTime] = {

    tags.find(t => t.name == r.version)
      .map {
      t =>
        val releaseLeadTimeInDays = daysBetweenTagAndRelease(t, r)
        ReleaseLeadTime(r, releaseLeadTimeInDays)
    }
  }

  def daysBetweenTagAndRelease(tag: ServiceReleaseTag, release: Release): Long = {
    daysBetween(tag.createdAt, release.releasedAt)
  }

  def daysBetween(before: LocalDateTime, after: LocalDateTime): Long = {
    ChronoUnit.DAYS.between(before, after)
  }

  case class ReleaseLeadTime(release: Release, daysSinceTag: Long)

  case class ReleaseInterval(release: Release, interval: Long) {
    def releasedAt = release.releasedAt
  }

}






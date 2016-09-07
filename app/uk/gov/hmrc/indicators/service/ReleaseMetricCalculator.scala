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

    val monthlyReleaseLeadTimeBuckets = MonthlyBucketBuilder(releases, periodInMonths)(dateExtractor = _.productionDate)

    monthlyReleaseLeadTimeBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (leadTimeYearMonth, _) = window.last
      val releaseLeadTimes = window.flatMap(x => x._2).flatMap(x => x.leadTime)

      //Logger.debug(s"$leadTimeYearMonth -> ${window.flatMap(_._2.flatten).toList}")
      ReleaseLeadTimeResult.of(leadTimeYearMonth, releaseLeadTimes.median)
    }
  }

  def calculateReleaseIntervalMetric(releases: Seq[Release],
                                     periodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {
    import IndicatorTraversable._

    val monthlyReleaseIntervalBuckets = MonthlyBucketBuilder(releases, periodInMonths)(_.productionDate)

    monthlyReleaseIntervalBuckets.slidingWindow(monthlyWindowSize).map { window =>
      val (yearMonth, _) = window.last
      val windowReleaseIntervals = window.flatMap(_._2)

      ReleaseIntervalResult.of(
        yearMonth,
        windowReleaseIntervals.flatMap(x => x.interval).median
      )
    }
  }
}






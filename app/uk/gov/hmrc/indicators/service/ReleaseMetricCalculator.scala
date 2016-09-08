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

import java.time.Clock

import uk.gov.hmrc.indicators.datasource.Release

object ReleaseMetricCalculator {
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateLeadTimeMetric(releases: Seq[Release], requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseLeadTimeResult] = {
    import IndicatorTraversable._

    withLookBack(requiredPeriodInMonths){ period =>
      val monthlyReleaseLeadTimeBuckets = MonthlyBucketBuilder(releases, period)(dateExtractor = _.productionDate)

      monthlyReleaseLeadTimeBuckets.slidingWindow(monthlyWindowSize).map { window =>
        val (leadTimeYearMonth, _) = window.last
        val releaseLeadTimes = window.flatMap(x => x._2).flatMap(x => x.leadTime)
        ReleaseLeadTimeResult.of(leadTimeYearMonth, releaseLeadTimes.median)
      }
    }

  }

  def calculateReleaseIntervalMetric(releases: Seq[Release],
                                     requiredPeriodInMonths: Int = 9)(implicit clock: Clock): Seq[ReleaseIntervalResult] = {
    import IndicatorTraversable._

    withLookBack(requiredPeriodInMonths) { period =>

      val monthlyReleaseIntervalBuckets = MonthlyBucketBuilder(releases, period)(_.productionDate)

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

  def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
  }
}






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
import java.time.{ZoneOffset, Clock, LocalDate, YearMonth}

import play.api.Logger
import uk.gov.hmrc.gitclient.GitTag

object IndicatorTraversable {

  implicit class TravOnce[A](self: TraversableOnce[A]) {
    def median[B >: A](implicit num: scala.Numeric[B], ord: Ordering[A]): Option[BigDecimal] = {
      val sorted = self.toList.sorted

      sorted.size match {
        case 0 => None
        case n if n % 2 == 0 =>
          val idx = (n - 1) / 2
          Some(sorted.drop(idx).dropRight(idx).average(num))
        case n => Some(BigDecimal(num.toDouble(sorted(n / 2))))
      }
    }

    def average[B >: A](implicit num: scala.Numeric[B]): BigDecimal = {
      BigDecimal(self.map(n => num.toDouble(n)).sum) / BigDecimal(self.size)
    }
  }

}

object LeadTimeCalculator {

  import IndicatorTraversable._

  def calculateLeadTime(tags: Seq[RepoTag], releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): List[LeadTimeResult] = {

    val start = YearMonth.now(clock).minusMonths(periodInMonths-1)
    val end   = YearMonth.now(clock)

    def releasesForYearMonth(ym: YearMonth): List[ReleaseLeadTime] = {
      releases
        .filter(r => YearMonth.from(r.releasedAt) == ym)
        .map(r => releaseLeadTime(r, tags)).toList.flatten
    }

    val timeSeries = YearMonthTimeSeries(start, end, bucketBuilder = releasesForYearMonth)

    timeSeries.expandingWindow.map { window =>
      val (leadTimeYearMonth, _) = window.last
      LeadTimeResult.of(leadTimeYearMonth, window.flatMap(_._2).map(_.daysSinceTag).median)
    }.toList
  }

  def releaseLeadTime(r: Release, tags: Seq[RepoTag]): Option[ReleaseLeadTime] = {

    tags.find(t => t.name == r.version && t.createdAt.isDefined)
      .map {
        t =>
          val releaseLeadTimeInDays = daysBetweenTagAndRelease(t, r)
          ReleaseLeadTime(r, releaseLeadTimeInDays)
      }
  }

  def daysBetweenTagAndRelease(tag: RepoTag, release: Release): Long = {
    ChronoUnit.DAYS.between(tag.createdAt.get.toLocalDate, release.releasedAt)
  }

  case class ReleaseLeadTime(release: Release, daysSinceTag: Long)
}






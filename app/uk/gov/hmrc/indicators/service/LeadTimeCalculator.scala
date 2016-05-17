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
import java.time.{Clock, LocalDate, YearMonth}

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

  def calculateLeadTime(tags: Seq[RepoTag], releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): List[ProductionLeadTime] = {
    val now = YearMonth.now(clock)

    Logger.debug(s"All releases ${releases.map(x => (x.version, x.releasedAt))}")

    val months = Iterator.iterate(now)(_ minusMonths 1)
      .take(periodInMonths).toList

    val allReleasesInThePeriod: Seq[Release] = releases.sortBy(_.releasedAt.toEpochDay).foldLeft(List.empty[Release]){
      case (ls, r) =>
        if(ls.exists(_.version == r.version)) ls
        else ls :+ r

    }.dropWhile(r => YearMonth.from(r.releasedAt).isBefore(months.last))

    Logger.debug(s"All releases in the period ${allReleasesInThePeriod.map(x => (x.version, x.releasedAt))}")

    val allReleaseLeadTimes: Seq[ReleaseLeadTime] = allReleasesInThePeriod
      .flatMap { r =>
      val times: Iterable[ReleaseLeadTime] = releaseLeadTime(r, tags)
      times
    }


    months.reverseMap { ym =>
      val releaseLeadTimes = allReleaseLeadTimes.takeWhile {
        case ReleaseLeadTime(release, days) =>
          val rym = YearMonth.from(release.releasedAt)
          rym.equals(ym) || rym.isBefore(ym)
      }

      ProductionLeadTime(
        LocalDate.of(ym.getYear, ym.getMonthValue, 1),
        releaseLeadTimes.map(_.daysSinceTag).median)
    }
  }

  def releaseLeadTime(r: Release, tags: Seq[RepoTag]): Option[ReleaseLeadTime] = {

    tags.find(t => t.name == r.version && t.createdAt.isDefined)
      .map {
      t =>
        val releaseLeadTimeInDays = daysBetweenTagAndRelease(t, r)
        Logger.debug(s"Release : ${r.version} -> ${r.releasedAt}, tag : ${t.name} -> ${t.createdAt.get.toLocalDate}, lead time -> $releaseLeadTimeInDays")
        ReleaseLeadTime(r, releaseLeadTimeInDays)
    }
  }

  def daysBetweenTagAndRelease(tag: RepoTag, release: Release): Long = {
    ChronoUnit.DAYS.between(tag.createdAt.get.toLocalDate, release.releasedAt)
  }

  case class ReleaseLeadTime(release: Release, daysSinceTag: Long)

}

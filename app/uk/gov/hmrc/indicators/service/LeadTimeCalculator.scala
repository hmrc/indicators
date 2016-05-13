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

  def calculateRollingLeadTime(tags: Seq[RepoTag], releases: Seq[Release], periodInMonths: Int = 9)(implicit clock: Clock): List[ProductionLeadTime] = {

    val now: YearMonth = YearMonth.now(clock)

    val t = Iterator.iterate(now)(_ minusMonths 1)
      .take(periodInMonths).toList

    val rt = releases
      .dropWhile(r => YearMonth.from(r.releasedAt).isBefore(t.last))
      .map { r => releaseLeadTime(r, tags).map((r, _)) }.flatten

    t.reverseMap { ym =>
      val m = rt.takeWhile { case (r, lt) =>
        val rym: YearMonth = YearMonth.from(r.releasedAt)
        rym.equals(ym) || rym.isBefore(ym)
      }.map(_._2).median

      ProductionLeadTime(LocalDate.of(ym.getYear, ym.getMonthValue, 1), m)
    }

  }

  def releaseLeadTime(r: Release, tags: Seq[RepoTag]): Option[Long] = {
    val find: Option[RepoTag] = tags.find(t => t.name == r.version && t.createdAt.isDefined)
    find.map(t => days(t, r))
  }
  
  def days(tag: RepoTag, release: Release): Long = {
    ChronoUnit.DAYS.between(tag.createdAt.get.toLocalDate, release.releasedAt)
  }
}

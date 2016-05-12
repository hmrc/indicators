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
import java.time.{LocalDate, YearMonth}

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

  def calculateRollingLeadTime(tags: Seq[GitTag], releases: Seq[Release], periodInMonths: Int = 9): List[ProductionLeadTime] = {

    val t = Iterator.iterate(YearMonth.now())(_ minusMonths 1)
      .take(periodInMonths).toList

    val rt = releases
      .dropWhile(r => YearMonth.from(r.fs).isBefore(t.last))
      .map { r => releaseLeadTime(r, tags).map((r, _)) }.flatten

    t.reverseMap { ym =>
      val m = rt.takeWhile { case (r, lt) =>
        val rym: YearMonth = YearMonth.from(r.fs)
        rym.equals(ym) || rym.isBefore(ym)
      }.map(_._2).median

      ProductionLeadTime(LocalDate.of(ym.getYear, ym.getMonthValue, 1), m)
    }

  }

  def releaseLeadTime(r: Release, tags: Seq[GitTag]): Option[Long] = {
    val find: Option[GitTag] = tags.find(_.name == r.ver)
    find.map(t => days(t, r))
  }

  def calculateLeadTime(tags: Seq[GitTag], releases: Seq[Release]): List[ProductionLeadTime] = {

    val groupByReleaseMonth: Map[Int, Seq[(GitTag, Release)]] = tags
      .map(t => t -> releases.find(r => r.ver == t.name))
      .collect { case (t, Some(r)) => t -> r }
      .groupBy { case (t, r) => r.fs.getMonth.getValue }

    groupByReleaseMonth.map { case (m, seq) =>
      val leadTimes = calculateLeadTimes(seq)
      ProductionLeadTime(seq.head._2.fs, leadTimes.median)
    }.toList.sortBy(_.period.toEpochDay)
  }


  def calculateLeadTimes(seq: Seq[(GitTag, Release)]): Seq[Long] = {
    seq.map { case (t, r) =>
      days(t, r)
    }
  }

  def days(tag: GitTag, release: Release): Long = {
    ChronoUnit.DAYS.between(tag.createdAt.get.toLocalDate, release.fs)
  }
}

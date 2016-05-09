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

import uk.gov.hmrc.gitclient.GitTag

object IndicatorTraversable{

  implicit class TravOnce[A](self:TraversableOnce[A]){
    def median[B >: A](implicit num : scala.Numeric[B],  ord:Ordering[A]) : BigDecimal ={
      self.size match {
        case 1 => BigDecimal(num.toDouble(self.toList.sorted.head))
        case 2 => average(num)
        case n if n % 2 == 0 => BigDecimal(num.toDouble(self.toList.sorted.apply((self.size / 2)-1)))
        case n if n % 2 == 1 => BigDecimal(num.toDouble(self.toList.sorted.apply(self.size / 2)))
      }
    }

    def average[B >: A](implicit num : scala.Numeric[B]) : BigDecimal ={
      BigDecimal(self.map(n => num.toDouble(n)).sum) / BigDecimal(self.size)
    }
  }
}

object LeadTimeCalculator {

  import IndicatorTraversable._


  def calculateLeadTime(tags: Seq[GitTag], releases: Seq[Release]): List[ProductionLeadTime] = {

    val groupByReleaseMonth: Map[Int, Seq[(GitTag, Release)]] = tags
      .map(t => t -> releases.find(r => r.tag == t.name))
      .collect { case (t, Some(r)) => t -> r }
      .groupBy { case (t, r) => r.date.getMonth.getValue }

    groupByReleaseMonth
      .map { case (m , seq) =>
        val leadTimes = calculateLeadTimes(seq)
        ProductionLeadTime(seq.head._2.date, leadTimes.median)
      }.toList.sortBy(_.period.toEpochDay)
  }


  def calculateLeadTimes(seq: Seq[(GitTag, Release)]): Seq[Long] = {
    seq.map { case (t, r) =>
      ChronoUnit.DAYS.between(t.createdAt.get.toLocalDate, r.date)
    }
  }
}

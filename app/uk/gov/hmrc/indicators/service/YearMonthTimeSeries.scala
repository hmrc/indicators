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

import java.time.YearMonth

object YearMonthTimeSeries {

  def apply[B](start: YearMonth, endInclusive: YearMonth, bucketBuilder: (YearMonth) => Seq[B]): YearMonthTimeSeries[B] = {
    new YearMonthTimeSeries[B] {
      override def iterator: Iterator[(YearMonth, Seq[B])] = {

        val map: Iterator[(YearMonth, Seq[B])] = Iterator.iterate(start)(_.plusMonths(1)).takeWhile(a => a.isBefore(endInclusive) || a.equals(endInclusive))
          .map(ym => ym -> bucketBuilder(ym))
        map

      }

    }
  }
}

trait YearMonthTimeSeries[B] extends Iterable[(YearMonth, Seq[B])] {
  self =>

  def mapBucketItems[T](f: B => T): YearMonthTimeSeries[T] =

    new YearMonthTimeSeries[T] {

      override def iterator: Iterator[(YearMonth, Seq[T])] = {
        self.map { case (month, items) =>
          (month, items.map(f))
        }.toIterator

      }

    }

  def slidingWindow(windowSize: Int): Seq[Iterable[(YearMonth, Seq[B])]] = {

    val expanding =
      (1 to Math.min(windowSize - 1, this.size)).map { i =>
        this.take(windowSize - 1).take(i)
      }

    if (this.size < windowSize)
      expanding
    else
      expanding ++ this.sliding(windowSize).toSeq

  }
}

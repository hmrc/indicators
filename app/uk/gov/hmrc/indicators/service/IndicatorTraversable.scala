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

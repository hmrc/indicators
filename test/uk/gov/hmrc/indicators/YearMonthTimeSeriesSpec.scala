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

package uk.gov.hmrc.indicators

import java.time.YearMonth

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.service.YearMonthTimeSeries

import scala.concurrent.ExecutionContext.Implicits.global

class YearMonthTimeSeriesSpec extends WordSpec with Matchers {

  "YearMonthTimeSeries" should {

    "produce a time series where from and to are included in the answer" in {

      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 3), ym => List())

      timeSeries.toList shouldBe List(
        (YearMonth.of(2016, 1), List.empty[Int]),
        (YearMonth.of(2016, 2), List.empty[Int]),
        (YearMonth.of(2016, 3), List.empty[Int])
      )
    }

    "produce an expanding window" in {

      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 3), ym => List())

      timeSeries.expandingWindow.toList shouldBe List(
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int])),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int])),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int]),
          (YearMonth.of(2016, 3), List.empty[Int]))
      )
    }
  }
}

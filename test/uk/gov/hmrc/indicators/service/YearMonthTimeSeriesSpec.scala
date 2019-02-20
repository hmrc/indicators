/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.{Matchers, WordSpec}

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

    "map monthly bucket items to given function" in {
      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 3), ym => List(1, 2, 3))

      timeSeries.toList shouldBe List(
        (YearMonth.of(2016, 1), List(1, 2, 3)),
        (YearMonth.of(2016, 2), List(1, 2, 3)),
        (YearMonth.of(2016, 3), List(1, 2, 3))
      )

      timeSeries.mapBucketItems(_ * 2).toList shouldBe List(
        (YearMonth.of(2016, 1), List(2, 4, 6)),
        (YearMonth.of(2016, 2), List(2, 4, 6)),
        (YearMonth.of(2016, 3), List(2, 4, 6))
      )
    }

    "produce a sliding window" in {

      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 9), ym => List())

      timeSeries.slidingWindow(3).toList shouldBe List(
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int]),
          (YearMonth.of(2016, 3), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 2), List.empty[Int]),
          (YearMonth.of(2016, 3), List.empty[Int]),
          (YearMonth.of(2016, 4), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 3), List.empty[Int]),
          (YearMonth.of(2016, 4), List.empty[Int]),
          (YearMonth.of(2016, 5), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 4), List.empty[Int]),
          (YearMonth.of(2016, 5), List.empty[Int]),
          (YearMonth.of(2016, 6), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 5), List.empty[Int]),
          (YearMonth.of(2016, 6), List.empty[Int]),
          (YearMonth.of(2016, 7), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 6), List.empty[Int]),
          (YearMonth.of(2016, 7), List.empty[Int]),
          (YearMonth.of(2016, 8), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 7), List.empty[Int]),
          (YearMonth.of(2016, 8), List.empty[Int]),
          (YearMonth.of(2016, 9), List.empty[Int])
        )
      )
    }

    "produce sliding window when there are less months then the window size" in {

      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 2), ym => List())

      timeSeries.slidingWindow(3).toList shouldBe List(
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int])
        )
      )

      timeSeries.slidingWindow(4).toList shouldBe List(
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int])
        )
      )

    }

    "produce sliding window when there are same months then the window size" in {

      val timeSeries = YearMonthTimeSeries[Int](YearMonth.of(2016, 1), YearMonth.of(2016, 3), ym => List())

      timeSeries.slidingWindow(3).toList shouldBe List(
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int])
        ),
        Iterable(
          (YearMonth.of(2016, 1), List.empty[Int]),
          (YearMonth.of(2016, 2), List.empty[Int]),
          (YearMonth.of(2016, 3), List.empty[Int])
        )
      )
    }

  }
}

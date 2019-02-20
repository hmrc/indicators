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

import org.scalatest.{Matchers, OptionValues, WordSpec}

class IndicatorTraversableSpec extends WordSpec with Matchers with OptionValues {

  import IndicatorTraversable._

  "IndicatorTraversable" should {
    "return none as median for empty seq" in {
      Seq.empty[Int].median shouldBe None
    }

    "find the median of 1 number" in {
      Seq(1).median.get shouldBe 1
    }

    "find the median of 2 numbers by calculating the average" in {
      Seq(1, 2).median.get shouldBe 1.5
    }

    "find the median of 3 numbers" in {
      Seq(1, 2, 3).median.get shouldBe 2
    }

    "find the median of 4 numbers by taking the left of the two inner numbers" in {
      Seq(7, 7, 1, 4).median.get shouldBe 5.5
    }

    "find the median of 5 numbers by taking the left of the two inner numbers" in {
      Seq(7, 7, 1, 4, 3).median.get shouldBe 4
    }

    "find the median of random series of numbers" in {
      (1 to 10).median.get                               shouldBe 5.5
      (11 to 20).median.get                              shouldBe 15.5
      Seq(1, 2, 3, 3, 5, 20, 7, 8, 9, 10, 30).median.get shouldBe 7

    }
  }

}

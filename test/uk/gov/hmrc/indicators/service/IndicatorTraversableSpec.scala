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

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global

class IndicatorTraversableSpec extends WordSpec with Matchers{

  import IndicatorTraversable._

  "IndicatorTraversable" should {
    "find the median of 1 number" in {
      Seq(1).median shouldBe 1
    }

    "find the median of 2 numbers by calculating the average" in {
      Seq(1, 2).median shouldBe 1.5
    }

    "find the median of 3 numbers" in {
      Seq(1, 2, 3).median shouldBe 2
    }

    "find the median of 4 numbers by taking the left of the two inner numbers" in {
      Seq(1, 5, 7, 7).median shouldBe 5
    }
  }

}

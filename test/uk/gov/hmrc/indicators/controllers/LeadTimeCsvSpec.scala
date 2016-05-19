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

package uk.gov.hmrc.indicators.controllers

import java.time.YearMonth

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.service.LeadTimeResult

class LeadTimeCsvSpec extends WordSpec with Matchers {

  "LeadTimeCsv" should {

    "return deployment lead times for a given service in csv format" in {

      val results = List(
        LeadTimeResult(YearMonth.of(2016, 4), Some(5)),
        LeadTimeResult(YearMonth.of(2016, 5), Some(6))
      )
      val content = LeadTimeCsv(results, "serviceName")

      content shouldBe """|Name,2016-04,2016-05
                         |serviceName,5,6""".stripMargin

    }

    "return deployment lead times csv having months with missing median" in {

      val results = List(
        LeadTimeResult(YearMonth.of(2016, 3), Some(5)),
        LeadTimeResult(YearMonth.of(2016, 4), None),
        LeadTimeResult(YearMonth.of(2016, 5), Some(6))
      )
      val content = LeadTimeCsv(results, "serviceName")

      content shouldBe """|Name,2016-03,2016-04,2016-05
                          |serviceName,5,,6""".stripMargin

    }

  }

}

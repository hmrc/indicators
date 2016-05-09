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

import java.time.{LocalDate, ZonedDateTime}
import java.util.TimeZone

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.gitclient.GitTag


class LeadTimeCalculatorSpec extends WordSpec with Matchers{


  implicit class RichLocalDate(self:LocalDate){
    def zoned: ZonedDateTime  = self.atStartOfDay().atZone(TimeZone.getDefault().toZoneId)
  }

  val Feb_1st = LocalDate.of(2000, 2, 1)
  val Feb_4th = Feb_1st.plusDays(3)
  val Feb_10th = Feb_1st.plusDays(9)
  val Feb_16th = Feb_1st.plusDays(15)

  val Mar_1st = LocalDate.of(2000, 3, 1)
  val Mar_4th = LocalDate.of(2000, 3, 4)

  val Apr_1st = LocalDate.of(2000, 4, 1)
  val Apr_4th = LocalDate.of(2000, 4, 4)
  val Apr_10th = LocalDate.of(2000, 4, 10)

  "LeadTimeCalculator" should {
    "calculate the correct median lead time for one tag and release in the same month 3 days apart" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned))
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, 3))
    }

    "calculate the correct median lead time for two tags" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_10th.zoned))
      )

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_16th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, 4.5))
    }

    "calculate the correct median lead time for releases that spans two months" in {

      val tags = List(
        GitTag("1.0.0", Some(Mar_1st.zoned)),
        GitTag("2.0.0", Some(Apr_4th.zoned))

      )

      val releases = List(
        Release("1.0.0", Mar_4th),
        Release("2.0.0", Apr_10th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Mar_4th, 3), ProductionLeadTime(Apr_10th, 6))
    }

    "ignore tags without any release" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_10th.zoned))
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )
      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, 3))
    }

  }
}

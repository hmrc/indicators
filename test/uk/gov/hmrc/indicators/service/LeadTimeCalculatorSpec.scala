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


class LeadTimeCalculatorSpec extends WordSpec with Matchers {


  implicit class RichLocalDate(self: LocalDate) {
    def zoned: ZonedDateTime = self.atStartOfDay().atZone(TimeZone.getDefault().toZoneId)
  }

  val Dec_1st_2015 = LocalDate.of(2015, 12, 1)

  val Jan_1st = LocalDate.of(2016, 1, 1)
  val Jan_10th = LocalDate.of(2016, 1, 10)

  val Feb_1st = LocalDate.of(2016, 2, 1)
  val Feb_4th = LocalDate.of(2016, 2, 4)
  val Feb_9th = LocalDate.of(2016, 2, 9)
  val Feb_10th = LocalDate.of(2016, 2, 10)
  val Feb_16th = LocalDate.of(2016, 2, 16)
  val Feb_18th = LocalDate.of(2016, 2, 18)

  val Mar_1st = LocalDate.of(2016, 3, 1)
  val Mar_4th = LocalDate.of(2016, 3, 4)
  val March_10th = LocalDate.of(2016, 3, 10)
  val Mar_27th = LocalDate.of(2016, 3, 27)

  val Apr_1st = LocalDate.of(2016, 4, 1)
  val Apr_4th = LocalDate.of(2016, 4, 4)
  val Apr_10th = LocalDate.of(2016, 4, 10)
  val Apr_11th = LocalDate.of(2016, 4, 11)
  val May_1st = LocalDate.of(2016, 5, 1)
  val May_10th = LocalDate.of(2016, 5, 10)

  "LeadTimeCalculator.calculateRollingLeadTime" should {
    "calculate the rolling lead time for 9 months" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_4th.zoned)),
        GitTag("3.0.0", Some(Feb_10th.zoned)),
        GitTag("4.0.0", Some(Feb_16th.zoned)),
        GitTag("5.0.0", Some(Feb_18th.zoned)),
        GitTag("6.0.0", Some(Mar_4th.zoned)),
        GitTag("7.0.0", Some(Mar_27th.zoned)),
        GitTag("8.0.0", Some(Apr_4th.zoned))
      )


      val releases = List(
        Release("", "", "1.0.0", Feb_4th), //  3 days
        Release("", "", "2.0.0", Feb_10th), //  6 days
        Release("", "", "3.0.0", Feb_16th), //  6 days
        Release("", "", "4.0.0", Feb_18th), //  2 days
        Release("", "", "5.0.0", Mar_1st), //   12 days
        Release("", "", "6.0.0", Mar_27th), //  23 days
        Release("", "", "7.0.0", Apr_1st), //   5 days
        Release("", "", "8.0.0", Apr_11th) //   7 days
      )


      LeadTimeCalculator.calculateRollingLeadTime(tags, releases, 6) shouldBe List(
        ProductionLeadTime(Dec_1st_2015, None),
        ProductionLeadTime(Jan_1st, None),
        ProductionLeadTime(Feb_1st, Some(4.5)),
        ProductionLeadTime(Mar_1st, Some(6.0)),
        ProductionLeadTime(Apr_1st, Some(6.0)),
        ProductionLeadTime(May_1st, Some(6.0))
      )

    }
  }

  "LeadTimeCalculator.calculateLeadTime" should {
    "calculate the correct median lead time for one tag and release in the same month 3 days apart" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned))
      )

      val releases = List(
        Release("", "", "1.0.0", Feb_4th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, Some(3)))
    }

    "calculate the correct median lead time for two tags" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_10th.zoned))
      )

      val releases = List(
        Release("", "", "1.0.0", Feb_4th),
        Release("", "", "2.0.0", Feb_16th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, Some(4.5)))
    }

    "calculate the correct median lead time for releases that spans two months" in {

      val tags = List(
        GitTag("1.0.0", Some(Mar_1st.zoned)),
        GitTag("2.0.0", Some(Apr_4th.zoned))

      )

      val releases = List(
        Release("", "", "1.0.0", Mar_4th),
        Release("", "", "2.0.0", Apr_10th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Mar_4th, Some(3)), ProductionLeadTime(Apr_10th, Some(6)))
    }


    "calculate the correct median lead time for 3 releases" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_4th.zoned)),
        GitTag("3.0.0", Some(Feb_10th.zoned))
      )

      val releases = List(
        Release("", "", "1.0.0", Feb_4th),
        Release("", "", "2.0.0", Feb_10th),
        Release("", "", "3.0.0", Feb_18th)
      )
      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, Some(6)))
    }


    "calculate the correct median lead time for 4 releases (3, 6, 6, 2)" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_4th.zoned)),
        GitTag("3.0.0", Some(Feb_10th.zoned)),
        GitTag("4.0.0", Some(Feb_16th.zoned))
      )


      val releases = List(
        Release("", "", "1.0.0", Feb_4th), // 3 days
        Release("", "", "2.0.0", Feb_10th), //6 days
        Release("", "", "3.0.0", Feb_16th), //6 days
        Release("", "", "4.0.0", Feb_18th) //2 days
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, Some(4.5)))
    }

    "ignore tags without any release" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st.zoned)),
        GitTag("2.0.0", Some(Feb_10th.zoned))
      )

      val releases = List(
        Release("", "", "1.0.0", Feb_4th)
      )
      LeadTimeCalculator.calculateLeadTime(tags, releases) shouldBe List(ProductionLeadTime(Feb_4th, Some(3)))
    }

  }
}

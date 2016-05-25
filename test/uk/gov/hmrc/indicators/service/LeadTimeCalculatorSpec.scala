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

import java.time._

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.DateHelper._
import uk.gov.hmrc.indicators.datasource.{Release, RepoReleaseTag}


class LeadTimeCalculatorSpec extends WordSpec with Matchers {

  trait SetUp {
    private val midNight: LocalTime = LocalTime.of(0, 0)
    val Dec_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)

    val Jan_1st = LocalDateTime.of(LocalDate.of(2016, 1, 1), midNight)
    val Jan_10th = LocalDateTime.of(LocalDate.of(2016, 1, 10), midNight)

    val Feb_1st = LocalDateTime.of(LocalDate.of(2016, 2, 1), midNight)
    val Feb_4th = LocalDateTime.of(LocalDate.of(2016, 2, 4), midNight)
    val Feb_9th = LocalDateTime.of(LocalDate.of(2016, 2, 9), midNight)
    val Feb_10th = LocalDateTime.of(LocalDate.of(2016, 2, 10), midNight)
    val Feb_16th = LocalDateTime.of(LocalDate.of(2016, 2, 16), midNight)
    val Feb_18th = LocalDateTime.of(LocalDate.of(2016, 2, 18), midNight)

    val Mar_1st = LocalDateTime.of(LocalDate.of(2016, 3, 1), midNight)
    val Mar_4th = LocalDateTime.of(LocalDate.of(2016, 3, 4), midNight)
    val March_10th = LocalDateTime.of(LocalDate.of(2016, 3, 10), midNight)
    val Mar_27th = LocalDateTime.of(LocalDate.of(2016, 3, 27), midNight)

    val Apr_1st = LocalDateTime.of(LocalDate.of(2016, 4, 1), midNight)
    val Apr_4th = LocalDateTime.of(LocalDate.of(2016, 4, 4), midNight)
    val Apr_10th = LocalDateTime.of(LocalDate.of(2016, 4, 10), midNight)
    val Apr_11th = LocalDateTime.of(LocalDate.of(2016, 4, 11), midNight)
    val May_1st = LocalDateTime.of(LocalDate.of(2016, 5, 1), midNight)
    val May_10th = LocalDateTime.of(LocalDate.of(2016, 5, 10), midNight)

    implicit val clock = clockFrom(May_10th)
  }


  "LeadTimeCalculator.calculateRollingLeadTime" should {

    "calculate the correct median lead time for one tag and release in the same month 3 days apart" in new SetUp {


      override implicit val clock: Clock = clockFrom(Feb_4th)

      val tags = List(
        RepoReleaseTag("1.0.0", Feb_1st)
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases, 1) shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }

    "calculate the correct median lead time for two tags" in new SetUp {
      override implicit val clock: Clock = clockFrom(Feb_16th)

      val tags = List(
        RepoReleaseTag("1.0.0", Feb_1st),
        RepoReleaseTag("2.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_16th)
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases, 1) shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "calculate the correct median lead time for releases that spans two months" in new SetUp {

      override implicit val clock: Clock = clockFrom(Apr_10th)

      val tags = List(
        RepoReleaseTag("1.0.0", Mar_1st),
        RepoReleaseTag("2.0.0", Apr_4th)

      )

      val releases = List(
        Release("1.0.0", Mar_4th), //3
        Release("2.0.0", Apr_10th) //6
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases, 2) shouldBe List(
        LeadTimeResult(YearMonth.from(Mar_1st), Some(3)),
        LeadTimeResult(YearMonth.from(Apr_1st), Some(5)))
    }


    "calculate the correct median lead time for 3 releases" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      val tags = List(
        RepoReleaseTag("1.0.0", Feb_1st),
        RepoReleaseTag("2.0.0", Feb_4th),
        RepoReleaseTag("3.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th),
        Release("2.0.0", Feb_10th),
        Release("3.0.0", Feb_18th)
      )
      LeadTimeCalculator.calculateLeadTime(tags, releases, 1) shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(6)))
    }


    "calculate the correct median lead time for 4 releases (3, 6, 6, 2)" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_18th)

      val tags = List(
        RepoReleaseTag("1.0.0", Feb_1st),
        RepoReleaseTag("2.0.0", Feb_4th),
        RepoReleaseTag("3.0.0", Feb_10th),
        RepoReleaseTag("4.0.0", Feb_16th)
      )


      val releases = List(
        Release("1.0.0", Feb_4th), // 3 days
        Release("2.0.0", Feb_10th), //6 days
        Release("3.0.0", Feb_16th), //6 days
        Release("4.0.0", Feb_18th) //2 days
      )

      LeadTimeCalculator.calculateLeadTime(tags, releases, 1) shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(5)))
    }

    "ignore tags without any release" in new SetUp {

      override implicit val clock: Clock = clockFrom(Feb_10th)

      val tags = List(
        RepoReleaseTag("1.0.0", Feb_1st),
        RepoReleaseTag("2.0.0", Feb_10th)
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )
      LeadTimeCalculator.calculateLeadTime(tags, releases, 1) shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }


    "calculate the rolling lead time for 9 months when provided tags and releases are not ordered" in new SetUp {

      val tags = List(
        RepoReleaseTag("8.0.0", Apr_4th),
        RepoReleaseTag("6.0.0", Mar_4th),
        RepoReleaseTag("7.0.0", Mar_27th),
        RepoReleaseTag("1.0.0", Feb_1st),
        RepoReleaseTag("2.0.0", Feb_4th),
        RepoReleaseTag("3.0.0", Feb_10th),
        RepoReleaseTag("4.0.0", Feb_16th),
        RepoReleaseTag("5.0.0", Feb_18th)

      )


      val releases = List(
        Release("7.0.0", Apr_1st), //   5 days
        Release("8.0.0", Apr_11th), //   7 days
        Release("1.0.0", Feb_4th), //  3 days
        Release("2.0.0", Feb_10th), //  6 days
        Release("3.0.0", Feb_16th), //  6 days
        Release("4.0.0", Feb_18th), //  2 days
        Release("5.0.0", Mar_1st), //   12 days
        Release("6.0.0", Mar_27th) //  23 days
      )


      LeadTimeCalculator.calculateLeadTime(tags, releases, 6) shouldBe List(
        LeadTimeResult(YearMonth.from(Dec_1st_2015), None),
        LeadTimeResult(YearMonth.from(Jan_1st), None),
        LeadTimeResult(YearMonth.from(Feb_1st), Some(5)),
        LeadTimeResult(YearMonth.from(Mar_1st), Some(6)),
        LeadTimeResult(YearMonth.from(Apr_1st), Some(6)),
        LeadTimeResult(YearMonth.from(May_1st), Some(6))
      )

    }
  }

}

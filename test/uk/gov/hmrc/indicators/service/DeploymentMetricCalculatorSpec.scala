/*
 * Copyright 2017 HM Revenue & Customs
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

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{LoneElement, Matchers, OptionValues, WordSpec}
import uk.gov.hmrc.indicators.DateHelper._
import uk.gov.hmrc.indicators.datasource.Deployment


class DeploymentMetricCalculatorSpec extends WordSpec with Matchers with TypeCheckedTripleEquals with LoneElement with OptionValues {


  object ResultExtractor {

    implicit class MeasureExtractor(deploymentsMetricResult: Seq[DeploymentsMetricResult]) {
      def leadTimes: Seq[(YearMonth, LocalDate, LocalDate, Option[Int])] = {
        deploymentsMetricResult.map { x =>

          (x.period, x.from, x.to, x.throughput.flatMap(_.leadTime).map(_.median))
        }
      }

      def intervals: Seq[(YearMonth, LocalDate, LocalDate, Option[Int])] = {
        deploymentsMetricResult.map { x =>

          (x.period, x.from, x.to, x.throughput.flatMap(_.interval).map(_.median))
        }
      }

      def stability: Seq[(YearMonth, LocalDate, LocalDate, Option[Double], Option[Int])] = {
        deploymentsMetricResult.map { x =>

          (x.period, x.from, x.to, x.stability.flatMap(_.hotfixRate), x.stability.flatMap(_.hotfixInterval.map(_.median)))

        }
      }

    }

  }


  trait SetUp {
    private val midNight: LocalTime = LocalTime.of(0, 0)

    val Oct_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 10, 1), midNight)
    val Nov_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 11, 1), midNight)
    val Nov_26th_2015 = LocalDateTime.of(LocalDate.of(2015, 11, 26), midNight)
    val Dec_1st_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)
    val Dec_2nd_2015 = LocalDateTime.of(LocalDate.of(2015, 12, 2), midNight)

    val Jan_1st = LocalDateTime.of(LocalDate.of(2016, 1, 1), midNight)
    val Jan_10th = LocalDateTime.of(LocalDate.of(2016, 1, 10), midNight)
    val Feb_1st = LocalDateTime.of(LocalDate.of(2016, 2, 1), midNight)
    val Feb_4th = LocalDateTime.of(LocalDate.of(2016, 2, 4), midNight)
    val Feb_6th = LocalDateTime.of(LocalDate.of(2016, 2, 6), midNight)
    val Feb_9th = LocalDateTime.of(LocalDate.of(2016, 2, 9), midNight)
    val Feb_10th = LocalDateTime.of(LocalDate.of(2016, 2, 10), midNight)
    val Feb_12th = LocalDateTime.of(LocalDate.of(2016, 2, 12), midNight)
    val Feb_16th = LocalDateTime.of(LocalDate.of(2016, 2, 16), midNight)
    val Feb_18th = LocalDateTime.of(LocalDate.of(2016, 2, 18), midNight)
    val Feb_20st = LocalDateTime.of(LocalDate.of(2016, 2, 20), midNight)
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
    val May_11th = LocalDateTime.of(LocalDate.of(2016, 5, 11), midNight)
    val June_1st = LocalDateTime.of(LocalDate.of(2016, 6, 1), midNight)
    val Jun_5th = LocalDateTime.of(LocalDate.of(2016, 6, 5), midNight)

    val Nov_2015 = YearMonth.of(2015, 11)
    val Dec_2015 = YearMonth.of(2015, 12)
    val Jan_2016 = YearMonth.of(2016, 1)
    val Feb_2016 = YearMonth.of(2016, 2)
    val Mar_2016 = YearMonth.of(2016, 3)
    val Apr_2016 = YearMonth.of(2016, 4)
    val May_2016 = YearMonth.of(2016, 5)
    val Jun_2016 = YearMonth.of(2016, 6)
    val July_2016 = YearMonth.of(2016, 7)
    val August_2016 = YearMonth.of(2016, 8)
    def clock = clockFrom(May_10th)

    def deploymentMetricCalculator = new DeploymentMetricCalculator(clock)

  }

  def deployment(name: String, creationDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None, version: String = "1.0.0"): Deployment = {
    Deployment(name, version, creationDate, leadTime, interval)
  }

  import ResultExtractor._


  private val serviceName: String = "test-service"

  "DeploymentMetricCalculator for stability" should {

    "calculates when there has been no deployment" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)
      val deployments = List()

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).stability shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, None, None))
    }

    "calculates hotfix rate based on (hotfix intervals only) when there has been some hotfix deployments" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)
      val deployments = List(
        deployment(serviceName, Feb_4th, version = "1.0.0"),
        deployment(serviceName, Feb_4th.plusDays(1), interval = Some(1), version = "1.0.1"),
        deployment(serviceName, Feb_4th.plusDays(2), interval = Some(1), version = "1.0.2"),
        deployment(serviceName, Feb_18th, version = "2.0.0"))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).stability shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(0.5), Some(1)))
    }

    "calculates hotfix rate when there has been no hotfix deployments" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)
      val deployments = List(
        deployment(serviceName, Feb_4th,  version = "1.0.0"),
        deployment(serviceName, Feb_4th.plusDays(1), version = "2.0.0"),
        deployment(serviceName, Feb_18th, interval = Some(12), version = "3.0.0"))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).stability shouldBe
        Seq((Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(0), None))
    }

    "calculates hotfix rate when there has been hotfixes and deployments in mulitple months" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val deployments = List(
        deployment("test-service", Feb_4th,  interval = Some(3), version = "3.1.1"),
        deployment("test-service", Feb_10th, interval = Some(6), version = "4.1.1"),
        deployment("test-service", Feb_16th, interval = Some(4), version = "5.1.1"),
        deployment("test-service", Feb_18th, interval = Some(2), version = "6.1.1"),
        deployment("test-service", Mar_1st, interval = Some(12), version = "7.1.1"),
        deployment("test-service", Mar_27th, interval = Some(23), version = "8.1.0"), // leadt times of hotfixes = 2,3,4,6,12 = 4 median

        deployment("test-service", Apr_1st, interval = Some(5), version = "1.1.1"),
        deployment("test-service", Apr_11th, interval = Some(7), version = "2.1.0"), // leadt times of hotfixes = 2,3,4,5,6,12 = 5 median
        deployment("test-service", May_11th, interval = Some(10), version = "9.1.1"), // lead times =   5, 7, 10, 12, 23
        deployment("test-service", Jun_5th, interval = Some(4), version = "10.1.0") // leadt times of hotfixes = 5 , 10 = 8 median
      )

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 7).stability shouldBe Seq(
        (Dec_2015, Oct_1st_2015.toLocalDate, toEndOfMonth(Dec_1st_2015), None, None),
        (Jan_2016, Nov_1st_2015.toLocalDate, toEndOfMonth(Jan_1st), None, None),
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(1.0), Some(4)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(0.83), Some(4)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(0.75), Some(5)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(0.60), Some(10)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(0.50), Some(8))
      )
    }

  }

  "deploymentMetricCalculator throughput leadtime" should {

    "calculate the correct median lead time for one tag and deployment in the same month 3 days apart" in new SetUp {
      override val clock: Clock = clockFrom(Feb_4th)


      val deploymentBucket = Seq(deployment(serviceName, Feb_4th, Some(3)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deploymentBucket, 1).leadTimes shouldBe
        Seq(
          (Feb_2016, Dec_1st_2015.toLocalDate, Feb_4th.toLocalDate, Some(3))
        )

    }

    "calculate the correct median lead time for two tags" in new SetUp {
      override val clock: Clock = clockFrom(Feb_16th)

      val deployments = Seq(deployment("test-service", Feb_4th, Some(3)), deployment("test-service", Feb_16th, Some(6)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).leadTimes shouldBe Seq(
        (YearMonth.from(Feb_1st), Dec_1st_2015.toLocalDate, Feb_16th.toLocalDate, Some(5))
      )
    }

    "calculate the correct median lead time for deployments that spans two months" in new SetUp {
      override val clock: Clock = clockFrom(Apr_10th)

      val deployments = Seq(deployment("test-service", Mar_4th, Some(3)), deployment("test-service", Apr_10th, Some(6)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 2).leadTimes shouldBe Seq(
        (YearMonth.from(Mar_1st), Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(3)),
        (YearMonth.from(Apr_1st), Feb_1st.toLocalDate, Apr_10th.toLocalDate, Some(5)))
    }

    "calculate the correct median lead time for 3 deployments" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val deployments = Seq(deployment("test-service", Feb_6th, Some(5)),
        deployment("test-service", Feb_12th, Some(6)),
        deployment("test-service", Feb_18th, Some(8)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).leadTimes shouldBe
        Seq(
          (YearMonth.from(Feb_1st), Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(6))
        )
    }

    "calculate the correct median lead time for 3 deployments with one missing tag date" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val deployments = Seq(deployment("test-service", Feb_6th, Some(5)),
        deployment("test-service", Feb_12th, None), // N/A
        deployment("test-service", Feb_20st, Some(8)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).leadTimes shouldBe Seq(
        (YearMonth.from(Feb_1st), Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(7))
      )
    }

    "calculate the correct median lead time for 4 deployments (3, 6, 6, 2)" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val deployments = Seq(deployment("test-service", Feb_4th, Some(3)),
        deployment("test-service", Feb_10th, Some(6)),
        deployment("test-service", Feb_16th, Some(6)),
        deployment("test-service", Feb_18th, Some(2))
      )

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).leadTimes shouldBe Seq(
        (YearMonth.from(Feb_1st), Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(5))
      )
    }

    "calculate the rolling lead time for 7 months (3 months sliding window) when provided tags and deployments are not ordered" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val deployments = List(
        deployment("test-service", Apr_1st, Some(5)),
        deployment("test-service", Apr_11th, Some(7)),
        deployment("test-service", Feb_4th, Some(3)),
        deployment("test-service", Feb_10th, Some(6)),
        deployment("test-service", Feb_16th, Some(6)),
        deployment("test-service", Feb_18th, Some(2)),
        deployment("test-service", Mar_1st, Some(12)),
        deployment("test-service", Mar_27th, Some(23)),
        deployment("test-service", May_11th, Some(10)),
        deployment("test-service", Jun_5th, Some(4))
      )

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 7).leadTimes shouldBe Seq(
        (Dec_2015, Oct_1st_2015.toLocalDate, toEndOfMonth(Dec_1st_2015), None),
        (Jan_2016, Nov_1st_2015.toLocalDate, toEndOfMonth(Jan_1st), None),
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(5)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(6)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(6)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(10)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(6))
      )
    }

    "calculate the median deployment lead time for 5 months (3 months sliding window) looking back 8 months" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val deployments = List(

        deployment("test-service", Nov_26th_2015, leadTime = Some(10)),
        deployment("test-service", Dec_2nd_2015, leadTime = Some(7)),
        deployment("test-service", Jan_10th, leadTime = Some(41)),

        deployment("test-service", Feb_4th, leadTime = Some(24)), // leadTime 24
        deployment("test-service", Feb_10th, leadTime = Some(6)), // leadTime 6
        deployment("test-service", Feb_16th, leadTime = Some(6)), // leadTime 6
        deployment("test-service", Feb_18th, leadTime = Some(2)), // leadTime 2
        deployment("test-service", Mar_1st, leadTime = Some(12)), //leadTime 12
        deployment("test-service", Mar_27th, leadTime = Some(26)), //leadTime 26
        deployment("test-service", Apr_1st, leadTime = Some(5)), //leadTime 5
        deployment("test-service", Apr_11th, leadTime = Some(10)), //leadTime 10
        deployment("test-service", May_11th, leadTime = Some(30)), //leadTime 30
        deployment("test-service", Jun_5th, leadTime = Some(25)) //leadTime 25
      )

      //dec None
      //jan None
      //feb 2,6,6,7,24,41   = 7
      //march 2,6,6,12,24,26,41 =12
      //april 2,6,6,24,12,26,5,10 => 2,5,6,6,10,12,24,26 = 8
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 5).leadTimes shouldBe Seq(
        (YearMonth.from(Feb_1st), Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(7)),
        (YearMonth.from(Mar_1st), Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(12)),
        (YearMonth.from(Apr_1st), Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(8)),
        (YearMonth.from(May_1st), Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(12)),
        (YearMonth.from(June_1st), Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(18)))
    }

  }

  "deploymentMetricCalculator throughput interval" should {

    "calculate the correct median deployment interval for deployment in the same month 3 days apart" in new SetUp {
      override val clock: Clock = clockFrom(Feb_4th)

      val deployments = Seq(deployment("test-service", Feb_4th))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).intervals shouldBe List(
        (Feb_2016, Dec_1st_2015.toLocalDate, Feb_4th.toLocalDate, None)
      )
    }

    "calculate the correct median deployment interval for two deployments" in new SetUp {

      val Jan_29th = LocalDateTime.of(LocalDate.of(2016, 1, 29), LocalTime.of(11, 16, 9))

      override val clock: Clock = clockFrom(Jan_29th)

      val Jan_7th = LocalDateTime.of(LocalDate.of(2016, 1, 7), LocalTime.of(12, 16, 3))
      val Jan_28th = LocalDateTime.of(LocalDate.of(2016, 1, 28), LocalTime.of(11, 16, 3))


      val deployments = Seq(deployment("test-service", Jan_7th), deployment("test-service", Jan_28th, interval = Some(21)))


      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).intervals shouldBe Seq(
        (Jan_2016, Nov_1st_2015.toLocalDate, Jan_29th.toLocalDate, Some(21))
      )
    }

    "calculate the correct median deployment interval for deployments that spans two months" in new SetUp {
      override val clock: Clock = clockFrom(Apr_10th)

      val deployments = Seq(deployment("test-service", Mar_4th), deployment("test-service", Apr_10th, interval = Some(37)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 2).intervals shouldBe Seq(
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), None),
        (Apr_2016, Feb_1st.toLocalDate, Apr_10th.toLocalDate, Some(37))
      )
    }

    "calculate the correct median deployment interval for 3 deployments" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      val deployments = Seq(deployment("test-service", Feb_4th),
        deployment("test-service", Feb_10th, interval = Some(6)),
        deployment("test-service", Feb_18th, interval = Some(8)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).intervals shouldBe List(
        (Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(7))
      )
    }

    "calculate the correct median deployment interval for 4 deployments (3, 6, 6, 2)" in new SetUp {
      override val clock: Clock = clockFrom(Feb_18th)

      //6,6,2
      val deployments = Seq(deployment("test-service", Feb_4th),
        deployment("test-service", Feb_10th, interval = Some(6)),
        deployment("test-service", Feb_16th, interval = Some(6)),
        deployment("test-service", Feb_18th, interval = Some(2)))

      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1).intervals shouldBe Seq(
        (Feb_2016, Dec_1st_2015.toLocalDate, Feb_18th.toLocalDate, Some(6))
      )
    }

    "calculate the median deployment interval for 7 months (3 months sliding window) when provided deployments are not ordered" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val deployments = List(
        deployment("test-service", May_11th, interval = Some(30)), //interval 30
        deployment("test-service", Mar_1st, interval = Some(12)), //interval 12
        deployment("test-service", Feb_10th, interval = Some(6)), // interval 6
        deployment("test-service", Feb_18th, interval = Some(2)), // interval 2
        deployment("test-service", Mar_27th, interval = Some(26)), //interval 26
        deployment("test-service", Apr_11th, interval = Some(10)), //interval 10
        deployment("test-service", Apr_1st, interval = Some(5)), //interval 5
        deployment("test-service", Feb_16th, interval = Some(6)), // interval 6
        deployment("test-service", Feb_4th, interval = None), // interval None
        deployment("test-service", Jun_5th, interval = Some(25)) //interval 25
      )

      //dec None
      //jan None
      //feb 2,6,6 = 6
      //march 2,6,6,12,26 =6
      //april 2,6,6,12,26,5,10 => 2,5,6,6,10,12,26 = 6
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 7).intervals shouldBe List(
        (Dec_2015, Oct_1st_2015.toLocalDate, toEndOfMonth(Dec_1st_2015), None),
        (Jan_2016, Nov_1st_2015.toLocalDate, toEndOfMonth(Jan_1st), None),
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(6)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(6)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(6)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(12)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(18))
      )
    }

    "calculate the median deployment interval for 5 months (3 months sliding window) looking back 8 months" in new SetUp {
      override val clock: Clock = clockFrom(Jun_5th)

      val deployments = List(

        deployment("test-service", Nov_26th_2015, interval = Some(10)),
        deployment("test-service", Dec_2nd_2015, interval = Some(7)),
        deployment("test-service", Jan_10th, interval = Some(41)),

        deployment("test-service", Feb_4th, interval = Some(24)), // interval 24
        deployment("test-service", Feb_10th, interval = Some(6)), // interval 6
        deployment("test-service", Feb_16th, interval = Some(6)), // interval 6
        deployment("test-service", Feb_18th, interval = Some(2)), // interval 2
        deployment("test-service", Mar_1st, interval = Some(12)), //interval 12
        deployment("test-service", Mar_27th, interval = Some(26)), //interval 26
        deployment("test-service", Apr_1st, interval = Some(5)), //interval 5
        deployment("test-service", Apr_11th, interval = Some(10)), //interval 10
        deployment("test-service", May_11th, interval = Some(30)), //interval 30
        deployment("test-service", Jun_5th, interval = Some(25)) //interval 25
      )

      //dec None
      //jan None
      //feb 2,6,6,7,24,41   = 7
      //march 2,6,6,12,24,26,41 =12
      //april 2,6,6,24,12,26,5,10 => 2,5,6,6,10,12,24,26 = 8
      //may 12,26,4,7,29 => 4,7,12,26,20 =12
      //june 5,10,30,25 => 5,10,25,30


      deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 5).intervals shouldBe List(
        (Feb_2016, Dec_1st_2015.toLocalDate, toEndOfMonth(Feb_1st), Some(7)),
        (Mar_2016, Jan_1st.toLocalDate, toEndOfMonth(Mar_1st), Some(12)),
        (Apr_2016, Feb_1st.toLocalDate, toEndOfMonth(Apr_1st), Some(8)),
        (May_2016, Mar_1st.toLocalDate, toEndOfMonth(May_1st), Some(12)),
        (Jun_2016, Apr_1st.toLocalDate, Jun_5th.toLocalDate, Some(18)))
    }
  }

  def toEndOfMonth(date: LocalDateTime): LocalDate = YearMonth.from(date).atEndOfMonth()
}

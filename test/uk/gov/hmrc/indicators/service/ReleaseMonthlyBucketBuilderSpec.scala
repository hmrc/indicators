/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime, LocalTime, YearMonth}

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.DateHelper._
import uk.gov.hmrc.indicators.datasource.Deployment

class DeploymentMonthlyBucketBuilderSpec extends WordSpec with Matchers {

  trait SetUp {
    private val midNight: LocalTime = LocalTime.of(0, 0)
    val Nov_2015: YearMonth         = YearMonth.of(2015, 11)
    val Dec_1st_2015                = LocalDateTime.of(LocalDate.of(2015, 12, 1), midNight)

    val Jan_1st  = LocalDateTime.of(LocalDate.of(2016, 1, 1), midNight)
    val Jan_10th = LocalDateTime.of(LocalDate.of(2016, 1, 10), midNight)

    val Feb_1st  = LocalDateTime.of(LocalDate.of(2016, 2, 1), midNight)
    val Feb_4th  = LocalDateTime.of(LocalDate.of(2016, 2, 4), midNight)
    val Feb_9th  = LocalDateTime.of(LocalDate.of(2016, 2, 9), midNight)
    val Feb_10th = LocalDateTime.of(LocalDate.of(2016, 2, 10), midNight)
    val Feb_16th = LocalDateTime.of(LocalDate.of(2016, 2, 16), midNight)
    val Feb_18th = LocalDateTime.of(LocalDate.of(2016, 2, 18), midNight)

    val Mar_1st    = LocalDateTime.of(LocalDate.of(2016, 3, 1), midNight)
    val Mar_4th    = LocalDateTime.of(LocalDate.of(2016, 3, 4), midNight)
    val March_10th = LocalDateTime.of(LocalDate.of(2016, 3, 10), midNight)
    val Mar_27th   = LocalDateTime.of(LocalDate.of(2016, 3, 27), midNight)

    val Apr_1st  = LocalDateTime.of(LocalDate.of(2016, 4, 1), midNight)
    val Apr_4th  = LocalDateTime.of(LocalDate.of(2016, 4, 4), midNight)
    val Apr_10th = LocalDateTime.of(LocalDate.of(2016, 4, 10), midNight)
    val Apr_11th = LocalDateTime.of(LocalDate.of(2016, 4, 11), midNight)
    val May_1st  = LocalDateTime.of(LocalDate.of(2016, 5, 1), midNight)
    val May_10th = LocalDateTime.of(LocalDate.of(2016, 5, 10), midNight)
    val May_11th = LocalDateTime.of(LocalDate.of(2016, 5, 11), midNight)
    val June_1st = LocalDateTime.of(LocalDate.of(2016, 6, 1), midNight)
    val June_5th = LocalDateTime.of(LocalDate.of(2016, 6, 5), midNight)

    val clock = clockFrom(May_10th)
  }

  def deployment(
    name: String,
    creationDate: LocalDateTime,
    leadTime: Option[Long] = None,
    interval: Option[Long] = None,
    version: String        = "version"): Deployment =
    Deployment(name, version, creationDate, leadTime, interval)

  "MonthlyDeploymentBucketBuilder" should {
    val serviceName = "test-service"

    "create YearMonthTimeSeries with deployment in each monthly buckets based on deployment date" in new SetUp {

      val deployments = List(
        deployment(serviceName, Apr_1st),
        deployment(serviceName, Apr_11th),
        deployment(serviceName, Feb_4th),
        deployment(serviceName, Feb_10th),
        deployment(serviceName, Feb_16th),
        deployment(serviceName, Feb_18th),
        deployment(serviceName, Mar_1st),
        deployment(serviceName, Mar_27th),
        deployment(serviceName, May_11th),
        deployment(serviceName, June_5th)
      )

      MonthlyBucketBuilder(deployments, 7)(_.productionDate)(clock).toSeq shouldBe Seq(
        (Nov_2015, Seq()),
        (YearMonth.from(Dec_1st_2015), Seq()),
        (YearMonth.from(Jan_1st), Seq()),
        (
          YearMonth.from(Feb_1st),
          Seq(
            deployment(serviceName, Feb_4th),
            deployment(serviceName, Feb_10th),
            deployment(serviceName, Feb_16th),
            deployment(serviceName, Feb_18th))),
        (YearMonth.from(Mar_1st), Seq(deployment(serviceName, Mar_1st), deployment(serviceName, Mar_27th))),
        (YearMonth.from(Apr_1st), Seq(deployment(serviceName, Apr_1st), deployment(serviceName, Apr_11th))),
        (YearMonth.from(May_1st), Seq(deployment(serviceName, May_11th)))
      )
    }
  }
}

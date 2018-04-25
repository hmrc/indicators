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

import java.time.{LocalDate, YearMonth}

import play.api.libs.json.{Json, Writes}

case class Throughput(leadTime: Option[MeasureResult], interval: Option[MeasureResult])

object Throughput {
  implicit val writes = Json.writes[Throughput]
}

case class Stability(hotfixRate: Option[Double], hotfixInterval: Option[MeasureResult])

object Stability {
  implicit val writes = Json.writes[Stability]
}

case class DeploymentsMetricResult(
  period: YearMonth,
  from: LocalDate,
  to: LocalDate,
  throughput: Option[Throughput],
  stability: Option[Stability])

object DeploymentsMetricResult {
  import uk.gov.hmrc.indicators.JavaDateTimeImplicits._
  implicit val writes = Json.writes[DeploymentsMetricResult]
}

case class MeasureResult(median: Int)

object MeasureResult {

  def toMeasureResult(median: BigDecimal): MeasureResult = new MeasureResult(Math.round(median.toDouble).toInt)

  implicit val measureResultWrites: Writes[MeasureResult] = Json.writes[MeasureResult]
}

abstract class MetricsResult {
  val period: YearMonth
  val from: LocalDate
  val to: LocalDate

  val median: Option[Int]
}

case class DeploymentLeadTimeResult(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[Int])
    extends MetricsResult

case class DeploymentIntervalResult(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[Int])
    extends MetricsResult

object DeploymentIntervalResult {

  def of(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[BigDecimal]): DeploymentIntervalResult =
    DeploymentIntervalResult(
      period = period,
      from   = from,
      to     = to,
      median = median.map(x => Math.round(x.toDouble).toInt))
}

object DeploymentLeadTimeResult {

  def of(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[BigDecimal]): DeploymentLeadTimeResult =
    DeploymentLeadTimeResult(
      period = period,
      from   = from,
      to     = to,
      median = median.map(x => Math.round(x.toDouble).toInt))
}

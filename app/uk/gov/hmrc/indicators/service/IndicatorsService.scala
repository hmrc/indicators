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

import java.time.{Clock, LocalDate, YearMonth}

import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.indicators.datasource._
import uk.gov.hmrc.indicators.service.ReleaseMetricCalculator._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.json.Json
import uk.gov.hmrc.indicators.JavaDateTimeImplicits._

case class Throughput(leadTime: Option[MeasureResult], interval: Option[MeasureResult])

object Throughput {
  implicit val writes = Json.writes[Throughput]
}

case class Stability(hotfixRate: Option[Double], hotfixLeadTime: Option[MeasureResult])

object Stability {
  implicit val writes = Json.writes[Stability]
}

case class DeploymentsMetricResult(period: YearMonth,
                                   from: LocalDate,
                                   to: LocalDate,
                                   throughput: Option[Throughput],
                                   stability: Option[Stability])

object DeploymentsMetricResult {

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


case class ReleaseLeadTimeResult(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[Int]) extends MetricsResult

case class ReleaseIntervalResult(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[Int]) extends MetricsResult

object ReleaseIntervalResult {

  def of(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[BigDecimal]): ReleaseIntervalResult =
    ReleaseIntervalResult(period = period, from = from, to = to, median = median.map(x => Math.round(x.toDouble).toInt))
}

object ReleaseLeadTimeResult {

  def of(period: YearMonth, from: LocalDate, to: LocalDate, median: Option[BigDecimal]): ReleaseLeadTimeResult =
    ReleaseLeadTimeResult(period = period, from = from, to = to, median = median.map(x => Math.round(x.toDouble).toInt))
}


class IndicatorsService(releasesDataSource: ReleasesDataSource, clock: Clock = Clock.systemUTC()) {

  implicit val c = clock

  def getDeploymentMetrics(serviceName: String, periodInMonths: Int = 9): Future[Option[Seq[DeploymentsMetricResult]]] =

    releasesDataSource.getForService(serviceName).map { releases =>

      Logger.debug(s"### Calculating production lead time for $serviceName , period : $periodInMonths months ###")
      Logger.info(s"Total production releases for :$serviceName total : ${releases.size}")

      Some(calculateDeploymentMetrics(releases, periodInMonths))

    }.recoverWith { case ex => Future.successful(None) }
}




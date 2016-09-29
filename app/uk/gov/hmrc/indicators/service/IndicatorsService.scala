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

case class ReleaseThroughputMetricResult(period: YearMonth,
                                         from: LocalDate,
                                         to: LocalDate,
                                         leadTime: Option[MeasureResult],
                                         interval: Option[MeasureResult])


object ReleaseThroughputMetricResult {

  implicit val writes = Json.writes[ReleaseThroughputMetricResult]

  def from(leadTimes: Seq[ReleaseLeadTimeResult], intervals: Seq[ReleaseIntervalResult]): Seq[ReleaseThroughputMetricResult] = {
    val ymToRLT = leadTimes.map(x => x.period -> x).toMap
    val ymToRI = intervals.map(x => x.period -> x).toMap

    val ymToReleaseLeadTime = leadTimes.map(x => x.period -> x.median).toMap
    val ymToReleaseInterval = intervals.map(x => x.period -> x.median).toMap

    (ymToReleaseLeadTime.keySet ++ ymToReleaseInterval.keySet).map { ym =>
      val leadTimeResult = ymToRLT.get(ym)
      val intervalResult = ymToRI.get(ym)
      val from = Seq(leadTimeResult.map(_.from), intervalResult.map(_.from)).flatten.head
      val to = Seq(leadTimeResult.map(_.to), intervalResult.map(_.to)).flatten.head

      ReleaseThroughputMetricResult(
        ym,
        from,
        to,
        leadTimeResult.flatMap(x => x.median.map(MeasureResult.apply)),
        intervalResult.flatMap(x => x.median.map(MeasureResult.apply))
      )
    }.toSeq.sortBy(_.period)
  }

}

case class ReleaseStabilityMetricResult(period: YearMonth,
                                        from: LocalDate,
                                        to: LocalDate,
                                        hotfixRate: HotFixRate,
                                        hotfixLeadTime: Option[MeasureResult])

object ReleaseStabilityMetricResult {
  implicit val writes = Json.writes[ReleaseStabilityMetricResult]

}

case class MeasureResult(median: Int)

object MeasureResult {

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

  def getReleaseThroughputMetrics(serviceName: String, periodInMonths: Int = 9): Future[Option[Seq[ReleaseThroughputMetricResult]]] =
    withReleases(serviceName) { releases =>
      Logger.debug(s"### Calculating production lead time for $serviceName , period : $periodInMonths months ###")
      Logger.info(s"Total production releases for :$serviceName total : ${releases.size}")

      Some(
        ReleaseThroughputMetricResult.from(
          calculateLeadTimeMetric(releases, periodInMonths),
          calculateReleaseIntervalMetric(releases, periodInMonths)))
    }


  def getReleaseStabilityMetrics(serviceName: String, periodInMonths: Int = 9): Future[Option[Seq[ReleaseStabilityMetricResult]]] =
    withReleases(serviceName)(releases => Some(calculateReleaseStabilityMetric(releases, periodInMonths)))


  private def withReleases[T](serviceName: String)(f: (Seq[Release] => Option[T])): Future[Option[T]] = {
    releasesDataSource.getForService(serviceName).map { releases =>

      f(releases)

    }.recoverWith { case ex => Future.successful(None) }
  }

}



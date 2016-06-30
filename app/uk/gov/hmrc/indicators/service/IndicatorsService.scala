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

import java.io.Serializable
import java.time.{Clock, YearMonth}

import com.sun.xml.internal.ws.developer.MemberSubmissionEndpointReference
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Writes}
import uk.gov.hmrc.indicators.JavaDateTimeJsonFormatter
import uk.gov.hmrc.indicators.datasource._
import uk.gov.hmrc.indicators.service.ReleaseMetricCalculator._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class FrequentReleaseMetricResult(period: YearMonth, leadTime: Option[MeasureResult], interval: Option[MeasureResult])

case class MeasureResult(median: Int)

object FrequentReleaseMetricResult {

  import play.api.libs.json.Json
  import JavaDateTimeJsonFormatter._

  implicit val measureResultWrites: Writes[MeasureResult] = Json.writes[MeasureResult]

  implicit val writes = Json.writes[FrequentReleaseMetricResult]

  def from(leadTimes: Seq[ReleaseLeadTimeResult], intervals: Seq[ReleaseIntervalResult]): Seq[FrequentReleaseMetricResult] = {
    val ymToReleaseLeadTime = leadTimes.map(x => x.period -> x.median).toMap
    val ymToReleaseInterval = intervals.map(x => x.period -> x.median).toMap

    (ymToReleaseLeadTime.keySet ++ ymToReleaseInterval.keySet).map { ym =>
      FrequentReleaseMetricResult(ym, ymToReleaseLeadTime.get(ym).flatten.map(x => MeasureResult(x)), ymToReleaseInterval.get(ym).flatten.map(x => MeasureResult(x)))
    }.toSeq.sortBy(_.period)
  }


}

abstract class MetricsResult {
  val period: YearMonth
  val median: Option[Int]
}

case class ReleaseLeadTimeResult(period: YearMonth, median: Option[Int]) extends MetricsResult

case class ReleaseIntervalResult(period: YearMonth, median: Option[Int]) extends MetricsResult

object ReleaseIntervalResult {

  def of(period: YearMonth, median: Option[BigDecimal]): ReleaseIntervalResult =
    ReleaseIntervalResult(period, median.map(x => Math.round(x.toDouble).toInt))


}

object ReleaseLeadTimeResult {

  import play.api.libs.json._

  import JavaDateTimeJsonFormatter._

  def of(period: YearMonth, median: Option[BigDecimal]): ReleaseLeadTimeResult =
    ReleaseLeadTimeResult(period, median.map(x => Math.round(x.toDouble).toInt))
}

class IndicatorsService(releasesDataSource: ReleasesDataSource, clock: Clock = Clock.systemUTC()) {
  implicit val c = clock

  def getFrequentReleaseMetric(serviceName: String, periodInMonths: Int = 9): Future[Option[Seq[FrequentReleaseMetricResult]]] =
    releasesDataSource.getForService(serviceName).map { releases =>
      Logger.debug(s"### Calculating production lead time for $serviceName , period : $periodInMonths months ###")
      Logger.info(s"Total production releases for :$serviceName total : ${releases.size}")

      Some(
        FrequentReleaseMetricResult.from(
          calculateLeadTimeMetric(releases, periodInMonths),
          calculateReleaseIntervalMetric(releases, periodInMonths)))
    }.recoverWith { case ex => Future.successful(None) }
}



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

import play.api.libs.json.Json
import uk.gov.hmrc.indicators.datasource.{Build, Deployment}

import scala.util.Try

object JobMetric {
  import uk.gov.hmrc.indicators.JavaDateTimeImplicits._
  implicit val writes = Json.writes[JobMetric]
}

case class JobMetric(period: YearMonth,
                     from: LocalDate,
                     to: LocalDate,
                     duration: Option[MeasureResult],
                     successRate: Option[Double])

class JobMetricCalculator(clock : Clock = Clock.systemUTC()) {

  implicit val c = clock

  type JobExecutionBucket = Iterable[(YearMonth, Seq[Deployment])]
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateJobMetrics(builds: Seq[Build], requiredPeriodInMonths: Int): Seq[JobMetric] = {
    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      val buildBuckets = getJobExecutionBuckets(builds, requiredMonths)

      buildBuckets.zipWithIndex.map { case (bucket, index) =>
        val dateData = DateData(buildBuckets.size, bucket, index)

        val allBuildsInBucket: Seq[Build] = bucket.flatMap(_._2).toSeq

        JobMetric(dateData.period, dateData.from, dateData.to, calculateMeasureResult(allBuildsInBucket, _.duration), calculateSuccessRate(allBuildsInBucket))
      }
    }
  }

  private def calculateSuccessRate(builds: Seq[Build]): Option[Double] = {

    def isBuildValid(build: Build) =
      build.result.isDefined && (build.result.contains("SUCCESS") || build.result.contains("FAILURE"))

    val buildResults = builds.filter(isBuildValid).flatMap(_.result)

    val successCount = buildResults.count(_ == "SUCCESS")

    buildResults.length match {
      case 0 => None
      case count => Some(successCount.toDouble / count.toDouble)
    }
  }

  private def calculateMeasureResult(builds: Seq[Build], measureReader: (Build) => Int): Option[MeasureResult] = {
    import IndicatorTraversable._

    val measures: Iterable[Int] = for {
      build <- builds
    } yield measureReader(build)

    measures.median.map(MeasureResult.toMeasureResult)
  }

  private def epochMillisToLocalDateTime(epochMillis: Long) = LocalDateTime.ofEpochSecond(epochMillis/1000, 0, ZoneOffset.UTC)

  private def getJobExecutionBuckets[T <: MetricsResult](builds: Seq[Build], requiredPeriod: Int) =
    MonthlyBucketBuilder(builds, requiredPeriod)(build => epochMillisToLocalDateTime(build.timestamp)).slidingWindow(monthlyWindowSize)

  private def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
  }

  private case class DateData(period: YearMonth, from: LocalDate, to: LocalDate)

  private object DateData {
    def apply(buildBucketSize: Int,
              bucket: Iterable[(YearMonth, Seq[Build])],
              indx: Int)(implicit clock: Clock): DateData = {

      val (period, _) = bucket.last
      val (from, _) = bucket.head

      val toDate =
        if (indx == (buildBucketSize - 1))
          period.atDay(LocalDate.now(clock).getDayOfMonth)
        else period.atEndOfMonth()

      val fromDate: LocalDate = from.atDay(1)

      DateData(period, fromDate, toDate)
    }
  }


}

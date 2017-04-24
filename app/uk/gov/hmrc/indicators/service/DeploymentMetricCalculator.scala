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

import uk.gov.hmrc.indicators.datasource.Deployment
import IndicatorTraversable._

import scala.math.BigDecimal.RoundingMode
import scala.util.Try



class DeploymentMetricCalculator(clock : Clock = Clock.systemUTC()) {

  implicit val c = clock

  type DeploymentBucket = Iterable[(YearMonth, Seq[Deployment])]
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateDeploymentMetrics(deployments: Seq[Deployment], requiredPeriodInMonths: Int): Seq[DeploymentsMetricResult] = {
    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      val deploymentBuckets = getDeploymentBuckets(deployments, requiredMonths)
      deploymentBuckets.zipWithIndex.map { case (bucket, index) =>
        val dateData = DateData(deploymentBuckets.size, bucket, index)

        val allDeploymentsInBucket: Seq[Deployment] = bucket.flatMap(_._2).toSeq
        DeploymentsMetricResult(dateData.period, dateData.from, dateData.to, calculateThroughputMetric(allDeploymentsInBucket), calculateDeploymentStabilityMetric(allDeploymentsInBucket))
      }

    }
  }

  private def calculateThroughputMetric(deployments: Seq[Deployment]): Option[Throughput] = {
    val leadTimeMetric = calculateMeasureResult(deployments, _.leadTime)
    val deploymentIntervalMetric = calculateMeasureResult(deployments, _.interval)
    (leadTimeMetric, deploymentIntervalMetric) match {
      case (None, None) => None
      case _ => Some(Throughput(leadTimeMetric, deploymentIntervalMetric))
    }
  }


  private def calculateMeasureResult(deployments: Seq[Deployment], measureReader: (Deployment) => Option[Long]): Option[MeasureResult] = {

    val measures: Iterable[Long] = for {
      deployment <- deployments
      measure <- measureReader(deployment)
    } yield measure

    measures.median.map(MeasureResult.toMeasureResult)
  }


  private def calculateDeploymentStabilityMetric(deployments: Seq[Deployment]): Option[Stability] = {

    val (baseDeployments, hotfixDeployments) = deployments.partition(_.version.endsWith(".0"))

    val hotfixRate = ratio(hotfixDeployments.size, deployments.size)

    val intervalMeasure: Option[MeasureResult] = calculateMeasureResult(hotfixDeployments, _.interval)

    (hotfixRate, intervalMeasure) match {
      case (None, None) => None
      case _ => Some(Stability(hotfixRate, intervalMeasure))
    }

  }


  def ratio(num: Int, denum: Int): Option[Double] = {
    if (denum == 0) None
    else {
      Some(BigDecimal(num.toDouble / denum).setScale(2, RoundingMode.HALF_UP).toDouble)
    }
  }

  private def getDeploymentBuckets[T <: MetricsResult](deployments: Seq[Deployment], requiredPeriod: Int): Seq[Iterable[(YearMonth, Seq[Deployment])]] = {
    val monthlyDeploymentIntervalBuckets: YearMonthTimeSeries[Deployment] = MonthlyBucketBuilder(deployments, requiredPeriod)(_.productionDate)

    val deploymentBuckets = monthlyDeploymentIntervalBuckets.slidingWindow(monthlyWindowSize)
    deploymentBuckets
  }

  private def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
  }

  private case class DateData(period: YearMonth, from: LocalDate, to: LocalDate)

  private object DateData {
    def apply(deploymentBucketSize: Int,
              bucket: Iterable[(YearMonth, Seq[Deployment])],
              indx: Int)(implicit clock: Clock): DateData = {

      val (period, _) = bucket.last
      val (from, _) = bucket.head

      val toDate =
        if (indx == (deploymentBucketSize - 1))
          period.atDay(LocalDate.now(clock).getDayOfMonth)
        else period.atEndOfMonth()

      val fromDate: LocalDate = from.atDay(1)

      DateData(period, fromDate, toDate)
    }
  }

}

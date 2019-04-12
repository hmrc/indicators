/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.indicators.datasource._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IndicatorsService @Inject()(
  serviceDeploymentsConnector: ServiceDeploymentsConnector,
  teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
  repositoryJobsConnector: RepositoryJobsConnector,
  deploymentMetricCalculator: DeploymentMetricCalculator,
  jobExecutionTimeMetricCalculator: JobMetricCalculator) {

  def getServiceDeploymentMetrics(serviceName: String, periodInMonths: Int = 12)(
    implicit hc: HeaderCarrier): Future[Option[Seq[DeploymentsMetricResult]]] =
    getServiceDeployments(serviceName)
      .map { deployments =>
        Logger.debug(s"### Calculating production lead time for $serviceName , period : $periodInMonths months ###")
        Logger.info(s"Total production deployments for :$serviceName total : ${deployments.size}")

        Some(deploymentMetricCalculator.calculateDeploymentMetrics(deployments, periodInMonths))

      }
      .recoverWith { case ex => Future.successful(None) }

  def getTeamDeploymentMetrics(teamName: String, periodInMonths: Int = 12)(
    implicit hc: HeaderCarrier): Future[Option[Seq[DeploymentsMetricResult]]] =
    teamsAndRepositoriesConnector
      .getServicesForTeam(teamName)
      .flatMap { services =>
        Future
          .traverse(services) { s =>
            getServiceDeployments(s).recoverWith { case _ => Future.successful(List()) }
          }
          .map(_.flatten)
          .map(getMetrics(periodInMonths))
      }
      .recoverWith { case ex => Future.successful(None) }

  private def getMetrics(periodInMonths: Int)(deployments: Seq[Deployment]) =
    deployments match {
      case Nil => None
      case l   => Some(deploymentMetricCalculator.calculateDeploymentMetrics(l, periodInMonths))
    }

  def getServiceDeployments(service: String)(implicit hc: HeaderCarrier): Future[List[Deployment]] =
    serviceDeploymentsConnector.getForService(service).map { rs =>
      val (invalidDeployments, validDeployments) = rs.partition(r => r.leadTime.isDefined && r.leadTime.exists(_ < 0))
      logInvalidDeployments(invalidDeployments)
      validDeployments
    }

  def getJobMetrics(repositoryName: String, periodInMonths: Int = 12)(
    implicit hc: HeaderCarrier): Future[Option[Seq[JobMetric]]] = {
    val repository = repositoryJobsConnector.getBuildsForRepository(repositoryName)

    repository
      .map { builds =>
        Some(jobExecutionTimeMetricCalculator.calculateJobMetrics(builds, periodInMonths))
      }
      .recoverWith { case _ => Future.successful(None) }
  }

  private def logInvalidDeployments(deployments: List[Deployment]) {
    Future {
      deployments.foreach(r =>
        Logger.warn(s"Invalid Deployment service:${r.name} version: ${r.version} leadTime : ${r.leadTime}"))
    }
  }

}

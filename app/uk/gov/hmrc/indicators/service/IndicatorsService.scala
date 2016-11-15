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

import play.api.Logger
import uk.gov.hmrc.indicators.datasource._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class IndicatorsService(releasesDataSource: ReleasesDataSource,
                        teamsAndRepositoriesDataSource: TeamsAndRepositoriesDataSource,
                        releaseMetricCalculator: ReleaseMetricCalculator) {


  def getServiceDeploymentMetrics(serviceName: String, periodInMonths: Int = 12): Future[Option[Seq[DeploymentsMetricResult]]] =

    getServiceReleases(serviceName).map { releases =>

      Logger.debug(s"### Calculating production lead time for $serviceName , period : $periodInMonths months ###")
      Logger.info(s"Total production releases for :$serviceName total : ${releases.size}")

      Some(releaseMetricCalculator.calculateDeploymentMetrics(releases, periodInMonths))

    }.recoverWith { case ex => Future.successful(None) }

  def getTeamDeploymentMetrics(teamName: String, periodInMonths: Int = 12): Future[Option[Seq[DeploymentsMetricResult]]] = {
    teamsAndRepositoriesDataSource.getServicesForTeam(teamName).flatMap { services =>
      Future.traverse(services) { s =>
        getServiceReleases(s).recoverWith { case _ => Future.successful(List()) }
      }
        .map(_.flatten)
        .map(getMetrics(periodInMonths))
    }.recoverWith { case ex => Future.successful(None) }
  }

  private def getMetrics(periodInMonths: Int)(releases: Seq[Release]) =
    releases match {
      case Nil => None
      case l => Some(releaseMetricCalculator.calculateDeploymentMetrics(l, periodInMonths))
    }

  def getServiceReleases(service: String): Future[List[Release]] = {
    releasesDataSource.getForService(service).map { rs =>
      val (invalidReleases, validReleases) = rs.partition(r => r.leadTime.isDefined && r.leadTime.exists(_ < 0))
      logInvalidReleases(invalidReleases)
      validReleases
    }
  }

  private def logInvalidReleases(releases: List[Release]) {
    Future {
      releases.foreach(r => Logger.warn(s"Invalid Release service:${r.name} version: ${r.version} leadTime : ${r.leadTime}"))
    }
  }


}




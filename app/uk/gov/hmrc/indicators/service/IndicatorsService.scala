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

import java.time.{LocalDate, YearMonth}

import uk.gov.hmrc.gitclient.{GitClient, GitTag}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class ProductionLeadTime(period : LocalDate, median : Option[BigDecimal])

object ReleasesPredicate {

  def apply(serviceName: String): (Release) => Boolean = {
    r => r.an == serviceName && (r.env.startsWith("production") || r.env.startsWith("prod"))
  }

}

class IndicatorsService(gitClient: GitClient, releasesClient: ReleasesConnector) {

  
  def getProductionDeploymentLeadTime(serviceName :String) : Future[List[ProductionLeadTime]]  = {

    for {
      tags <- gitClient.getGitRepoTags(serviceName, "HMRC")
      releases <- releasesClient.getAllReleases.map { r => r.filter(ReleasesPredicate(serviceName)) }
    } yield LeadTimeCalculator.calculateLeadTime(tags, releases)
  }
}



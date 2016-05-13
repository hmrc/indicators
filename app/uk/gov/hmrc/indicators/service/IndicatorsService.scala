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

import java.time.{Clock, LocalDate}

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.indicators.service.LeadTimeCalculator.calculateLeadTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ProductionLeadTime(period: LocalDate, median: Option[BigDecimal])

object ProductionLeadTime {
  import JavaDateTimeFormatters._
  implicit val formats = Json.format[ProductionLeadTime]
}

class IndicatorsService(tagsDataSource: TagsDataSource, releasesDataSource: ReleasesDataSource, clock: Clock = Clock.systemUTC()) {
  implicit val c = clock

  def getProductionDeploymentLeadTime(serviceName: String, periodInMonths: Int = 9): Future[List[ProductionLeadTime]] = {
    val repoTagsF: Future[List[RepoTag]] = tagsDataSource.getServiceRepoTags(serviceName, "HMRC")
    val releasesF: Future[List[Release]] = releasesDataSource.getAllReleases(serviceName)

    for {
      tags <- repoTagsF
      releases <- releasesF
    } yield {
      Logger.debug(s"---------calculation fpr for : $serviceName --------")
      calculateLeadTime(tags, releases, periodInMonths)
    }
  }
}



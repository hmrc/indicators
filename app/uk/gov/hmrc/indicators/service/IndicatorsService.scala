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

import java.time.{Clock, YearMonth}

import play.api.Logger

import uk.gov.hmrc.indicators.service.LeadTimeCalculator.calculateLeadTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class LeadTimeResult(period: YearMonth, median: Option[Long])

object LeadTimeResult {

  import play.api.libs.json.Json
  import JavaDateTimeJsonFormatter._

  implicit val writes = Json.writes[LeadTimeResult]

}

class IndicatorsService(tagsDataSource: TagsDataSource, releasesDataSource: ReleasesDataSource, clock: Clock = Clock.systemUTC()) {
  implicit val c = clock

  def getProductionDeploymentLeadTime(serviceName: String, periodInMonths: Int = 9): Future[List[LeadTimeResult]] = {
    val repoTagsF: Future[List[RepoTag]] = tagsDataSource.getServiceRepoTags(serviceName, "HMRC")
    val releasesF: Future[List[Release]] = releasesDataSource.getServiceReleases(serviceName)
    for {
      tags <- repoTagsF
      releases <- releasesF
    } yield {
      Logger.debug(s"### Calculating production lead time for ${serviceName} , period : $periodInMonths months ###")
      calculateLeadTime(tags, releases, periodInMonths)
    }
  }
}



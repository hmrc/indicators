/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.indicators.datasource

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails

import scala.concurrent.Future

class TeamsAndRepositoriesConnector @Inject()(
  httpClient: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment)
    extends ServicesConfig {

  val mode           = environment.mode
  val url            = baseUrl("teams-and-repositories")
  implicit val reads = Json.reads[Deployment]

  def getServicesForTeam(teamName: String)(implicit hc: HeaderCarrier): Future[List[String]] =
    httpClient.GET[HttpResponse](s"$url/api/teams/$teamName").map { response =>
      response.status match {
        case 200 => (response.json \ "Service").as[List[String]]
        case 404 => List.empty
      }
    }
}

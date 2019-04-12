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

package uk.gov.hmrc.indicators.datasource

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.indicators.JavaDateTimeImplicits
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

case class Deployment(
  name: String,
  version: String,
  productionDate: LocalDateTime,
  leadTime: Option[Long] = None,
  interval: Option[Long] = None)

@Singleton
class ServiceDeploymentsConnector @Inject()(
  httpClient: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment)(implicit ec: ExecutionContext)
    extends ServicesConfig {

  override protected def mode: Mode = environment.mode
  implicit val localDateTimeReads = JavaDateTimeImplicits.localDateTime
  implicit val reads                = Json.reads[Deployment]

  def getForService(serviceName: String)(implicit hc: HeaderCarrier): Future[List[Deployment]] =
    httpClient
      .GET[Option[List[Deployment]]](s"${baseUrl("service-deployments")}/api/deployments/$serviceName")
      .map(_.getOrElse(List.empty))
}

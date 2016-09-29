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

package uk.gov.hmrc.indicators.datasource

import java.time.LocalDateTime

import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.indicators.JavaDateTimeImplicits
import uk.gov.hmrc.indicators.http.HttpClient

import scala.concurrent.Future
import scala.concurrent.duration._

case class Release(name: String, version: String, productionDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None)

trait ReleasesDataSource {
  def getForService(serviceName: String): Future[List[Release]]
}

class ReleasesClient(releasesApiBase: String) extends ReleasesDataSource {

  import JavaDateTimeImplicits._

  implicit val reads = Json.reads[Release]

  def getForService(serviceName: String): Future[List[Release]] =
    HttpClient.get[List[Release]](s"$releasesApiBase/$serviceName")
}

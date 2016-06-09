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

import play.api.libs.json.Json
import uk.gov.hmrc.indicators.{JavaDateTimeJsonFormatter, FuturesCache}
import uk.gov.hmrc.indicators.http.HttpClient

import scala.concurrent.Future
import scala.concurrent.duration._


case class AppRelease(env: String, an: String, ver: String, fs: LocalDateTime)

object AppRelease {
  import JavaDateTimeJsonFormatter._
  implicit val format = Json.reads[AppRelease]
}

trait ReleasesClient {
  def getAllReleases: Future[List[AppRelease]]
}

class CachedAppReleasesClient(releasesClient: ReleasesClient) extends ReleasesClient with FuturesCache[String, List[AppRelease]] {

  override val refreshTimeInMillis: FiniteDuration = 3 hour

  def getAllReleases: Future[List[AppRelease]] = cache.getUnchecked("appReleases")

  def cacheLoader: (String) => Future[List[AppRelease]] = _ => releasesClient.getAllReleases
}

class AppReleasesClient(releasesApiBase: String) extends ReleasesClient {

  def getAllReleases: Future[List[AppRelease]] = HttpClient.get[List[AppRelease]](s"$releasesApiBase/apps")
}

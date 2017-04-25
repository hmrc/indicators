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

package uk.gov.hmrc.indicators.datasource

import play.api.libs.json.Json
import uk.gov.hmrc.indicators.http.HttpClient

import scala.concurrent.Future

case class Build(repositoryName: String,
                 jobName: String,
                 jobUrl: String,
                 buildNumber: Int,
                 result: Option[String],
                 timestamp: Long,
                 duration: Int,
                 buildUrl: String,
                 builtOn: String)

trait RepositoryJobsDataSource {
  def getBuildsForRepository(repositoryName: String): Future[Seq[Build]]
}

class RepositoryJobsConnector(repositoryJobsApiBase: String) extends RepositoryJobsDataSource {

  def getBuildsForRepository(repositoryName: String): Future[Seq[Build]] =  {
    implicit val reads = Json.reads[Build]
    HttpClient.get[List[Build]](s"$repositoryJobsApiBase/api/builds/$repositoryName")
  }

}

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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.indicators.http.HttpClient.getWithParsing
import uk.gov.hmrc.indicators.datasource.CatalogueServiceInfo.toServiceRepos

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed trait RepoType


object RepoType {


  case object Enterprise extends RepoType

  case object Open extends RepoType

  def repoTypeFor(st: String): Option[RepoType] = {
    st match {
      case "github" => Some(Enterprise)
      case "github-open" => Some(Open)
      case _ => {
        None
      }
    }
  }
}

case class ServiceRepositoryInfo(name: String, org: String, repoType: RepoType)


case class CatalogueServiceGitUrl(name: String, url: String)

object CatalogueServiceGitUrl {

  implicit val reads = Json.reads[CatalogueServiceGitUrl]
}

case class CatalogueServiceInfo(name: String, githubUrls: List[CatalogueServiceGitUrl])

object CatalogueServiceInfo {

  implicit val reads = Json.reads[CatalogueServiceInfo]

  val org = "^.*://.*(?<!/)/(.*)/.*(?<!/)$".r

  def toServiceRepos(cats: CatalogueServiceInfo): List[ServiceRepositoryInfo] = cats.githubUrls.flatMap { u =>
    toServiceRepo(cats.name, u.name, u.url)
  }

  private def extractOrg(url: String) = url match {
    case org(o) => Some(o)
    case _ => None
  }


  private def toServiceRepo(service: String, repoType: String, repoUrl: String) = {
    extractOrg(repoUrl).flatMap { org =>
      RepoType.repoTypeFor(repoType).map { typ =>
        ServiceRepositoryInfo(service, org, typ)
      }
    }
  }
}

trait CatalogueClient {

  def getServiceRepoInfo(serviceName: String): Future[Option[List[ServiceRepositoryInfo]]]

}

class CatalogueServiceClient(catalogueApiBase: String) extends CatalogueClient {

  override def getServiceRepoInfo(serviceName: String): Future[Option[List[ServiceRepositoryInfo]]] =

    getWithParsing(s"$catalogueApiBase/services")(toListOfCatalogueServiceInfo).map { serviceInfos =>

      serviceInfos.find(_.name == serviceName).map(toServiceRepos)
    }.recoverWith { case _ => Future.successful(None) }

  val toListOfCatalogueServiceInfo: (JsValue) => List[CatalogueServiceInfo] = jsV => (jsV \ "data").as[List[CatalogueServiceInfo]]
}

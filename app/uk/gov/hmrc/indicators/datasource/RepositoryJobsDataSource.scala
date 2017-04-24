package uk.gov.hmrc.indicators.datasource

import play.api.libs.json.Json
import uk.gov.hmrc.indicators.http.HttpClient

import scala.concurrent.Future

case class Build(repositoryName: String, jobName: String, jobUrl: String, buildNumber: Int, result: String,
                 timestamp: Long, duration: Int, buildUrl: String, builtOn: String)

trait RepositoryJobsDataSource {
  def getBuildsForRepository(repositoryName: String): Future[Seq[Build]]
}

class RepositoryJobsConnector(repositoryJobsApiBase: String) extends RepositoryJobsDataSource {

  def getBuildsForRepository(repositoryName: String): Future[Seq[Build]] =  {
    implicit val reads = Json.reads[Build]
    HttpClient.get[List[Build]](s"$repositoryJobsApiBase/builds/$repositoryName")
  }

}
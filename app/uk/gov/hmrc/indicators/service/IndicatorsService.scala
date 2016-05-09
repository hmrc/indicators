package uk.gov.hmrc.indicators.service

import java.time.{LocalDate, YearMonth}

import uk.gov.hmrc.gitclient.{GitClient, GitTag}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class ProductionLeadTime(period : LocalDate, median : Int )

class IndicatorsService(gitClient: GitClient, releasesClient: ReleasesClient) {

  def getProductionDeploymentLeadTime(serviceName :String) : Future[List[ProductionLeadTime]]  = {

    for {
      tags <- gitClient.getGitRepoTags(serviceName, "HMRC")
      releases <- releasesClient.getAllReleases(serviceName)
    } yield {
      List(ProductionLeadTime(releases.head.date, 3))
    }
  }
}

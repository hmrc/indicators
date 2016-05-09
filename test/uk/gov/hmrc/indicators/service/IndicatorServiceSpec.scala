package uk.gov.hmrc.indicators.service

import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.gitclient.{GitClient, GitTag}
import java.time.{LocalDate, LocalDateTime}
import java.util.TimeZone

import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndicatorServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures {

  val gitClient = mock[GitClient]
  val releasesClient = mock[ReleasesClient]

  val indicatorsService = new IndicatorsService(gitClient, releasesClient)

  val Feb_1st = LocalDate.of(2000, 2, 1).atStartOfDay().atZone(TimeZone.getDefault().toZoneId)
  val Feb_4th = LocalDate.of(2000, 2, 4)

  "IndicatorService getProductionDeploymentLeadTime" should {
    "calculates production deployment lead time" in {

      val tags = List(
        GitTag("1.0.0", Some(Feb_1st))
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      Mockito.when(gitClient.getGitRepoTags("test-service", "HMRC")).thenReturn(Future.successful(tags))
      Mockito.when(releasesClient.getAllReleases("test-service")).thenReturn(Future.successful(releases))

      indicatorsService.getProductionDeploymentLeadTime("test-service")
        .futureValue shouldBe List(ProductionLeadTime(Feb_4th, 3))
    }
  }
}

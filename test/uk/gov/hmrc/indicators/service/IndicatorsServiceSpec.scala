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

import java.time.{LocalDateTime, YearMonth}

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.indicators.datasource._
import uk.gov.hmrc.indicators.{DateHelper, DefaultPatienceConfig}

import scala.concurrent.Future


class IndicatorsServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig {

  val tagsDataSource = mock[ServiceReleaseTagDataSource]
  val releasesClient = mock[ReleasesDataSource]
  val catalogueClient = mock[CatalogueClient]

  val Feb_1st = LocalDateTime.of(2000, 2, 1, 0, 0, 0)
  val Feb_4th = LocalDateTime.of(2000, 2, 4, 0, 0, 0)
  val fixedClock = DateHelper.clockFrom(Feb_1st)

  val indicatorsService = new IndicatorsService(tagsDataSource, releasesClient, catalogueClient, fixedClock)


  "IndicatorService getProductionDeploymentLeadTime" should {
    "calculates production deployment lead time" in {

      val tags = List(
        ServiceReleaseTag("1.0.0", Feb_1st)
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      val serviceRepoInfo: ServiceRepositoryInfo = ServiceRepositoryInfo("test-service", "HMRC", RepoType.Enterprise)

      Mockito.when(catalogueClient.getServiceRepoInfo("test-service")).thenReturn(Future.successful(Some(List(serviceRepoInfo))))
      Mockito.when(tagsDataSource.getServiceRepoReleaseTags(serviceRepoInfo)).thenReturn(Future.successful(tags))
      Mockito.when(releasesClient.getServiceReleases("test-service")).thenReturn(Future.successful(releases))

      indicatorsService.getProductionDeploymentLeadTime("test-service", 1).futureValue.get shouldBe List(LeadTimeResult(YearMonth.from(Feb_1st), Some(3)))
    }

    "returns None if the service is not found" in {


      val serviceRepoInfo: ServiceRepositoryInfo = ServiceRepositoryInfo("test-service", "HMRC", RepoType.Enterprise)

      Mockito.when(catalogueClient.getServiceRepoInfo("test-service")).thenReturn(Future.successful(None))

      indicatorsService.getProductionDeploymentLeadTime("test-service", 1).futureValue shouldBe None
    }

  }
}

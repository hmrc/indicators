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

import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.gitclient.{GitClient, GitTag}
import java.time.{LocalDate, LocalDateTime}
import java.util.TimeZone

import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndicatorsServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures {

  val tagsDataSource = mock[TagsDataSource]
  val releasesClient = mock[ReleasesDataSource]

  val indicatorsService = new IndicatorsService(tagsDataSource, releasesClient)

  val Feb_1st = LocalDate.of(2000, 2, 1).atStartOfDay().atZone(TimeZone.getDefault().toZoneId)
  val Feb_4th = LocalDate.of(2000, 2, 4)

  "IndicatorService getProductionDeploymentLeadTime" should {
    "calculates production deployment lead time" in {

      val tags = List(
        RepoTag("1.0.0", Some(Feb_1st))
      )

      val releases = List(
        Release("1.0.0", Feb_4th)
      )

      Mockito.when(tagsDataSource.getServiceRepoTags("test-service", "HMRC")).thenReturn(Future.successful(tags))
      Mockito.when(releasesClient.getAllReleases("test-service")).thenReturn(Future.successful(releases))

      indicatorsService.getProductionDeploymentLeadTime("test-service").futureValue shouldBe List(ProductionLeadTime(Feb_4th, Some(3)))
    }
  }
}

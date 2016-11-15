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

import java.time.{LocalDate, LocalDateTime, YearMonth}

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpec}
import uk.gov.hmrc.indicators.datasource.{TeamsAndRepositoriesDataSource, _}
import uk.gov.hmrc.indicators.{DateHelper, DefaultPatienceConfig}

import scala.concurrent.Future


class IndicatorsServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig with OptionValues {

  trait SetUp {


    val releasesClient = mock[ReleasesDataSource]
    val teamsAndRepositoriesDataSource = mock[TeamsAndRepositoriesDataSource]
    val releaseMetricCalculator = mock[ReleaseMetricCalculator]

    val Feb_1st = LocalDateTime.of(2000, 2, 1, 0, 0, 0)
    val Feb_4th = LocalDateTime.of(2000, 2, 4, 0, 0, 0)
    val Feb_5th = LocalDateTime.of(2000, 2, 5, 0, 0, 0)
    val Feb_6th = LocalDateTime.of(2000, 2, 6, 0, 0, 0)

    val Feb_18th = LocalDateTime.of(2000, 2, 18, 0, 0, 0)
    val now = DateHelper.clockFrom(Feb_18th)

    val indicatorsService = new IndicatorsService(releasesClient, teamsAndRepositoriesDataSource, releaseMetricCalculator)
  }

  val serviceName = "test-service"


  def release(name: String, creationDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None, version: String = "version"): Release = {
    Release(name, version, creationDate, leadTime, interval)
  }

  val deploymentsMetricResult: DeploymentsMetricResult = mock[DeploymentsMetricResult]

  "IndicatorService" should {

    "calculates DeploymentsMetricResult for a service" in new SetUp {
      val releases = List(
        release(serviceName, Feb_4th, leadTime = Some(3)),
        release(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))
      when(releaseMetricCalculator.calculateDeploymentMetrics(releases, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "calculates DeploymentsMetricResult ignoring releases with negative leadTime" in new SetUp {
      val correctReleases = release(serviceName, Feb_4th, leadTime = Some(3))
      val releases = List(
        correctReleases,
        release(serviceName, Feb_6th, leadTime = Some(-1), interval = Some(2)))

      when(releasesClient.getForService("test-service")).thenReturn(Future.successful(releases))
      when(releaseMetricCalculator.calculateDeploymentMetrics(List(correctReleases), 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "returns None if the service is not found" in new SetUp {
      when(releasesClient.getForService("test-service")).thenReturn(Future.failed(new RuntimeException("404")))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue shouldBe None
    }

    "calculates DeploymentsMetricResult for a team" in new SetUp {
      val releases1 = List(
        release(serviceName, Feb_4th, leadTime = Some(3)),
        release(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val releases2 = List(
        release(serviceName, Feb_4th, leadTime = Some(5)),
        release(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2")))

      when(releasesClient.getForService("Service1")).thenReturn(Future.successful(releases1))
      when(releasesClient.getForService("Service2")).thenReturn(Future.successful(releases2))
      when(releaseMetricCalculator.calculateDeploymentMetrics(releases1 ++ releases2, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }

    "calculates DeploymentsMetricResult for a team ignoring services with no releases" in new SetUp {
      val releases1 = List(
        release(serviceName, Feb_4th, leadTime = Some(3)),
        release(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val releases2 = List(
        release(serviceName, Feb_4th, leadTime = Some(5)),
        release(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2", "Service3")))

      when(releasesClient.getForService("Service1")).thenReturn(Future.successful(releases1))
      when(releasesClient.getForService("Service2")).thenReturn(Future.successful(releases2))
      when(releasesClient.getForService("Service3")).thenReturn(Future.failed(new RuntimeException))
      when(releaseMetricCalculator.calculateDeploymentMetrics(releases1 ++ releases2, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "calculates DeploymentsMetricResult for a team ignoring releases with negative lead time" in new SetUp {
      val releases1 = List(
        release(serviceName, Feb_4th, leadTime = Some(3)),
        release(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val releases2 = List(
        release(serviceName, Feb_4th, leadTime = Some(5)),
        release(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2", "Service3")))

      when(releasesClient.getForService("Service1")).thenReturn(Future.successful(releases1))
      when(releasesClient.getForService("Service2")).thenReturn(Future.successful(releases2))
      when(releasesClient.getForService("Service3")).thenReturn(Future.successful(List(release(serviceName, Feb_4th, leadTime = Some(-5)))))
      when(releaseMetricCalculator.calculateDeploymentMetrics(releases1 ++ releases2, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "returns None if the team has no throughput or stability indicators" in new SetUp {

      val noMetricResult = Seq(
        DeploymentsMetricResult(
          YearMonth.of(2015, 12),
          LocalDate.of(2015, 10, 1),
          LocalDate.of(2015, 12, 31),
          None,
          None
        ),
        DeploymentsMetricResult(
          YearMonth.of(2016, 1),
          LocalDate.of(2015, 11, 1),
          LocalDate.of(2016, 1, 31),
          None,
          None
        )
      )

      val teamServices = List("service1", "service2")

      val emptyReleases = List()


      when(releasesClient.getForService("service1")).thenReturn(Future.successful(emptyReleases))
      when(releasesClient.getForService("service2")).thenReturn(Future.successful(emptyReleases))
      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(teamServices))
      when(releaseMetricCalculator.calculateDeploymentMetrics(emptyReleases, 1)).thenReturn(noMetricResult)

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue shouldBe None
    }
  }
}

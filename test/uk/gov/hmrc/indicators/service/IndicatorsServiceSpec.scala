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

package uk.gov.hmrc.indicators.service

import java.time.{LocalDate, LocalDateTime, YearMonth, ZoneOffset}

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpec}
import uk.gov.hmrc.indicators.datasource.{TeamsAndRepositoriesDataSource, _}
import uk.gov.hmrc.indicators.{DateHelper, DefaultPatienceConfig}

import scala.collection.immutable
import scala.concurrent.Future


class IndicatorsServiceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig with OptionValues {

  trait SetUp {


    val deploymentsClient = mock[DeploymentsDataSource]
    val repositoryJobsDataSource = mock[RepositoryJobsDataSource]
    val teamsAndRepositoriesDataSource = mock[TeamsAndRepositoriesDataSource]
    val deploymentMetricCalculator = mock[DeploymentMetricCalculator]
    val jobExecutionTimeMetricCalculator = mock[JobMetricCalculator]

    val Feb_1st = LocalDateTime.of(2000, 2, 1, 0, 0, 0)
    val Feb_4th = LocalDateTime.of(2000, 2, 4, 0, 0, 0)
    val Feb_5th = LocalDateTime.of(2000, 2, 5, 0, 0, 0)
    val Feb_6th = LocalDateTime.of(2000, 2, 6, 0, 0, 0)

    val Feb_18th = LocalDateTime.of(2000, 2, 18, 0, 0, 0)
    val now = DateHelper.clockFrom(Feb_18th)

    val indicatorsService = new IndicatorsService(
      deploymentsClient,
      teamsAndRepositoriesDataSource,
      repositoryJobsDataSource,
      deploymentMetricCalculator,
      jobExecutionTimeMetricCalculator)
  }

  val serviceName = "test-service"


  def deployment(name: String, creationDate: LocalDateTime, leadTime: Option[Long] = None, interval: Option[Long] = None, version: String = "version"): Deployment = {
    Deployment(name, version, creationDate, leadTime, interval)
  }

  val deploymentsMetricResult = mock[DeploymentsMetricResult]
  val jobExecutionTimeMetricResult = mock[JobMetric]

  "IndicatorService" should {

    "calculates DeploymentsMetricResult for a service" in new SetUp {
      val deployments = List(
        deployment(serviceName, Feb_4th, leadTime = Some(3)),
        deployment(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      when(deploymentsClient.getForService("test-service")).thenReturn(Future.successful(deployments))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(deployments, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "calculates DeploymentsMetricResult ignoring deployments with negative leadTime" in new SetUp {
      val correctDeployments = deployment(serviceName, Feb_4th, leadTime = Some(3))
      val deployments = List(
        correctDeployments,
        deployment(serviceName, Feb_6th, leadTime = Some(-1), interval = Some(2)))

      when(deploymentsClient.getForService("test-service")).thenReturn(Future.successful(deployments))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(List(correctDeployments), 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "returns None if the service is not found" in new SetUp {
      when(deploymentsClient.getForService("test-service")).thenReturn(Future.failed(new RuntimeException("404")))

      indicatorsService.getServiceDeploymentMetrics("test-service", 1).futureValue shouldBe None
    }

    "calculates DeploymentsMetricResult for a team" in new SetUp {
      val deployments1 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(3)),
        deployment(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val deployments2 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(5)),
        deployment(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2")))

      when(deploymentsClient.getForService("Service1")).thenReturn(Future.successful(deployments1))
      when(deploymentsClient.getForService("Service2")).thenReturn(Future.successful(deployments2))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(deployments1 ++ deployments2, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }

    "calculates DeploymentsMetricResult for a team ignoring services with no deployments" in new SetUp {
      val deployments1 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(3)),
        deployment(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val deployments2 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(5)),
        deployment(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2", "Service3")))

      when(deploymentsClient.getForService("Service1")).thenReturn(Future.successful(deployments1))
      when(deploymentsClient.getForService("Service2")).thenReturn(Future.successful(deployments2))
      when(deploymentsClient.getForService("Service3")).thenReturn(Future.failed(new RuntimeException))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(deployments1 ++ deployments2, 1)).thenReturn(Seq(deploymentsMetricResult))

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue.value shouldBe
        List(deploymentsMetricResult)
    }


    "calculates DeploymentsMetricResult for a team ignoring deployments with negative lead time" in new SetUp {
      val deployments1 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(3)),
        deployment(serviceName, Feb_6th, leadTime = Some(1), interval = Some(2)))

      val deployments2 = List(
        deployment(serviceName, Feb_4th, leadTime = Some(5)),
        deployment(serviceName, Feb_6th, leadTime = Some(6), interval = Some(7)))

      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(List("Service1", "Service2", "Service3")))

      when(deploymentsClient.getForService("Service1")).thenReturn(Future.successful(deployments1))
      when(deploymentsClient.getForService("Service2")).thenReturn(Future.successful(deployments2))
      when(deploymentsClient.getForService("Service3")).thenReturn(Future.successful(List(deployment(serviceName, Feb_4th, leadTime = Some(-5)))))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(deployments1 ++ deployments2, 1)).thenReturn(Seq(deploymentsMetricResult))

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

      val emptyDeployments = List()


      when(deploymentsClient.getForService("service1")).thenReturn(Future.successful(emptyDeployments))
      when(deploymentsClient.getForService("service2")).thenReturn(Future.successful(emptyDeployments))
      when(teamsAndRepositoriesDataSource.getServicesForTeam("teamA")).thenReturn(Future.successful(teamServices))
      when(deploymentMetricCalculator.calculateDeploymentMetrics(emptyDeployments, 1)).thenReturn(noMetricResult)

      indicatorsService.getTeamDeploymentMetrics("teamA", 1).futureValue shouldBe None
    }

    "delegate to jobExecutionTimeMetricCalculator for calculating JobMetric for a repository" in new SetUp {
      private val repoName = "test-repo"
      val builds: immutable.Seq[Build] = List(
        build(repoName, Feb_4th.toEpochSecond(ZoneOffset.UTC), 3),
        build(repoName, Feb_6th.toEpochSecond(ZoneOffset.UTC), 6))

      when(repositoryJobsDataSource.getBuildsForRepository(repoName)).thenReturn(Future.successful(builds))
      when(jobExecutionTimeMetricCalculator.calculateJobMetrics(builds, 1)).thenReturn(Seq(jobExecutionTimeMetricResult))

      indicatorsService.getJobMetrics(repoName, 1).futureValue.value shouldBe
        List(jobExecutionTimeMetricResult)
    }
  }

  def build(repoName: String, epochSecond: Long, duration: Int) =
    Build(repoName, "jobName", "jobUrl", 1234, Some("SUCCESS"), epochSecond, duration, "some.url", "slave-1")


}

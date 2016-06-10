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

import java.time.{ZoneId, ZoneOffset, LocalDateTime}
import java.util.Date

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.githubclient.{GhRepoRelease, GithubApiClient}

import scala.concurrent.Future


class GitHubReleaseTagDataSourceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val githubApiClient: GithubApiClient = mock[GithubApiClient]

  private val releaseTagsDataSource: GitHubReleaseTagDataSource = new GitHubReleaseTagDataSource(githubApiClient)


  "getServiceRepoReleaseTags" should {
    "get repo release tags from git hub releases" in {

      val now: LocalDateTime = LocalDateTime.now()

      val releases: List[GhRepoRelease] = List(
        GhRepoRelease(123, "releases/1.9.0", Date.from(now.atZone(ZoneId.systemDefault()).toInstant))
      )
      Mockito.when(githubApiClient.getReleases("OrgA", "repoA")(BlockingIOExecutionContext.executionContext)).thenReturn(Future.successful(releases))

      val tags: Future[List[ServiceReleaseTag]] = releaseTagsDataSource.getServiceRepoReleaseTags(ServiceRepositoryInfo("repoA", "OrgA", RepoType.Open))

      tags.futureValue shouldBe List(ServiceReleaseTag("1.9.0", now))
    }
  }


}

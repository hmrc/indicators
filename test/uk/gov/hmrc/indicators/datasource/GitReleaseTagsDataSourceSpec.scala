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

import java.time.ZonedDateTime

import jdk.nashorn.internal.parser.TokenKind
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec, FunSuite}
import uk.gov.hmrc.gitclient.{GitTag, GitClient}
import uk.gov.hmrc.indicators.DefaultPatienceConfig

import scala.concurrent.Future


class GitReleaseTagsDataSourceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig{

  val gitClient = mock[GitClient]
  val dataSource = new GitReleaseTagsDataSource(gitClient)


  "GitTagsDataSource.getServiceRepoTags" should {
    "return tags form gitClient with normalized tag name (i.e just the numbers)" in {


      val now: ZonedDateTime = ZonedDateTime.now()

      val serviceRepoInfo = ServiceRepositoryInfo("repoName", "HMRC", RepoType.Enterprise)

      when(gitClient.getGitRepoTags("repoName", "HMRC")(BlockingIOExecutionContext.executionContext)).thenReturn(Future.successful(List(
        GitTag("v1.0.0", Some(now)),
        GitTag("release/9.101.0", Some(now)),
        GitTag("someRandomtagName", Some(now))
      )))

      dataSource.getServiceRepoReleaseTags(serviceRepoInfo).futureValue shouldBe List(
        RepoReleaseTag("1.0.0", now.toLocalDateTime),
        RepoReleaseTag("9.101.0", now.toLocalDateTime),
        RepoReleaseTag("someRandomtagName", now.toLocalDateTime)
      )

    }

    "return only tags with createdAt defined form gitClient " in {


      val now: ZonedDateTime = ZonedDateTime.now()

      val serviceRepoInfo = ServiceRepositoryInfo("repoName", "HMRC", RepoType.Enterprise)

      when(gitClient.getGitRepoTags("repoName", "HMRC")(BlockingIOExecutionContext.executionContext)).thenReturn(Future.successful(List(
        GitTag("v1.0.0", None),
        GitTag("release/9.101.0", Some(now)),
        GitTag("someRandomtagName", None)
      )))

      dataSource.getServiceRepoReleaseTags(serviceRepoInfo).futureValue shouldBe List(
        RepoReleaseTag("9.101.0", now.toLocalDateTime)
      )

    }


  }

}

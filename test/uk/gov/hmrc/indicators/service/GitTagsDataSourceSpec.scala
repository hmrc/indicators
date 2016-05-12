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

import java.time.ZonedDateTime

import jdk.nashorn.internal.parser.TokenKind
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec, FunSuite}
import uk.gov.hmrc.gitclient.{GitTag, GitClient}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GitTagsDataSourceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures {

  val gitClient = mock[GitClient]
  val dataSource = new GitTagsDataSource(gitClient)


  "GitTagsDataSource.getServiceRepoTags" should {
    "return tags form gitClient with normalized tag name (i.e just the numbers)" in {

      val now: ZonedDateTime = ZonedDateTime.now()
      when(gitClient.getGitRepoTags("repoName", "HMRC")).thenReturn(Future.successful(List(
        GitTag("v1.0.0", Some(now)),
        GitTag("release/9.101.0", Some(now)),
        GitTag("someRandomtagName", Some(now))
      )))

      dataSource.getServiceRepoTags("repoName", "HMRC").futureValue shouldBe List(
        RepoTag("1.0.0", Some(now)),
        RepoTag("9.101.0", Some(now)),
        RepoTag("someRandomtagName", Some(now))
      )


    }
  }

}

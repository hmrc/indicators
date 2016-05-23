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
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class CompositeReleaseTagsDataSourceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures{

  trait SetUp {
    val gitEnterpriseTagDataSource = mock[ReleaseTagsDataSource]

    val gitOpenTagDataSource = mock[ReleaseTagsDataSource]


    val compositeTagsSource = new CompositeReleaseTagsDataSource(gitEnterpriseTagDataSource, gitOpenTagDataSource)
  }


  "getServiceRepoTags" should {
    "use enterprise data source if RepoType is Enterprise" in new SetUp {

      val serviceRepoInfo = ServiceRepositoryInfo("service", "org", RepoType.Enterprise)
      private val enterpriseRepoTags: List[RepoReleaseTag] = List(RepoReleaseTag("E", None))

      when(gitEnterpriseTagDataSource.getServiceRepoReleaseTags(serviceRepoInfo)).thenReturn(Future.successful(enterpriseRepoTags))

      val tags = compositeTagsSource.getServiceRepoReleaseTags(ServiceRepositoryInfo("service", "org", RepoType.Enterprise))

      tags.futureValue shouldBe enterpriseRepoTags

      Mockito.verifyZeroInteractions(gitOpenTagDataSource)

    }

    "use open data source if RepoType is Open" in new SetUp {

      val serviceRepoInfo = ServiceRepositoryInfo("service", "org", RepoType.Open)

      private val repoTags: List[RepoReleaseTag] = List(RepoReleaseTag("E", None))

      when(gitOpenTagDataSource.getServiceRepoReleaseTags(serviceRepoInfo)).thenReturn(Future.successful(repoTags))

      val tags = compositeTagsSource.getServiceRepoReleaseTags(ServiceRepositoryInfo("service", "org", RepoType.Open))

      tags.futureValue shouldBe repoTags

      Mockito.verifyZeroInteractions(gitEnterpriseTagDataSource)

    }

  }

}

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

import java.time.{LocalDateTime, LocalDate}

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

import scala.concurrent.Future

class CachedReleaseTagsDataSourceSpec extends WordSpec with Matchers with ScalaFutures with MockitoSugar {

  val tagsDataSource = mock[ReleaseTagsDataSource]
  val cachedDataSource = new CachedReleaseTagsDataSource(tagsDataSource) {
    override val refreshTimeInMillis = 1000.millis
  }

  "getServiceRepoTags" should {
    "load from the releases client and also cache the values" in {

      val result = List(RepoReleaseTag("tag1", LocalDateTime.now()))

      val serviceRepo = ServiceRepositoryInfo("repoName", "owner", RepoType.Enterprise)

      when(tagsDataSource.getServiceRepoReleaseTags(serviceRepo)).thenReturn(Future.successful(result))

      cachedDataSource.getServiceRepoReleaseTags(serviceRepo).futureValue should be(result)

      cachedDataSource.cache.get(serviceRepo) shouldBe result

      verify(tagsDataSource, times(1)).getServiceRepoReleaseTags(serviceRepo)
    }


  }
}

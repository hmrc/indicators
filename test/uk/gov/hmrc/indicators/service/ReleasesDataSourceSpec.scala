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

import java.time.{LocalDateTime, LocalDate}

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.gitclient.GitClient
import uk.gov.hmrc.indicators.DefaultPatienceConfig

import scala.concurrent.Future

class AppReleasesDataSourceSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with DefaultPatienceConfig {
  
  val releasesClient = mock[AppReleasesClient]

  val dataSource = new AppReleasesDataSource(releasesClient)
  val now = LocalDateTime.now()
  
  "AppReleasesDataSource.getAllReleases(serviceName)" should {
    
    "give all releases for a given service in production" in {
      Mockito.when(releasesClient.getAllReleases).thenReturn(Future.successful(
      List(
      AppRelease("production","some-serviceName","1.0",now),
      AppRelease("prod","some-serviceName","2.0",now),
      AppRelease("production","some-other-ServiceName","1.0",now)
      )
      ))

      dataSource.getServiceReleases("some-serviceName").futureValue shouldBe List(Release("1.0",now), Release("2.0",now))
    }  
  }

}

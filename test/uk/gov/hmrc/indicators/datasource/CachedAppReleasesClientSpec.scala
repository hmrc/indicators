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

import org.mockito.Mockito
import org.mockito.Mockito.{times, when, verify}
import org.mockito.internal.verification.{Times, Only}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}


import scala.concurrent.Future
import scala.concurrent.duration._

class CachedAppReleasesClientSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures {

  val releaseClient = mock[ReleasesClient]
  val cachedClient = new CachedAppReleasesClient(releaseClient) {
    override val refreshTimeInMillis = 100.millis
  }

  "getAllReleases" should {
    "load from the releases client and also cache the values" in {

      val result = List(AppRelease("", "appName", "1.0.0", LocalDateTime.now()))

      when(releaseClient.getAllReleases).thenReturn(Future.successful(result))

      cachedClient.getAllReleases.futureValue should be(result)

      cachedClient.cache.get("appReleases") shouldBe result

      verify(releaseClient, times(1)).getAllReleases
    }


  }


}

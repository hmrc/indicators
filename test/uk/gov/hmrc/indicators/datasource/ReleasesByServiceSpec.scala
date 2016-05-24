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

import org.scalatest.{Matchers, WordSpec}

class ReleasesByServiceSpec extends WordSpec with Matchers {

  val now = LocalDateTime.now()

  "ReleasesByService" should {
    "return releases by name" in {

      val requiredServiceName: String = "serviceName"

      ReleasesByService(requiredServiceName)(
        List(
          AppRelease("production", requiredServiceName, "1.0.0", now),
          AppRelease("prod", requiredServiceName, "2.0.0", now),
          AppRelease("production", "someService", "1.0", now),
          AppRelease("prod", "someService", "2.0", now)
        )) shouldBe List(
        Release("1.0.0", now),
        Release("2.0.0", now)
      )

    }

    "return releases by name and env starting with prod and production" in {

      val requiredServiceName: String = "serviceName"

      ReleasesByService(requiredServiceName)(
        List(
          AppRelease("production", requiredServiceName, "1.0.0", now),
          AppRelease("prod", requiredServiceName, "2.0.0", now),
          AppRelease("qa", requiredServiceName, "3.0.0", now)

        )) shouldBe List(
        Release("1.0.0", now),
        Release("2.0.0", now)
      )

    }

    "return releases by name and env starting with prod and production factoring multiple entries in different environments" in {

      val requiredServiceName: String = "serviceName"

      ReleasesByService(requiredServiceName)(
        List(
          AppRelease("production", requiredServiceName, "1.0.0", now),
          AppRelease("prod", requiredServiceName, "1.0.0", now),
          AppRelease("qa", requiredServiceName, "3.0.0", now)

        )) shouldBe List(
        Release("1.0.0", now)
      )

    }

  }


}

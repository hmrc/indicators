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

import java.time.LocalDate

import org.scalatest.{Matchers, WordSpec}

class ReleasesPredicateSpec extends WordSpec with Matchers {

  val now = LocalDate.now()

  "ReleasesPredicate" should {
    "return boolean if releases matches service name and has env string starting with prod or production" in {

      val requiredServiceName: String = "serviceName"
      val forService: (Release) => Boolean = ReleasesPredicate(requiredServiceName)

      forService(Release("production", requiredServiceName, "", now)) shouldBe true
      forService(Release("prod", requiredServiceName, "", now)) shouldBe true
      forService(Release("production", "someService", "", now)) shouldBe false
      forService(Release("prod", "someService", "", now)) shouldBe false
      forService(Release("qa", requiredServiceName, "", now)) shouldBe false

    }
  }


}

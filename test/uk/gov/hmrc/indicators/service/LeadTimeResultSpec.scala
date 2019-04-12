/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.YearMonth

import org.scalatest.{Matchers, WordSpec}

class LeadTimeResultSpec extends WordSpec with Matchers {

  "LeadTimeResult.of" should {
    "construct LeadTimeResult by rounding" in {
      val now: YearMonth = YearMonth.now()
      DeploymentLeadTimeResult.of(now, now.atDay(1), now.atDay(2), Some(BigDecimal(4.5))).median shouldBe Some(5)
      DeploymentLeadTimeResult.of(now, now.atDay(1), now.atDay(2), Some(BigDecimal(4))).median   shouldBe Some(4)
      DeploymentLeadTimeResult.of(now, now.atDay(1), now.atDay(2), Some(BigDecimal(4.3))).median shouldBe Some(4)
    }
  }

}

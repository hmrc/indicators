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

import uk.gov.hmrc.gitclient.GitTag

object LeadTimeCalculator {
  def calculateLeadTime(tags: Seq[GitTag], releases: Seq[Release]): List[ProductionLeadTime] = {

    val leadTimes = tags
      .map(t => t -> releases.find(r => r.tag == t.name))
      .collect { case (t, Some(r)) => t -> r }
      .map { case (t, r) =>
        t.createdAt.get.toLocalDate.until(r.date).getDays
      }
    
    val avg = BigDecimal(leadTimes.sum) / BigDecimal(leadTimes.size)

    List(ProductionLeadTime(releases.head.date, avg))
  }
}

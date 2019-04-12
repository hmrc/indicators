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

package uk.gov.hmrc.indicators

import java.time._
import java.time.format.DateTimeFormatter

import play.api.libs.json._

object JavaDateTimeImplicits {

  implicit val localDateTime = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsNumber(v) =>
        JsSuccess(
          LocalDateTime.ofEpochSecond(v.toLongExact, 0, ZoneOffset.UTC)
        )
      case v => JsError(s"invalid value for epoch second '$v'")
    }
  }

  implicit val localDate = new Writes[LocalDate] {
    override def writes(date: LocalDate): JsValue = JsString(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
  }

  implicit val yearMonthWrite = new Writes[YearMonth] {
    override def writes(o: YearMonth): JsValue = JsString(o.toString)
  }

}

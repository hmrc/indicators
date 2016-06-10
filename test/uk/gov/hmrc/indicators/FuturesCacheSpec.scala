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

package uk.gov.hmrc.indicators

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec, FunSuite}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class FuturesCacheSpec extends WordSpec with Matchers with ScalaFutures with DefaultPatienceConfig {


  trait Setup extends FuturesCache[String, String] {
    def refreshTimeInMillis: Duration = 1000 millis
  }


  "cache" should {
    "retain the old value if the load fails" in new Setup {

      import scala.collection.JavaConversions._

      override protected def cacheLoader: (String) => Future[String] =
        scala.collection.immutable.Map(
          "key" -> Future.successful("newValue"),
          "key1" -> Future {
            throw new RuntimeException
          }
        )

      cache.putAll(Map(
        "key" -> Future.successful("value"),
        "key1" -> Future.successful("oldValue")
      ))

      cache.refresh("key")
      cache.refresh("key1")

      cache.get("key").futureValue shouldBe "newValue"
      cache.get("key1").futureValue shouldBe "oldValue"

    }
  }

}

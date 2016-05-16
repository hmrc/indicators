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

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}


import scala.concurrent.duration.Duration

trait Cache[K <: Object, V <: Object] {

  def refreshTimeInMillis: Duration

  protected def cacheLoader: K => V

  private val executor: ExecutorService = Executors.newCachedThreadPool()

  lazy val cache: LoadingCache[K, V] = {

    CacheBuilder.newBuilder()
      .expireAfterWrite(refreshTimeInMillis.toMillis, TimeUnit.MILLISECONDS)
      .build(
        CacheLoader.asyncReloading(
          new CacheLoader[K, V] {
            override def load(k: K): V = cacheLoader(k)
          },
          executor
        )
      )
  }

}

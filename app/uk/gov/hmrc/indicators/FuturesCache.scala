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

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{Futures, ListenableFuture}
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global


trait FuturesCache[K, V] {
  private val executor: ExecutorService = Executors.newCachedThreadPool()

  def refreshTimeInMillis: Duration

  protected def cacheLoader: K => Future[V]


  lazy val cache = {

    CacheBuilder.newBuilder()
      .expireAfterWrite(refreshTimeInMillis.toMillis, TimeUnit.MILLISECONDS)
      .build(
        CacheLoader.asyncReloading(
          new CacheLoader[K, Future[V]] {
            override def load(k: K): Future[V] = cacheLoader(k)

            override def reload(key: K, oldValue: Future[V]): ListenableFuture[Future[V]] = {
              val p = Promise[V]()

              val loadF: Future[V] = load(key)

              loadF.onSuccess { case v => p.success(v) }

              loadF.recover {
                case e =>
                  Logger.warn(s"Error while loading cache for Key :$key retaining the old value", e)
                  p.completeWith(oldValue)

              }
              Futures.immediateFuture(p.future)
            }
          },
          executor
        )
      )
  }

}

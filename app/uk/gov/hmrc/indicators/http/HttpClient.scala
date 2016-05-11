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

package uk.gov.hmrc.indicators.http

import play.Logger
import play.api.libs.json.{JsValue, Reads}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global


class HttpClient {

  val ws = WS.client

  def get[T](url: String)(implicit r: Reads[T]): Future[T] = withErrorHandling("GET", url) {
    case s if s.status >= 200 && s.status < 300 =>
      Try {
        s.json.as[T]
      } match {
        case Success(a) => a
        case Failure(e) =>
          Logger.error(s"Error paring response failed body was: ${s.body} root url : $url")
          throw e
      }
    case res =>
      throw new RuntimeException(s"Unexpected response status : ${res.status}  calling url : $url response body : ${res.body}")
  }

  private def withErrorHandling[T](method: String, url: String, body : Option[JsValue] = None)(f: WSResponse => T)(implicit ec: ExecutionContext): Future[T] = {
    buildCall(method, url, body).execute().transform(
      f,
      _ => throw new RuntimeException(s"Error connecting  $url")
    )
  }

  private def buildCall(method: String, url: String, body: Option[JsValue] = None): WSRequestHolder = {
    val req = ws.url(url)
      .withMethod(method)
      .withHeaders("content-type" -> "application/json")

    body.map { b =>
      req.withBody(b)
    }.getOrElse(req)
  }

}

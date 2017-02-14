/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json.{Json, JsValue, Reads}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global


object HttpClient {

  def get[T](url: String, header: List[(String, String)] = List())(implicit r: Reads[T]): Future[T] =
    getResponseBody(url, header).map { rsp =>
      Try {
        Json.parse(rsp).as[T]
      } match {
        case Success(a) => a
        case Failure(e) =>
          Logger.error(s"Error paring response failed body was: $rsp root url : $url")
          throw e
      }

    }

  def getWithParsing[T](url: String, header: List[(String, String)] = List())(bodyParser: JsValue => T): Future[T] =
    getResponseBody(url, header).map { rsp =>
      Try {
        bodyParser(Json.parse(rsp))
      } match {
        case Success(a) => a
        case Failure(e) =>
          Logger.error(s"Error paring response failed body was: $rsp root url : $url")
          throw e
      }

    }


  private def getResponseBody(url: String, header: List[(String, String)] = List()): Future[String] =
    withErrorHandling("GET", url)(header) {
      case s if s.status >= 200 && s.status < 300 => s.body
      case res =>
        val msg = s"Unexpected response status : ${res.status}  calling url : $url response body : ${res.body}"
        Logger.error(msg)
        throw new RuntimeException(msg)
    }

  private def withErrorHandling[T](method: String, url: String, body: Option[JsValue] = None)(headers: List[(String, String)])(f: WSResponse => T)(implicit ec: ExecutionContext): Future[T] =
    buildCall(method, url, body, headers).execute().transform(
      f,
      e => {
        Logger.error(s"error connecting to $url", e)
        throw new RuntimeException(s"Error connecting  $url", e)
      }
    )


  private def buildCall(method: String, url: String, body: Option[JsValue] = None, headers: List[(String, String)] = List()) = {
    val req = WS.client.url(url)
      .withMethod(method)
      .withHeaders(headers: _*)

    body.map { b =>
      req.withBody(b)
    }.getOrElse(req)
  }

}

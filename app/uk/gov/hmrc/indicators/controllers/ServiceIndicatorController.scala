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

package uk.gov.hmrc.indicators.controllers

import org.apache.commons.io.FileUtils
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.gitclient.Git
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

object ServiceIndicatorController extends ServiceIndicatorController


trait ServiceIndicatorController extends BaseController {

example.com")



  def frequentProdRelease(serviceName: String) = Action.async { implicit request =>


    gitEnterPrise.getGitRepoTags(serviceName, "HMRC").map(x => Ok(Json.toJson(x.map(y => y.name))))
  }
}

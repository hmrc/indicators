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

package uk.gov.hmrc.indicators.controllers

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.indicators.ComponentRegistry
import uk.gov.hmrc.indicators.service.IndicatorsService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

object ServiceIndicatorController extends ServiceIndicatorController {

  override val indicatorsService: IndicatorsService = ComponentRegistry.indicatorsService


}


trait ServiceIndicatorController extends BaseController {

  def indicatorsService: IndicatorsService


  def serviceDeploymentMetrics(serviceName: String) = Action.async { implicit request =>

    indicatorsService.getServiceDeploymentMetrics(serviceName) map {
      case Some(ls) => Ok(Json.toJson(ls)).as("application/json")
      case _ => NotFound
    }
  }

  def teamDeploymentMetrics(serviceName: String) = Action.async { implicit request =>

    indicatorsService.getTeamDeploymentMetrics(serviceName) map {
      case Some(ls) => Ok(Json.toJson(ls)).as("application/json")
      case _ => NotFound
    }
  }

  def jobExecutionTimeMetrics(repoName: String) = Action.async { implicit request =>

    val metrics = indicatorsService.getJobExecutionTimeMetrics(repoName)
    metrics map {
      case Some(ls) =>
        Ok(Json.toJson(ls)).as("application/json")
      case _ => NotFound
    }
  }


}

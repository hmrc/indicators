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

///*
// * Copyright 2016 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.indicators
//
//import akka.stream.Materializer
//import com.kenshoo.play.metrics.MetricsFilter
//import com.typesafe.config.Config
//import net.ceedubs.ficus.Ficus._
//import play.api._
//import play.api.mvc.{EssentialAction, EssentialFilter, Filters}
//import uk.gov.hmrc.play.audit.filters.AuditFilter
//import uk.gov.hmrc.play.audit.http.connector.AuditConnector
//import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
//import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
//import uk.gov.hmrc.play.filters.{NoCacheFilter, RecoveryFilter}
//import uk.gov.hmrc.play.graphite.GraphiteConfig
//import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
//import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
//import uk.gov.hmrc.play.microservice.bootstrap.JsonErrorHandling
//import uk.gov.hmrc.play.microservice.bootstrap.Routing.RemovingOfTrailingSlashes
//import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
//import play.api.Play.current
//
//object ControllerConfiguration extends ControllerConfig {
//  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
//}
//
//object AuthParamsControllerConfiguration extends AuthParamsControllerConfig  {
//  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
//}
//
//object MicroserviceLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
//  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
//
//}
//
//object MicroserviceAuditFilter extends AuditFilter with AppName {
//  implicit def mat: Materializer = Play.materializer
//
//  override val auditConnector = MicroserviceAuditConnector
//  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
//}
//
//
////trait MicroserviceFilters {
////
////  def loggingFilter: LoggingFilter
////
////  def metricsFilter: MetricsFilter = MetricsFilter
////
////  protected lazy val defaultMicroserviceFilters: Seq[EssentialFilter] = Seq(
////    Some(metricsFilter),
////    Some(loggingFilter),
////    Some(NoCacheFilter),
////    Some(RecoveryFilter)).flatten
////
////  def microserviceFilters: Seq[EssentialFilter] = defaultMicroserviceFilters
////}
//
////object LoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
////  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
////} <- MicroserviceLoggingFilter is the one that replaces this
//
//
//
//
//object MicroserviceGlobal extends DefaultMicroserviceGlobal
//  with RunMode {
//
//  override lazy val auditConnector = MicroserviceAuditConnector
//
////  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
//  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
//
//  override lazy val loggingFilter = MicroserviceLoggingFilter
//
//  override lazy val microserviceAuditFilter = MicroserviceAuditFilter
//
//  override lazy val authFilter = None
//}
//
//

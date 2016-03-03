package uk.gov.hmrc.indicators

import com.kenshoo.play.metrics.MetricsFilter
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api._
import play.api.mvc.{EssentialAction, EssentialFilter, Filters}
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.config.{ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters.{NoCacheFilter, RecoveryFilter}
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.JsonErrorHandling
import uk.gov.hmrc.play.microservice.bootstrap.Routing.RemovingOfTrailingSlashes


object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceLoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

trait MicroserviceFilters {

  def loggingFilter: LoggingFilter

  def metricsFilter: MetricsFilter = MetricsFilter

  protected lazy val defaultMicroserviceFilters: Seq[EssentialFilter] = Seq(
    Some(metricsFilter),
    Some(loggingFilter),
    Some(NoCacheFilter),
    Some(RecoveryFilter)).flatten

  def microserviceFilters: Seq[EssentialFilter] = defaultMicroserviceFilters
}

object LoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object MicroserviceGlobal extends GlobalSettings
  with RunMode
  with GraphiteConfig
  with RemovingOfTrailingSlashes
  with JsonErrorHandling
  with MicroserviceFilters {

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  lazy val appName = Play.current.configuration.getString("appName").getOrElse("APP NAME NOT SET")

  override def onStart(app: Application) {
    Logger.info(s"Starting microservice : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  override def doFilter(a: EssentialAction): EssentialAction = {
    Filters(super.doFilter(a), microserviceFilters: _*)
  }

  override def loggingFilter: LoggingFilter = LoggingFilter
}

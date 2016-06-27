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

import java.nio.file.{Files, Paths}

import play.api.{Logger, Play}

trait ConfigProvider {
  def configs: Configs

  lazy val releasesApiBase: String = requiredConf("releases.app.api.base")

  private def optionalConf(path: String): Option[String] = configs.config(path)
  private def requiredConf(path: String): String = configs.config(path).getOrElse(
    throw new RuntimeException(s"No conf for key : $path"))
}

trait IndicatorsConfigProvider extends ConfigProvider {
  def configs: Configs = PlayConfigs
}

trait Configs {
  def config(path: String): Option[String]
}

object PlayConfigs extends Configs {
  def config(path: String) = Play.current.configuration.getString(path)
}

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

import java.nio.file.{Path, Files, Paths}


import uk.gov.hmrc.gitclient.Git
import uk.gov.hmrc.indicators.service._

object ComponentRegistry extends ConfigProvider {
  lazy private val tempDirectory = gitClientStorePath.fold(Files.createTempDirectory("local-git-store").toString)(identity)
  println(tempDirectory)

  val gitClient = Git(tempDirectory, gitEnterpriseToken, gitEnterpriseHost)
  val tagsDataSource = new GitTagsDataSource(gitClient)
  val releasesClient = new ReleasesClient(releasesApiBase)
  val releasesDataSource = new AppReleasesDataSource(releasesClient)

  val indicatorsService = new IndicatorsService(tagsDataSource, releasesDataSource)
}

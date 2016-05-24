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


import play.api.Logger
import uk.gov.hmrc.gitclient.Git
import uk.gov.hmrc.indicators.datasource._
import uk.gov.hmrc.indicators.service.IndicatorsService

object ComponentRegistry extends IndicatorsConfigProvider {


  val gitEnterpriseClient = Git(enterpriseGitStorePath, gitEnterpriseToken, gitEnterpriseHost, withCleanUp = true)
  val gitOpenClient = Git(openGitStorePath, gitOpenToken, gitOpenHost, withCleanUp = true)

  val enterpriseTagsDataSource = new GitReleaseTagsDataSource(gitEnterpriseClient)
  val openTagsDataSource = new GitReleaseTagsDataSource(gitOpenClient)
  val compositeTagsDataSource = new CompositeReleaseTagsDataSource(enterpriseTagsDataSource, openTagsDataSource)
  val cachedTagsDataSource = new CachedReleaseTagsDataSource(compositeTagsDataSource)

  val releasesClient = new AppReleasesClient(releasesApiBase)
  val cachedReleasesClient = new CachedAppReleasesClient(releasesClient)
  val releasesDataSource = new AppReleasesDataSource(cachedReleasesClient)
  val catalogueClient = new CatalogueServiceClient(catalogueApiBase)

  val indicatorsService = new IndicatorsService(cachedTagsDataSource, releasesDataSource, catalogueClient)

}

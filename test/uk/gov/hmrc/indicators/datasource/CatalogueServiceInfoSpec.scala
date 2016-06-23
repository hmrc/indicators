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

package uk.gov.hmrc.indicators.datasource

import org.scalatest.{Matchers, WordSpec}

class CatalogueServiceInfoSpec extends WordSpec with Matchers {

  "toServiceInfo" should {
    "convert to serviceInfo" in {

      val catalogueService = CatalogueServiceInfo(
        "service-frontend",
        List(
          CatalogueServiceGitUrl("github-enterprise","https://github.some.host.url.gov.uk/org1-org/service-frontend"),
          CatalogueServiceGitUrl("github-com", "http://github.com/org2/service-frontend")
        )
      )

      CatalogueServiceInfo.toServiceRepos(catalogueService) shouldBe List(
        ServiceRepositoryInfo(
          "service-frontend",
          "org1-org",
          RepoType.Enterprise
        ),
        ServiceRepositoryInfo(
          "service-frontend",
          "org2",
          RepoType.Open
        )
      )
    }

    "convert to serviceInfo when no gitenterprise url" in {

      val catalogueService = CatalogueServiceInfo(
        "serviceName",
        List(
          CatalogueServiceGitUrl("github-com", "https://someOtherGitHubHost/org2/serviceName")
        )
      )

      CatalogueServiceInfo.toServiceRepos(catalogueService) shouldBe List(
        ServiceRepositoryInfo(
          "serviceName",
          "org2",
          RepoType.Open
        )
      )

    }

    "convert to serviceInfo when no git open url" in {

      val catalogueService = CatalogueServiceInfo(
        "serviceName",
        List(
          CatalogueServiceGitUrl("github-enterprise", "https://someOtherGitHubHost/org1/serviceName")
        )
      )

      CatalogueServiceInfo.toServiceRepos(catalogueService) shouldBe List(ServiceRepositoryInfo(
        "serviceName",
        "org1",
        RepoType.Enterprise
      ))
    }
  }

}

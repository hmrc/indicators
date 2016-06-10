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

import java.time.{LocalDateTime, ZoneId}
import java.util.concurrent.Executors

import play.api.Logger
import uk.gov.hmrc.gitclient.{GitClient, GitTag}
import uk.gov.hmrc.githubclient.{GhRepoRelease, GithubApiClient}
import uk.gov.hmrc.indicators.FuturesCache
import uk.gov.hmrc.indicators.datasource.RepoType.{Enterprise, Open}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

case class ServiceReleaseTag(name: String, createdAt: LocalDateTime)


object ServiceReleaseTag {
  val versionNumber = "(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$".r

  implicit def gitTagsToServiceReleaseTags(gt: List[GitTag]): List[ServiceReleaseTag] = gt.map(ServiceReleaseTag.apply)

  implicit def ghRepoReleasesToServiceReleaseTags(gr: List[GhRepoRelease]): List[ServiceReleaseTag] = gr.map(ServiceReleaseTag.apply)


  def apply(ghr: GhRepoRelease): ServiceReleaseTag = {

    ServiceReleaseTag(getVersionNumber(ghr.tagName), ghr.createdAt.toInstant.atZone(ZoneId.systemDefault()).toLocalDateTime)
  }

  def apply(gt: GitTag): ServiceReleaseTag = {

    ServiceReleaseTag(getVersionNumber(gt.name), gt.createdAt.get.toLocalDateTime)
  }


  private def getVersionNumber(tag: String): String = versionNumber.findFirstIn(tag).getOrElse(tag)
}


trait ServiceReleaseTagDataSource {

  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[ServiceReleaseTag]]
}

class CachedServiceReleaseTagDataSource(tagsDataSource: ServiceReleaseTagDataSource) extends ServiceReleaseTagDataSource with FuturesCache[ServiceRepositoryInfo, List[ServiceReleaseTag]] {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[ServiceReleaseTag]] = cache.getUnchecked(serviceRepositoryInfo)

  override def refreshTimeInMillis: Duration = 3 hours

  override protected def cacheLoader: ServiceRepositoryInfo => Future[List[ServiceReleaseTag]] = tagsDataSource.getServiceRepoReleaseTags

}

class CompositeServiceReleaseTagDataSource(gitEnterpriseTagDataSource: ServiceReleaseTagDataSource, gitOpenTagDataSource: ServiceReleaseTagDataSource) extends ServiceReleaseTagDataSource {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo) =
    serviceRepositoryInfo.repoType match {

      case Enterprise =>
        Logger.debug(s"${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org} get Enterprise Repo release tags")

        gitEnterpriseTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)

      case Open =>
        Logger.debug(s"${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org} get Open Repo release tags")

        gitOpenTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)

    }
}

class GitHubReleaseTagDataSource(gitHubClient: GithubApiClient) extends ServiceReleaseTagDataSource {
  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[ServiceReleaseTag]] = {
    import BlockingIOExecutionContext.executionContext

    gitHubClient.getReleases(serviceRepositoryInfo.org, serviceRepositoryInfo.name).map(identity(_))
  }
}

class GitTagDataSource(gitClient: GitClient, githubApiClient: GithubApiClient) extends ServiceReleaseTagDataSource {

  import BlockingIOExecutionContext.executionContext

  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[ServiceReleaseTag]] = {


    gitClient.getGitRepoTags(serviceRepositoryInfo.name, serviceRepositoryInfo.org).flatMap {
      x =>
        val (withCreatedAt, withoutCreatedAt) = x.partition(_.createdAt.isDefined)

        val serviceRelease: List[ServiceReleaseTag] = withCreatedAt

        tagsWithReleaseDate(withoutCreatedAt, serviceRepositoryInfo).map(serviceRelease ++ _)
    }

  }

  private def tagsWithReleaseDate(tags: List[GitTag], serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[ServiceReleaseTag]] = {
    if (tags.nonEmpty) {

      Logger.warn(s"${serviceRepositoryInfo.name} invalid git Tags total : ${tags.size} getting git releases")

      for {
        rs <- githubApiClient.getReleases(serviceRepositoryInfo.org, serviceRepositoryInfo.name)
      } yield {

        val releaseTags = tags.flatMap { x =>
          rs.find(_.tagName == x.name).map(ServiceReleaseTag.apply)
        }

        Logger.info(s"${serviceRepositoryInfo.name} tags from git releases total : ${releaseTags.size}")
        releaseTags
      }

    } else Future.successful(Nil)

  }
}

object BlockingIOExecutionContext {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))


}



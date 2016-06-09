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

import java.time.LocalDateTime
import java.util.concurrent.Executors

import play.api.Logger
import uk.gov.hmrc.gitclient.GitClient
import uk.gov.hmrc.indicators.FuturesCache
import uk.gov.hmrc.indicators.datasource.RepoType.{Enterprise, Open}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

case class RepoReleaseTag(name: String, createdAt: LocalDateTime)


trait ReleaseTagsDataSource {
  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoReleaseTag]]
}

class CachedReleaseTagsDataSource(tagsDataSource: ReleaseTagsDataSource) extends ReleaseTagsDataSource with FuturesCache[ServiceRepositoryInfo, List[RepoReleaseTag]] {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoReleaseTag]] = cache.getUnchecked(serviceRepositoryInfo)

  override def refreshTimeInMillis: Duration = 3 hours


  override protected def cacheLoader: ServiceRepositoryInfo => Future[List[RepoReleaseTag]] = tagsDataSource.getServiceRepoReleaseTags


}

class CompositeReleaseTagsDataSource(gitEnterpriseTagDataSource: ReleaseTagsDataSource, gitOpenTagDataSource: ReleaseTagsDataSource) extends ReleaseTagsDataSource {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo) =
    serviceRepositoryInfo.repoType match {

      case Enterprise =>
        Logger.debug(s"Get Enterprise Repo release tags for : ${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org}")

        gitEnterpriseTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)

      case Open =>
        Logger.debug(s"Get Open Repo release tags for : ${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org}")

        gitOpenTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)

    }
}

class GitReleaseTagsDataSource(gitClient: GitClient) extends ReleaseTagsDataSource {

  val versionNumber = "(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$".r

  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoReleaseTag]] = {
    import BlockingIOExecutionContext.executionContext

    gitClient.getGitRepoTags(serviceRepositoryInfo.name, serviceRepositoryInfo.org).map {
      x =>
        val (withCreatedAt, withoutCreatedAt) = x.partition(_.createdAt.isDefined)

        if (withoutCreatedAt.nonEmpty) {
          Logger.warn(s"Invalid Release Tags Service : ${serviceRepositoryInfo.name} total : ${withoutCreatedAt.size} of : ${x.size}")
        }

        withCreatedAt.map(x => RepoReleaseTag(getVersionNumber(x.name), x.createdAt.get.toLocalDateTime))
    }

  }


  private def getVersionNumber(tag: String): String = versionNumber.findFirstIn(tag).getOrElse(tag)
}

object BlockingIOExecutionContext {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

}



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

package uk.gov.hmrc.indicators.service

import java.time.{LocalDateTime, ZonedDateTime}

import play.api.Logger
import uk.gov.hmrc.gitclient.{GitClient, GitTag}
import uk.gov.hmrc.indicators.Cache
import uk.gov.hmrc.indicators.service.RepoType.{Open, Enterprise}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

case class RepoTag(name: String, createdAt: Option[LocalDateTime])

object RepoTag {
  val versionNumber = "(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$".r

  def gitTagToRepoTag(gitTag: GitTag): RepoTag = {
    RepoTag(getVersionNumber(gitTag.name).getOrElse(gitTag.name), gitTag.createdAt.map(_.toLocalDateTime))
  }

  private def getVersionNumber(tag: String): Option[String] = versionNumber.findFirstIn(tag)
}

trait TagsDataSource {
  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoTag]]
}

class CachedTagsDataSource(tagsDataSource: TagsDataSource) extends TagsDataSource with Cache[ServiceRepositoryInfo, List[RepoTag]] {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoTag]] = Future.successful(cache.get(serviceRepositoryInfo))

  override def refreshTimeInMillis: Duration = 6 hours

  override protected def cacheLoader: ServiceRepositoryInfo => List[RepoTag] = {
    case serviceRepositoryInfo => Await.result(tagsDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo), 30 seconds)
  }
}

class CompositeTagsDataSource(gitEnterpriseTagDataSource: TagsDataSource, gitOpenTagDataSource: TagsDataSource) extends TagsDataSource {

  override def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo) =
    serviceRepositoryInfo.repoType match {

      case Enterprise => {
        Logger.debug(s"Enterprise Repo release tags for : ${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org}")

        gitEnterpriseTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)
      }
      case Open => {
        Logger.debug(s"Open Repo release tags for : ${serviceRepositoryInfo.name} org : ${serviceRepositoryInfo.org}")

        gitOpenTagDataSource.getServiceRepoReleaseTags(serviceRepositoryInfo)
      }
    }
}

class GitTagsDataSource(gitClient: GitClient) extends TagsDataSource {

  def getServiceRepoReleaseTags(serviceRepositoryInfo: ServiceRepositoryInfo): Future[List[RepoTag]] = {

    gitClient.getGitRepoTags(serviceRepositoryInfo.name, serviceRepositoryInfo.org).map(x => x.map(RepoTag.gitTagToRepoTag))

  }
}

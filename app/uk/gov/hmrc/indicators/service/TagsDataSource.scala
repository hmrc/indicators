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

import java.time.ZonedDateTime

import uk.gov.hmrc.gitclient.{GitClient, GitTag}
import uk.gov.hmrc.indicators.Cache

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

case class RepoTag(name: String, createdAt: Option[ZonedDateTime])

object RepoTag {
  val versionNumber = "(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$".r

  def gitTagToRepoTag(gitTag: GitTag): RepoTag = {
    RepoTag(getVersionNumber(gitTag.name).getOrElse(gitTag.name), gitTag.createdAt)
  }

  private def getVersionNumber(tag: String): Option[String] = versionNumber.findFirstIn(tag)
}

trait TagsDataSource {
  def getServiceRepoTags(repoName: String, owner: String): Future[List[RepoTag]]
}

class CachedTagsDataSource(tagsDataSource: TagsDataSource) extends TagsDataSource with Cache[(String, String), List[RepoTag]] {

  override def getServiceRepoTags(repoName: String, owner: String): Future[List[RepoTag]] = Future.successful(cache.get((repoName, owner)))

  override def refreshTimeInMillis: Duration = 24 hours

  override protected def cacheLoader: ((String, String)) => List[RepoTag] = {
    case (repoName, owner) => Await.result(tagsDataSource.getServiceRepoTags(repoName, owner), 30 seconds)
  }
}


class GitTagsDataSource(gitClient: GitClient) extends TagsDataSource {

  def getServiceRepoTags(repoName: String, owner: String): Future[List[RepoTag]] = {
    gitClient.getGitRepoTags(repoName, owner).map(x => x.map(RepoTag.gitTagToRepoTag))
  }
}

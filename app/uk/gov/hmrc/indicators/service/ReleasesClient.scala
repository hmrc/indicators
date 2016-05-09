package uk.gov.hmrc.indicators.service

import java.time.LocalDate

import scala.concurrent.Future


case class Release(tag : String, date : LocalDate)
class ReleasesClient {

  def getAllReleases(serviceName:String) : Future[List[Release]]= ???

}

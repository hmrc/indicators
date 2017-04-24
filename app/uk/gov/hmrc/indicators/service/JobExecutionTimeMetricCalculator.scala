package uk.gov.hmrc.indicators.service

import java.time._

import uk.gov.hmrc.indicators.datasource.{Build, Deployment}

case class JobExecutionTimeMetricResult(period: YearMonth,
                                   from: LocalDate,
                                   to: LocalDate,
                                   duration: Option[MeasureResult])

class JobExecutionTimeMetricCalculator {

  type JobExecutionBucket = Iterable[(YearMonth, Seq[Deployment])]
  val monthlyWindowSize: Int = 3
  val monthsToLookBack = 3

  def calculateDeploymentMetrics(builds: Seq[Build], requiredPeriodInMonths: Int): Seq[JobExecutionTimeMetricResult] = {
    withLookBack(requiredPeriodInMonths) { requiredMonths =>
      val buildBuckets = getJobExecutionBuckets(builds, requiredMonths)

      buildBuckets.zipWithIndex.map { case (bucket, index) =>
        val dateData = DateData(buildBuckets.size, bucket, index)

        val allBuildsInBucket: Seq[Build] = bucket.flatMap(_._2).toSeq
        JobExecutionTimeMetricResult(dateData.period, dateData.from, dateData.to, calculateMeasureResult(allBuildsInBucket, _.duration))
      }
    }
  }

  private def calculateMeasureResult(builds: Seq[Build], measureReader: (Build) => Int): Option[MeasureResult] = {
    import IndicatorTraversable._

    val measures: Iterable[Int] = for {
      build <- builds
      measure <- measureReader(build)
    } yield measure

    measures.median.map(MeasureResult.toMeasureResult)
  }

  private def epochToDate(epoch: Long) = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)

  private def getJobExecutionBuckets[T <: MetricsResult](builds: Seq[Build], requiredPeriod: Int) =
    MonthlyBucketBuilder(builds, requiredPeriod)(o => epochToDate(o.timestamp)).slidingWindow(monthlyWindowSize)

  private def withLookBack[T](requiredPeriod: Int)(f: Int => Seq[T]): Seq[T] = {
    f(requiredPeriod + monthsToLookBack).takeRight(requiredPeriod)
  }
  
  private case class DateData(period: YearMonth, from: LocalDate, to: LocalDate)

  private object DateData {
    def apply(buildBucketSize: Int,
              bucket: Iterable[(YearMonth, Seq[Build])],
              indx: Int)(implicit clock: Clock): DateData = {

      val (period, _) = bucket.last
      val (from, _) = bucket.head

      val toDate =
        if (indx == (buildBucketSize - 1))
          period.atDay(LocalDate.now(clock).getDayOfMonth)
        else period.atEndOfMonth()

      val fromDate: LocalDate = from.atDay(1)

      DateData(period, fromDate, toDate)
    }
  }


}

package analysis

import model.SquerylEntryPoint._
import model.{DataSource, TimeSeriesData}
import play.api.libs.json.Json
import web.{QuarterlyResults}

/**
  * Created by sdoshi on 6/12/2016.
  */
class QuarterToQuarterRevenueGrowth extends IRecommendationAnalysisRun {

  def revenueStepFunction(ratio: Float) : BigDecimal = {
    if ( ratio <= 0)
      BigDecimal(0)
    else if ( ratio <= 1) {
      val revenueDecrease = 1 - ratio
      if (revenueDecrease <= .2)
        BigDecimal(75 - (revenueDecrease * 250))
      else {
        BigDecimal(25 - (revenueDecrease * 25))
      }
    }
    else if ( ratio > 1.10)
      BigDecimal(100)
    else {
      val revenueIncrease = (((ratio - 1) * 100) * 2.5) + 75
      BigDecimal(revenueIncrease.toDouble)
    }
  }

  def run(symbol: String) : BigDecimal = {
    println("Running MarketWatch Quarterly Report for " + symbol)

    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.MarketWatchQuarterly)
    }

    val quarterlyResults: QuarterlyResults =
      Json.parse(timeSeriesData.data).validate[QuarterlyResults].get

    if ( quarterlyResults.results.length < 5)
      throw new Exception("Not enough quarterly data to run analysis")

    val firstQuarter = quarterlyResults.results.head
    println("FirstQuarter: " + firstQuarter)

    val lastQuarter = quarterlyResults.results.last
    println("LastQuarter: " + lastQuarter)

    if (firstQuarter.income.revenue.isEmpty || lastQuarter.income.revenue.isEmpty)
      throw new Exception("No revenue data for QuarterToQuarterRevenueGrowth")

    val ratio = lastQuarter.income.revenue.get.toFloat/firstQuarter.income.revenue.get.toFloat

    revenueStepFunction(ratio)
  }

}

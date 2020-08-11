package analysis

import model.SquerylEntryPoint._
import model.{DataSource, TimeSeriesData}
import play.api.libs.json.Json
import web.MarketWatchKeyStatistics

/**
  * Created by sdoshi on 8/4/2016.
  */
class RevenuePerEmployee extends IRecommendationAnalysisRun {


  override def run(symbol: String): BigDecimal = {

    println("Running RevenuePerEmployee analysis for " + symbol)

    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.MarketWatchKeyStatistics)
    }

    val keyStatistics: MarketWatchKeyStatistics =
      Json.parse(timeSeriesData.data).validate[MarketWatchKeyStatistics].get

    val revenuePerEmployee = keyStatistics.revenuePerEmployee

    if ( revenuePerEmployee.isEmpty || revenuePerEmployee.get <= 0)
      BigDecimal(0)
    else if ( revenuePerEmployee.get <= BigDecimal(100000)) {
      (revenuePerEmployee.get/100000) * 25
    }
    else if ( revenuePerEmployee.get <= BigDecimal(200000)) {
      (revenuePerEmployee.get/200000) * 50
    }
    else if ( revenuePerEmployee.get <= BigDecimal(300000)) {
      (revenuePerEmployee.get/300000) * 75
    }
    else if ( revenuePerEmployee.get <= BigDecimal(400000)) {
      (revenuePerEmployee.get/400000) * 85
    }
    else if ( revenuePerEmployee.get <= BigDecimal(600000)) {
      (revenuePerEmployee.get/600000) * 90
    }
    else if ( revenuePerEmployee.get <= BigDecimal(800000)) {
      (revenuePerEmployee.get/800000) * 95
    }
    else if ( revenuePerEmployee.get <= BigDecimal(1000000)) {
      (revenuePerEmployee.get/1000000) * 100
    }
    else {
      BigDecimal(100)
    }
  }
}

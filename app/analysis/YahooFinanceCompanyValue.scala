package analysis

import model.TimeSeriesData
import model.DataSource
import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json._
import web.KeyStatistics
import web.YahooFinanceAPI._


class YahooFinanceCompanyValue extends IRecommendationAnalysisRun {
  

  
  def run(symbol: String) : BigDecimal = {
    println("Running Yahoo Finance Company Value analysis for " + symbol)
    
    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.YahooFinance)
    }

    val keyStatistics: KeyStatistics =
      Json.parse(timeSeriesData.data).validate[KeyStatistics].get

    val priceToBook = keyStatistics.priceBook

    val logScale = priceToBook.getOrElse(BigDecimal(28)).toDouble


   val toReturn = -(scala.math.log(logScale) * 15) + 100


    if ( toReturn < 0)
      BigDecimal(0)
    else if ( toReturn > 100 )
      BigDecimal(100)
    else
      toReturn
  }
}
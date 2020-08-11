package analysis

import org.squeryl._
import model.SquerylEntryPoint._
import model.TimeSeriesData
import model.DataSource
import web.EarningsData
import play.api.libs.json.Json
import web.NasdaqEarningsDateScraper._

class NasdaqEarningsTimeOfDay extends IRecommendationAnalysisRun {
 
  def run(symbol: String) : BigDecimal = {
    println("Running NasdaqEarningsTimeOfDay analysis for " + symbol)
    
    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.NasdaqEarnings)
    }
    
    val earningsData: EarningsData =
      Json.parse(timeSeriesData.data).validate[EarningsData].get

    if (earningsData.is_premarket)
      BigDecimal(100)
    else
      BigDecimal(0)  
  }
}
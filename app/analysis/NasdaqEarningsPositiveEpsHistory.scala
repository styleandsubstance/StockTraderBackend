package analysis

import org.squeryl._
import model.SquerylEntryPoint._
import model.TimeSeriesData
import model.DataSource
import web.EarningsData
import play.api.libs.json.Json
import web.NasdaqEarningsDateScraper._
import web.HistoricalEpsData
import web.EpsData

class NasdaqEarningsPositiveEpsHistory extends IRecommendationAnalysisRun {
 
  def calculateScore(epsData: EpsData) : BigDecimal = {
    //val calculations: List[BigDecimal] = Nil
    
    val baseScore: BigDecimal = {
      if (epsData.epsReported > 0 )
        BigDecimal(10)
      else
        BigDecimal(0)
    }
    

    val percentSurpriseScore = {
      if ( epsData.percentSurprise > 15 )
        BigDecimal(15)
      else if ( epsData.percentSurprise > 0 )
        epsData.percentSurprise
      else
        BigDecimal(0)
    }
      
    baseScore + percentSurpriseScore
  }
  
  def run(symbol: String) : BigDecimal = {
    println("Running NasdaqEarningsPositiveEpsHistory analysis for " + symbol)
    
    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.NasdaqHistoricalEps)
    }
    
    val earningsData: HistoricalEpsData =
      Json.parse(timeSeriesData.data).validate[HistoricalEpsData].get
   
    val totalScore: BigDecimal = 
      earningsData.historicalEpsData.take(4).map{x => calculateScore(x) }.sum
    
    if ( totalScore > 100 )
      BigDecimal(100)
    else
      totalScore
  }
}
package analysis

import model.RecommendationAnalysis
import web.AmericanBullsScraper
import web.AmericanBullsRecommendation
import web.TransactionType
import org.squeryl._
import model.SquerylEntryPoint._
import model.TimeSeriesData
import model.DataSource
import play.api.libs.json._
import web.AmericanBullsRecommendationHistory
import web.AmericanBullsScraper._
import web.RecommendationStatus
import web.RecommendationStatus._


class AmericanBulls extends IRecommendationAnalysisRun {
  
  def run(symbol: String) : BigDecimal = {
    println("Running American Bulls analysis for " + symbol)
    val timeSeriesData = transaction {
      TimeSeriesData.loadLatest(symbol, DataSource.AmericanBulls)
    }
    
    val recommendationHistory: AmericanBullsRecommendationHistory =
      Json.parse(timeSeriesData.data).validate[AmericanBullsRecommendationHistory].get
    
    
    val currentRecommendation = {
      if (recommendationHistory.recommendations.lift(0).get.transactionType.id == TransactionType.BUY.id)
        BigDecimal(50)
      else
        BigDecimal(0)
    }
    
    val numRecommendations = {
      if ( recommendationHistory.recommendations.length > 0 ) {
        recommendationHistory.recommendations.length
      }
      else {
        1
      }
    }
      
    val correctRecommendationWeight: BigDecimal = 
      BigDecimal(50)/BigDecimal(numRecommendations)
    
    val recommendationAccuracy = recommendationHistory.recommendations.map(r => {
      if ( r.recommendationStatus.id == RecommendationStatus.CORRECT.id)
        correctRecommendationWeight
      else 
        BigDecimal(0)  
    }).sum
    
    currentRecommendation + recommendationAccuracy
  }
}
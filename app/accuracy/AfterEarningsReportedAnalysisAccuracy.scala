package accuracy

import java.util.Date

import model._
import model.SquerylEntryPoint._
import play.api.libs.json.Json
import web.StockQuote
import web.YahooFinanceAPI._


/**
  * Created by sdoshi on 8/28/2016.
  */
class AfterEarningsReportedAnalysisAccuracy extends IRecommendationAnalysisAccuracyRun{

  def floor(value: BigDecimal) : BigDecimal = {
    if ( value > 0 && value < 0.5)
      BigDecimal(0.5)
    else if ( value < 0 && value > -0.5)
      BigDecimal(-0.5)
    else {
      value
    }
  }

  def calculateAccuracy(result: BigDecimal, quote: StockQuote): Option[BigDecimal] = {
    try {
      val closePrice = BigDecimal(quote.LastTradePriceOnly.getOrElse("0"))
      val pointChange = BigDecimal(quote.Change.getOrElse("0"))
      val previousPrice = closePrice - pointChange;
      val percentChange = (pointChange/previousPrice) * 100
      val predictionPercentChange = floor((result - 70)/3);

      val accuracy = {

        if ( predictionPercentChange > 0 && percentChange > 0 ) {
          percentChange * 5
        }
        else if ( predictionPercentChange < 0 && percentChange < 0) {
          percentChange.abs * 5
        }
        else if (predictionPercentChange > 0 && percentChange < 0) {
          percentChange * 5
        }
        else {
          -percentChange * 5
        }
      }

      Some(accuracy)
    } catch {
      case e: Exception =>
        None
    }
  }



  override def run(recommendationAnalysisAccuracyId: Long, params: Option[String]): Unit = {

    val data = transaction {
      val accuracy: RecommendationAnalysisAccuracy = RecommendationAnalysisAccuracy.findById(
        recommendationAnalysisAccuracyId)
      val analysis = RecommendationAnalysis.findById(accuracy.analysis_id)

      AccuracyData(accuracy, analysis, List(TimeSeriesData.loadLatest(analysis.stock.symbol, DataSource.StockQuote)))
    }

    val stockQuote = Json.parse(data.timeSeriesData.head.data).validate[StockQuote].get

    val accuracy = calculateAccuracy(data.recommendationAnalysis.result.getOrElse(0), stockQuote)

    transaction {
      RecommendationAnalysisAccuracy.updateAccuracy(data.recommendationAnalysisAccuracy.id, accuracy)

      val analysisEngines = data.recommendationAnalysis.engines
      analysisEngines.foreach(e => {
        val engineAccuracy = calculateAccuracy(e.result.get, stockQuote)
        Library.recommendationAnalysisEngineAccuracy.insert(
          new RecommendationAnalysisEngineAccuracy(0, new Date(), data.recommendationAnalysis.id, AccuracyEngineEnum.AfterEarningsReportedAnalysisAccuracy.id, e.id, engineAccuracy))
      })

    }
  }
}

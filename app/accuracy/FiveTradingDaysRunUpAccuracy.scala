package accuracy

import java.util.Date

import org.squeryl._
import model.SquerylEntryPoint._
import model._
import web.StockQuote
import play.api.libs.json._
import web.YahooFinanceAPI._




/**
  * Created by sdoshi on 8/28/2016.
  */
class FiveTradingDaysRunUpAccuracy extends IRecommendationAnalysisAccuracyRun {


  def stepFunction(percentageChange: BigDecimal): BigDecimal = {

    val result = (((percentageChange * 100) - 100)/3) * 25

    //val result = percentageChange * 24
    val toReturn = result + 60;
    toReturn.min(100).max(0)
  }


  def calculateAccuracy(result: BigDecimal, firstQuote: BigDecimal, maxStockPrice: BigDecimal, minStockPrice: BigDecimal): BigDecimal = {
    val percentageChange = {
      if ( result > 75 ) {
        maxStockPrice/firstQuote
      }
      else {
        firstQuote/minStockPrice
      }
    }

    stepFunction(percentageChange)
  }

//  def getOpen(quote: StockQuote): BigDecimal = {
//    //sometimes the quote has an Open
//
//  }


  override def run(recommendationAnalysisAccuracyId: Long, params: Option[String]): Unit = {



    //get five trading days of data
    val data = transaction {
      val accuracy: RecommendationAnalysisAccuracy = RecommendationAnalysisAccuracy.findById(
        recommendationAnalysisAccuracyId)
      val analysis = RecommendationAnalysis.findById(accuracy.analysis_id)
      AccuracyData(accuracy, analysis, TimeSeriesData.load(analysis.stock.symbol, DataSource.StockQuote, None, 5).reverse)
    }

    //check to see if the stock price went up within the params percentage
    val quotes: List[StockQuote] = data.timeSeriesData.map(x => Json.parse(x.data).validate[StockQuote].get)

    println(data.timeSeriesData.length)
    println(data.timeSeriesData.foreach(d => println(d.stock_id + ": " + d.data)))

    println("Done")

    //find the first day quote
    //val firstQuote = BigDecimal(quotes.head.Open.get)
    val firstQuote = BigDecimal(quotes.head.Open.getOrElse( {
      quotes.head.DaysHigh.getOrElse("100")
    }))
    println("Open: " + firstQuote)
    val maxStockPrice = quotes.map(x => BigDecimal(x.DaysHigh.get)).max
    println("Max: " + maxStockPrice)
    val minStockPrice =  quotes.map(x => BigDecimal(x.DaysLow.get)).min
    println("Min: " + minStockPrice)

    //calculate an accuracy based on what analysis result predicted
    val percentageChange = {
      if ( data.recommendationAnalysis.result.get > 75 ) {
        maxStockPrice/firstQuote
      }
      else {
        firstQuote/minStockPrice
      }
    }

    val accuracy = calculateAccuracy(data.recommendationAnalysis.result.get, firstQuote, maxStockPrice, minStockPrice)

    println("Accuracy: " + accuracy)

    //update the accuracy
    transaction {
      RecommendationAnalysisAccuracy.updateAccuracy(data.recommendationAnalysisAccuracy.id, Some(accuracy))

      //find all the engines associated with this analysis
      val analysisEngines = data.recommendationAnalysis.engines
      analysisEngines.foreach(e => {
        val engineAccuracy = calculateAccuracy(e.result.get, firstQuote, maxStockPrice, minStockPrice)
        Library.recommendationAnalysisEngineAccuracy.insert(
          new RecommendationAnalysisEngineAccuracy(0, new Date(), data.recommendationAnalysis.id, AccuracyEngineEnum.FiveTradingDaysRunUpAccuracy.id, e.id, Some(engineAccuracy)))
      })
    }
  }
}

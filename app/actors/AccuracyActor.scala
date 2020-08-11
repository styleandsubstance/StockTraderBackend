package actors

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import java.util.Date
import javax.inject.Inject

import accuracy.IRecommendationAnalysisAccuracyRun
import model._
import org.squeryl._
import model.SquerylEntryPoint._
import web.{EarningsData, StockQuote, YahooFinanceAPI}
import web.YahooFinanceAPI._
import org.joda.time.DateTime
import play.api.libs.json._
import web.NasdaqEarningsDateScraper._

import scala.util.{Failure, Try}

case class PerformAccuracyAnalysis(date: Date)
case class LaunchRecommendationAnalysisAccuracyTasks(date: Date)
case class PerformEngineAccuracyAnalysis(date: Date)
case class StockDataForAnalysis(stock: Stock, analyses: List[RecommendationAnalysis], data: List[TimeSeriesData])



class AccuracyActor extends Actor {
  def receive = {
    case LaunchRecommendationAnalysisAccuracyTasks(date) => {

      println("Here!")

      val accuracyEngines = transaction {
        RecommendationAnalysisAccuracy.findByExecutionDate(date)
      }

      accuracyEngines.foreach(r => {
        val accuracyEngineToRun: IRecommendationAnalysisAccuracyRun =
          Class.forName("accuracy." + AccuracyEngineEnum(r.accuracy_engine_id.toInt)).newInstance().asInstanceOf[IRecommendationAnalysisAccuracyRun]

        context.system.actorOf(Props[RecommendationAnalysisAccuracyActor]) !
          RunAccuracyEngine(r.id, accuracyEngineToRun)
      })
    }

//    case PerformAccuracyAnalysis(date) => {
//
//      val yesterday = new DateTime(date).plusDays(-1).toDate
//
//
//      println(yesterday)
//
//      //get list of stocks
//      val stocksInEarningsDate: List[StockDataForAnalysis] = transaction {
//        val stocks: List[Stock] = Stock.findStocksByNextEarningsDate(yesterday, date)
//
//        println(stocks.length)
//
//        val filteredStock = stocks.filter(x => {
//
//          val stockData = TimeSeriesData.load(x.symbol, DataSource.NasdaqEarnings, None, 1).head
//          val nasdaqData = Json.parse(stockData.data).validate[EarningsData].get
//
//          if ( nasdaqData.is_premarket == true && x.next_earnings_date == date ||
//               nasdaqData.is_premarket == false && x.next_earnings_date == yesterday)
//            true
//          else
//            false
//        })
//
//        filteredStock.map(x => StockDataForAnalysis(x,
//            RecommendationAnalysis.findCompletedAnalysesForStockForDate(x.id, x.next_earnings_date),
//            TimeSeriesData.load(x.symbol, DataSource.StockQuote, None, 1)))
//      }
//
//      stocksInEarningsDate.foreach( x => {
//       val latestStockDataChange = {
//         if ( x.data.length > 0 )
//           Some(Json.parse(x.data.head.data).validate[StockQuote].get)
//         else
//           None
//       }
//
//       x.analyses.foreach ( y => {
//         latestStockDataChange.map(s => {
//           transaction {
//             val analysisEngines = RecommendationAnalysisEngine.findByAnalysisId(y.id)
//             analysisEngines.foreach(engine => {
//               val engineAccuracy = AccuracyActor.calculateAccuracy(engine.result.getOrElse(0), s)
//               RecommendationAnalysisEngine.updateAccuracy(y.id, engineAccuracy)
//             })
//
//             val accuracy = AccuracyActor.calculateAccuracy(y.result.getOrElse(0), s)
//             RecommendationAnalysis.updateAccuracy(y.id, accuracy)
//           }
//         })
//       })
//      })
//    }
//
//    case PerformEngineAccuracyAnalysis(date) => {
//      transaction {
//        val stocks: List[Stock] = Stock.findStocksByNextEarningsDate(date, date)
//
//        val completedAnalyses = stocks.map(x => {
//          StockDataForAnalysis(x,
//            RecommendationAnalysis.findCompletedAnalysesForStockForDate(x.id, x.next_earnings_date),
//            TimeSeriesData.load(x.symbol, DataSource.StockQuote, None, 1))
//        })
//
//        completedAnalyses.foreach(ca => {
//          ca.analyses.foreach(a => {
//            val engines = RecommendationAnalysisEngine.findByAnalysisId(a.id)
//            engines.foreach(e => {
//              val latestStockDataChange = {
//                if (ca.data.length > 0)
//                  Some(Json.parse(ca.data.head.data).validate[StockQuote].get)
//                else
//                  None
//              }
//
//              latestStockDataChange.map(sd => {
//                val engineAccuracy = AccuracyActor.calculateAccuracy(e.result.get, sd)
//                RecommendationAnalysisEngine.updateAccuracy(e.id, engineAccuracy)
//              })
//            })
//          })
//        })
//      }
//    }
  }
}

//object AccuracyActor {
//
//  def floor(value: BigDecimal) : BigDecimal = {
//    if ( value > 0 && value < 0.5)
//      BigDecimal(0.5)
//    else if ( value < 0 && value > -0.5)
//      BigDecimal(-0.5)
//    else {
//      value
//    }
//
//  }
//
//  def calculateAccuracy(result: BigDecimal, quote: StockQuote): Option[BigDecimal] = {
//    try {
//      //val analysisResult = analysis.result.get
////      println("Analysis Result: " + result)
//      val closePrice = BigDecimal(quote.LastTradePriceOnly.getOrElse("0"))
////      println("Closing Prince: " + closePrice)
//      val pointChange = BigDecimal(quote.Change.getOrElse("0"))
////      println("Point Change: " + pointChange)
//      val previousPrice = closePrice - pointChange;
////      println("Previous Price: " + previousPrice)
//      val percentChange = (pointChange/previousPrice) * 100
//
////      println("Percent Change: " + percentChange)
//
//      val predictionPercentChange = floor((result - 70)/3);
//
////      println("Predicted Percent Change: " + predictionPercentChange)
//      //val accuracy = (percentChange/predictionPercentChange) * 100
//      //println("Accuracy: " + accuracy)
//
//
////      val maxAccuracy = (percentChange - predictionPercentChange)
//
// //     val percentDifference = percentChange - predictionPercentChange;
//
//      val accuracy = {
//
//        if ( predictionPercentChange > 0 && percentChange > 0 ) {
//          percentChange * 5
//        }
//        else if ( predictionPercentChange < 0 && percentChange < 0) {
//          percentChange.abs * 5
//        }
//        else if (predictionPercentChange > 0 && percentChange < 0) {
//          percentChange * 5
//        }
//        else {
//          -percentChange * 5
//        }
//      }
//
////      println("Impact: " + accuracy)
//
//
//
//      Some(accuracy)
////      val normalizedAccuracy = {
////        if ( accuracy > 100 )
////          BigDecimal(100)
////        else if ( accuracy < -100) {
////          BigDecimal(-100)
////        }
////        else {
////          accuracy
////        }
////      }
////      Some(normalizedAccuracy)
//
//    } catch {
//      case e: Exception =>
//        None
//    }
//
//
//  }
//}





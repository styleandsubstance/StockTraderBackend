package actors

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask

import scala.concurrent.duration._
import org.squeryl._
import model.SquerylEntryPoint._
import model.RecommendationAnalysis
import model.RecommendationAnalysisEngine
import analysis.IRecommendationAnalysisRun
import analysis.IRecommendationAnalysisRun

import scala.concurrent.Await
import akka.util.Timeout
import model.Library
import model.DataSource
import web._
import web.AmericanBullsScraper._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import model.TimeSeriesData
import model.DataSource._
import web.NasdaqEarningsDateScraper._
import web.YahooFinanceAPI._
import web.MarketWatchScraper._
import web.QuarterlyResults._

//case class LoadData(symbol: String, dataSource: DataSourceEnum)

//class StockDataActor extends Actor {
//  def receive = {
//    
//    case LoadData(symbol, dataSource) => {
//      println("received request");
//      //get the specific DataSource
//      val json: Option[String] = { 
//        try {
//          dataSource match {
//      
//            case DataSource.AmericanBulls => {
//              println("--------------Got an American Bulls data Request");
//              val results = 
//                AmericanBullsScraper.getTradingHistory(symbol)
//          
//              println(results);  
//              Some(Json.toJson(results).toString())
//            }
//            case DataSource.NasdaqHistoricalEps => {
//              println("Got Historical EPS request")
//              val results = 
//                NasdaqEarningsDateScraper.getStockEarningsHistory(symbol)
//              
//              results.map { x => Json.toJson(results).toString()}  
//            }
//            case DataSource.YahooFinance => {
//              println("Got Yahoo Finance request")
//              val results = 
//                YahooFinanceAPI.getYahooFinanceData(symbol)
//                
//              Some(Json.toJson(results).toString())
//            }
//          }
//        } 
//        catch {
//          case e: Exception =>
//            e.printStackTrace()
//            None
//        }
//      }
//      
//      val toReturn: Option[TimeSeriesData] = json match {
//        case Some(jsonString) => {
//          val timeSeriesData = transaction {
//            TimeSeriesData.add(symbol, dataSource, jsonString)
//          }
//          Some(timeSeriesData)
//        }
//        case None => {
//          None
//        }
//        
//      }
//      
//      sender ! toReturn
//    }
//  } 
//}

object StockDataActor {

    def LoadData(symbol: String, dataSource: DataSourceEnum) : Option[TimeSeriesData] = {
      println("received request");
      //get the specific DataSource
      val json: Option[String] = { 
        try {
          dataSource match {
      
            case DataSource.AmericanBulls => {
              println("Got an American Bulls data Request");
              val results = 
                AmericanBullsScraper.getTradingHistory(symbol)
          
              println(results);  
              Some(Json.toJson(results).toString())
            }
            case DataSource.NasdaqHistoricalEps => {
              println("Got Historical EPS request")
              val results = 
                NasdaqEarningsDateScraper.getStockEarningsHistory(symbol)
              
              results.map { x => Json.toJson(results).toString()}  
            }
            case DataSource.YahooFinance => {
              println("Got Yahoo Finance request")
              val results =
                YahooFinanceAPI.getKeyStatistics(symbol)

              Some(Json.toJson(results).toString())
            }
            case DataSource.MarketWatchQuarterly => {
              println("Got MarketWatch Quarterly request")
              val results =
                MarketWatchScraper.getQuarterlyIncomeStatement(symbol)

              Some(Json.toJson(results).toString())
            }
            case DataSource.SymbolIndustry => {
              val results = YahooFinanceAPI.getSymbolIndustry(symbol)

              Some(Json.toJson(results).toString())
            }
            case DataSource.MarketWatchKeyStatistics => {
              val results = MarketWatchScraper.getKeyStatistics(symbol)

              Some(Json.toJson(results).toString())
            }

          }
        } 
        catch {
          case e: Exception =>
            e.printStackTrace()
            None
        }
      }
      
      val toReturn: Option[TimeSeriesData] = json match {
        case Some(jsonString) => {
          val timeSeriesData = 
            TimeSeriesData.add(symbol, dataSource, jsonString)
          
          Some(timeSeriesData)
        }
        case None => {
          None
        }
        
      }
      
      toReturn
    }
}


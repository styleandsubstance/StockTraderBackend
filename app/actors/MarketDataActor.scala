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
import model.MarketDataSource
import model.MarketDataSource._
import web.YahooFinanceAPI
import web.YahooFinanceAPI._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import model.MarketTimeSeriesData


case class LoadMarketData(marketDataSource: MarketDataSource)

//class MarketDataActor  extends Actor {
//  def receive = {
//    case LoadMarketData(marketDataSource) => {
//      println("received request");
//      val json: Option[String] = marketDataSource match {
//        case  MarketDataSource.IndustryPeRatio => {
//          println("Got request for Industry PE Ratios")
//          
//          val results =
//            YahooFinanceAPI.industryPeRatios()
//          
//          Some(Json.toJson(results).toString())
//        }
//      }
//      
//      val toReturn: Option[MarketTimeSeriesData] = json match {
//        case Some(jsonString) => {
//          val timeSeriesData = transaction {
//            MarketTimeSeriesData.add(marketDataSource, jsonString)
//          }
//          Some(timeSeriesData)
//        }
//        case None => {
//          None
//        }
//      }
//      
//      sender ! toReturn
//    }
//  }
//}

object MarketDataActor {
    def LoadMarketData(marketDataSource: MarketDataSource): Option[MarketTimeSeriesData] = {
      println("received request");
      val json: Option[String] = marketDataSource match {
        case  MarketDataSource.IndustryPeRatio => {
          println("Got request for Industry PE Ratios")
          
          val results =
            YahooFinanceAPI.industryPeRatios()
          
          Some(Json.toJson(results).toString())
        }
      }
      
      val toReturn: Option[MarketTimeSeriesData] = json match {
        case Some(jsonString) => {
          val timeSeriesData = 
            MarketTimeSeriesData.add(marketDataSource, jsonString)
          
          Some(timeSeriesData)
        }
        case None => {
          None
        }
      }
      
      toReturn
    }
}
package controllers

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

import play.api._
import play.api.mvc._
import play.api.libs.json._
import web.{EarningsData, NasdaqEarningsDateScraper}
import java.util.Date

import model._
import org.squeryl._
import model.SquerylEntryPoint._
import java.util.ArrayList
import javax.inject.Inject

import actors.{AccuracyActor, BeginNasdaqEarnignsDateScraping, RetrieveStockQuotes, WebScraper}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import akka.actor.Props
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import service.{RecommendationAnalysisSearcher, SearchParams}

class RecommendationAnalysisAPI @Inject() (recommendationAnalysisSearcher: RecommendationAnalysisSearcher) extends Controller {
  

  def get(startDate: String, endDate: String, globalSearch: Option[String], sortParam: Option[String], descending: Option[Boolean]) = Action {

    val searchParams = SearchParams(DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(startDate).toDate,
      DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(endDate).toDate, globalSearch)
    val sortField = Class.forName("model.RecommendationAnalysis").getDeclaredField(sortParam.getOrElse("result"))
    val sortParams = SortParams(sortField, descending.getOrElse(true))

    val recommendations = transaction {
      val recommendations = recommendationAnalysisSearcher.search(searchParams, sortParams, 1000, 0)
      recommendationAnalysisSearcher.count(searchParams)
      recommendations
    }

    implicit val recommendationAnalysisWrites = new Writes[(RecommendationAnalysis, Stock)] {
      def writes(value: (RecommendationAnalysis, Stock)): JsValue = {
        Json.obj(
          "id" -> value._1.id,
          "name" -> value._1.name,
          "scheduled_time" -> value._1.scheduled_date,
          "analysis_run_date" -> value._1.analysis_run_time,
          "num_engines" -> value._1.num_engines,
          "num_engines_completed" -> value._1.num_engines_completed,
          "result" -> value._1.result,
          "accuracy" -> value._1.accuracy,
          "stock" -> Json.toJson(value._2)
        )
      }
    }


    Ok(Json.toJson(recommendations).toString)
  }


  def getAccuracyAnalyses(id: Long) = Action {

    val accuracyAnalyses: List[RecommendationAnalysisAccuracy] = transaction {
      RecommendationAnalysisAccuracy.findByAnalysisId(id)
    }

    implicit val recommendationAnalysisAccuracyWrites = new Writes[RecommendationAnalysisAccuracy] {
      def writes(value: RecommendationAnalysisAccuracy): JsValue = {
        Json.obj(
          "id" -> value.id,
          "name" -> AccuracyEngineEnum(value.accuracy_engine_id.toInt).toString,
          "analysis_id" -> value.analysis_id,
          "execution_date" -> value.execution_date,
          "accuracy" -> value.accuracy
        )
      }
    }

    Ok(Json.toJson(accuracyAnalyses).toString)

  }

//  def startScraping = Action {
//    Akka.system.actorOf(Props[WebScraper]) ! BeginNasdaqEarnignsDateScraping
//    Ok
//  }
//
//  def retrieveStockQuotes = Action {
//    val today = new DateTime("2016-06-01").toDate
//    val yesterday = new DateTime(today).plusDays(-1).toDate
//    println(today)
//    val stocksToUpdate = transaction {
//      val stocksWithEarningsDate =
//        Stock.findStocksByNextEarningsDate(yesterday, today)
//
//      println(stocksWithEarningsDate.length)
//      val stocksWithEarningsDateAndEarningsDateMatch =
//        stocksWithEarningsDate.filter(s => {
//          val stockData = TimeSeriesData.load(s.symbol, DataSource.NasdaqEarnings, None, 1).head
//          val nasdaqData = Json.parse(stockData.data).validate[EarningsData].get
//
//          println(s.next_earnings_date == today)
//
//          if ( nasdaqData.is_premarket && s.next_earnings_date == today ||
//            nasdaqData.is_premarket == false && s.next_earnings_date == yesterday)
//            true
//          else
//            false
//      })
//
//      stocksWithEarningsDateAndEarningsDateMatch.map(s => s.symbol)
//    }
//
//    Akka.system.actorOf(Props[AccuracyActor]) ! RetrieveStockQuotes(stocksToUpdate)
//    Ok
//
//  }
  
}
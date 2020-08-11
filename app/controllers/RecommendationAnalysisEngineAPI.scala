package controllers

import javax.inject.Inject

import model._
import model.SquerylEntryPoint._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, Controller}
import service.{RecommendationAnalysisEngineSearcher, RecommendationAnalysisSearcher, SearchParams}

/**
  * Created by sdoshi on 5/28/2016.
  */
class RecommendationAnalysisEngineAPI @Inject() (recommendationAnalysisEngineSearcher: RecommendationAnalysisEngineSearcher) extends Controller {





  def get(startDate: String, endDate: String, globalSearch: Option[String], sortParam: Option[String], descending: Option[Boolean]) = Action {

    val searchParams = SearchParams(DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(startDate).toDate,
      DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(endDate).toDate, globalSearch)

    println(searchParams.startDate)
    println(searchParams.endDate)

    val sortField = Class.forName("model.RecommendationAnalysisEngine").getDeclaredField(sortParam.getOrElse("result"))
    val sortParams = SortParams(sortField, descending.getOrElse(true))

    val recommendationEngines = transaction {
      val recommendationEngines = recommendationAnalysisEngineSearcher.search(searchParams, sortParams, 5000, 0)
      recommendationEngines
    }

    implicit val recommendationAnalysisEngineSearchWrites = new Writes[(RecommendationAnalysisEngine, RecommendationAnalysis, Stock)] {
      def writes(value: (RecommendationAnalysisEngine, RecommendationAnalysis, Stock)): JsValue = {
        Json.obj(
          "id" -> value._1.id,
          "engine_id" -> value._1.engine_id,
          "engine_name" -> RecommendationEngineEnum(value._1.engine_id.toInt).toString,
          "analysis_id" -> value._1.analysis_id,
          "weight" -> value._1.weight,
          "result" -> value._1.result,
          "analysis_result" -> value._2.result,
          "accuracy" -> value._1.accuracy,
          "symbol" -> value._3.symbol
        )
      }
    }


    Ok(Json.toJson(recommendationEngines).toString)
  }
}

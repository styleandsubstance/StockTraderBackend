package controllers

import model.Library
import play.api.mvc.{Action, Controller}
import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json._
import model.RecommendationEngine._

/**
  * Created by sdoshi on 5/29/2016.
  */
class RecommendationEngineAPI extends Controller {

  def get = Action {
    val recommendationEngines = transaction {
      Library.recommendationEngine.allRows.toList
    }

    Ok(Json.toJson(recommendationEngines))

  }

}

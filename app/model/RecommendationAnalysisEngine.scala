package model

import java.util.Date

import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json.{JsValue, Json, Writes}

class RecommendationAnalysisEngine(var id: Long,
  var analysis_id: Long,
  var engine_id: Long,
  var weight: BigDecimal,
  var result: Option[BigDecimal],
  var accuracy: Option[BigDecimal]) extends KeyedEntity[Long]  {
  
}


object RecommendationAnalysisEngine {

  def findById(recommendationAnalysisEngineId: Long): RecommendationAnalysisEngine  =
    from(Library.recommendationAnalysisEngine)(s => where(s.id === recommendationAnalysisEngineId) select(s)).single
 
  def findByAnalysisId(recommendationAnalysisId: Long): List[RecommendationAnalysisEngine]  = 
    from(Library.recommendationAnalysisEngine)(s => where(s.analysis_id === recommendationAnalysisId) select(s)).toList

  def updateAccuracy(engine_id: Long, accuracy: Option[BigDecimal]) = {
    update(Library.recommendationAnalysisEngine)(s =>
      where(s.id === engine_id)
        set(s.accuracy := accuracy))
  }
    
}
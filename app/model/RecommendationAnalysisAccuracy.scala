package model

import java.util.Date
import model.SquerylEntryPoint._
import org.squeryl._
import org.squeryl.KeyedEntity

/**
  * Created by sdoshi on 9/1/2016.
  */
class RecommendationAnalysisAccuracy(var id: Long,
                                     var execution_date: Date,
                                     var analysis_id: Long,
                                     var accuracy_engine_id: Long,
                                     var accuracy: Option[BigDecimal]) extends KeyedEntity[Long] {

}

object RecommendationAnalysisAccuracy {


  def findById(id: Long): RecommendationAnalysisAccuracy = {
    from(Library.recommendationAnalysisAccuracy)(s => where(s.id === id) select(s)).single
  }

  def updateAccuracy(id: Long, accuracy: Option[BigDecimal]) = {
    update(Library.recommendationAnalysisAccuracy)(s =>
      where(s.id === id)
        set(s.accuracy := accuracy))
  }


  def findByExecutionDate(date: Date) : List[RecommendationAnalysisAccuracy] = {
    from(Library.recommendationAnalysisAccuracy)(s => where(s.execution_date === date) select(s)).toList
  }

  def findByAnalysisId(recommendationAnalysisId: Long): List[RecommendationAnalysisAccuracy] = {
    from(Library.recommendationAnalysisAccuracy)(s => where(s.analysis_id === recommendationAnalysisId) select(s) orderBy(s.accuracy_engine_id)).toList
  }
}
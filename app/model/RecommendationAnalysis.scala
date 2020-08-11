package model

import java.sql.Timestamp
import java.util.Date

import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json.{JsValue, Json, Writes}

class RecommendationAnalysis(var id: Long,
                             var stock_id: Long,
                             var name: String,
                             var scheduled_earnings_date: Date,
                             var scheduled_date: Date,
                             var analysis_run_time: Option[Timestamp],
                             var num_engines: Long,
                             var num_engines_completed: Long,
                             var result: Option[BigDecimal],
                             var accuracy: Option[BigDecimal]) extends KeyedEntity[Long]  {


  lazy val stock: Stock = {
    Library.stockToRecommendationAnalyses.right(this).head
  }

  lazy val engines: List[RecommendationAnalysisEngine] = {
    Library.recommendationAnalysisToEngines.left(this).toList
  }

}

object RecommendationAnalysis {

  def findById(recommendationAnalysisId: Long): RecommendationAnalysis  =
    from(Library.recommendationAnalysis)(s => where(s.id === recommendationAnalysisId) select(s)).single

  def findByIdForUpdate(recommendationAnalysisId: Long): RecommendationAnalysis  =
    from(Library.recommendationAnalysis)(s => where(s.id === recommendationAnalysisId) select(s)).forUpdate.single

  def findByStockId(stockId: Long) : List[RecommendationAnalysis] = {
    from(Library.recommendationAnalysis)(s => where(s.stock_id === stockId) select(s)).toList
  }
  
  def findCompletedAnalysesForStockForDate(stock_id: Long, date: Date) : List[RecommendationAnalysis] = {
    from(Library.recommendationAnalysis, Library.stock)((ra, s) =>
      where(ra.stock_id === s.id and 
          ra.stock_id === stock_id and
          s.next_earnings_date === date and
          ra.result.isNotNull)
      select(ra)).toList
  }
  
  def updateAccuracy(analysis_id: Long, accuracy: Option[BigDecimal]) = {
    update(Library.recommendationAnalysis)(ra =>
      where(ra.id === analysis_id)
      set(ra.accuracy := accuracy))
  }


  def updateScheduledEarningsDate(analysis_id: Long, date: Date) = {
    update(Library.recommendationAnalysis)(ra =>
      where(ra.id === analysis_id)
        set(ra.scheduled_earnings_date := date))
  }
}
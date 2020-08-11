package model

import org.squeryl._
import model.SquerylEntryPoint._
import java.lang.Long

object Library extends Schema {
  
  val stock = table[Stock]("stock")
  on(stock)(s => declare(
    s.symbol is (unique),  
    s.id is (autoIncremented("stock_id_seq") )
  ))
  
  val recommendationEngine = table[RecommendationEngine]("recommendationengine")
  on(recommendationEngine)(r => declare(
    r.id is (autoIncremented("recommendationengine_id_seq") )
  ))
  
  val recommendationAnalysis = table[RecommendationAnalysis]("recommendationanalysis")
  on(recommendationAnalysis)(r => declare(
    r.id is (autoIncremented("recommendationanalysis_id_seq") )
  ))
  
  val recommendationAnalysisEngine = table[RecommendationAnalysisEngine]("recommendationanalysisengine")
  on(recommendationAnalysisEngine)(r => declare(
    r.id is (autoIncremented("recommendationanalysisengine_id_seq") )
  ))
  
  val timeSeriesData = table[TimeSeriesData]("timeseriesdata")
  on(timeSeriesData)(r => declare(
    r.id is (autoIncremented("timeseriesdata_id_seq")),
    r.data is (dbType("jsonb").explicitCast)
  ))
  
  val marketTimeSeriesData = table[MarketTimeSeriesData]("markettimeseriesdata")
  on(marketTimeSeriesData)(r => declare(
    r.id is (autoIncremented("markettimeseriesdata_id_seq")),
    r.data is (dbType("jsonb").explicitCast)
  ))
  
  
  val recommendationAnalysisToEngines =
    oneToManyRelation(recommendationAnalysis, recommendationAnalysisEngine).
      via((a, e) => a.id === e.analysis_id)

  val stockToRecommendationAnalyses = 
  	oneToManyRelation(stock, recommendationAnalysis).
      via((s, ra) => s.id === ra.stock_id)
      
  val stockToTimeSeriesData = 
  	oneToManyRelation(stock, timeSeriesData).
      via((s, t) => s.id === t.stock_id)


  val recommendationAnalysisAccuracy = table[RecommendationAnalysisAccuracy]("recommendationanalysisaccuracy")
  on(recommendationAnalysisAccuracy)(r => declare(
    r.id is (autoIncremented("recommendationanalysisaccuracy_id_seq"))
  ))

  val recommendationAnalysisAccuracyToAnalysis =
    oneToManyRelation(recommendationAnalysis, recommendationAnalysisAccuracy).
      via((a, ac) => a.id === ac.analysis_id)

  val recommendationAnalysisEngineAccuracy = table[RecommendationAnalysisEngineAccuracy]("recommendationanalysisengineaccuracy")
  on(recommendationAnalysisEngineAccuracy)(r => declare(
    r.id is (autoIncremented("recommendationanalysisengineaccuracy_id_seq"))
  ))

  val recommendationAnalysisEngineAccuracyToAnalysis =
    oneToManyRelation(recommendationAnalysis, recommendationAnalysisEngineAccuracy).
      via((a, ac) => a.id === ac.analysis_id)

  val recommendationAnalysisEngineAccuracyToEngine =
    oneToManyRelation(recommendationAnalysisEngine, recommendationAnalysisEngineAccuracy).
      via((a, ac) => a.id === ac.analysis_engine_id)

}
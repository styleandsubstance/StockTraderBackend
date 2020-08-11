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
import model.RecommendationAnalysisEngine
import model.RecommendationAnalysisEngine
import model.RecommendationAnalysisEngine
import model.RecommendationAnalysisEngine
import model.RecommendationEngineEnum


case class StartRecommendationAnalysis(analysisId: Long)
case class RunRecommendationAnalysisEngine(symbol: String, analysisId: Long, engineId: Long, 
    engine: IRecommendationAnalysisRun)
case class CompleteRecommendationAnalysis(analysisId: Long)

class RecommendationAnalysisActor extends Actor {
  def receive = {
    case StartRecommendationAnalysis(analysisId) => {
      println("StartRecommendationAnalysis" + analysisId)
      val analysis = transaction {
        val analysis = RecommendationAnalysis.findByIdForUpdate(analysisId);
        analysis.engines
        analysis.stock
        analysis
      }
      
      analysis.engines.foreach( x => {
        val engineToRun: IRecommendationAnalysisRun = 
          Class.forName("analysis." + RecommendationEngineEnum(x.engine_id.toInt)).newInstance().asInstanceOf[IRecommendationAnalysisRun]
        
        println("Running engine: " + x.engine_id)
        
//        context.system.actorOf(Props[RecommendationAnalysisActor]) !
//          RunRecommendationAnalysisEngine(analysis.stock.symbol, analysisId, x.id, engineToRun)

        self ! RunRecommendationAnalysisEngine(analysis.stock.symbol, analysisId, x.id, engineToRun)
      })
    }
    case RunRecommendationAnalysisEngine(symbol, analysisId, engineId, engine) => {
      println("Running engine: " + engineId + " for symbol" + symbol + " for class: " + engine)
      val result: BigDecimal = {
        try {
          engine.run(symbol)  
        } catch {
          case e: Exception => 
            e.printStackTrace()
            BigDecimal(0)
        }
      }
      val updatedAnalysis: RecommendationAnalysis = transaction {
        val analysisEngine = RecommendationAnalysisEngine.findById(engineId)
        analysisEngine.result = Option(result)
        Library.recommendationAnalysisEngine.update(analysisEngine)
        
        val analysis = RecommendationAnalysis.findByIdForUpdate(analysisId)
        analysis.num_engines_completed = analysis.num_engines_completed + 1
        Library.recommendationAnalysis.update(analysis)
        analysis
      }
      println("Done with engine with result: " + result)
      
      if ( updatedAnalysis.num_engines_completed == updatedAnalysis.num_engines) {
         //context.system.actorOf(Props[RecommendationAnalysisActor]) ! CompleteRecommendationAnalysis(analysisId)
        self ! CompleteRecommendationAnalysis(analysisId)
      }
    }
    case CompleteRecommendationAnalysis(analysisId) => {
      transaction {
        val analysis = RecommendationAnalysis.findByIdForUpdate(analysisId)
        
        val analysisResults: List[RecommendationAnalysisEngine] = 
          RecommendationAnalysisEngine.findByAnalysisId(analysisId)
        
        val analysisResult = 
          analysisResults.foldLeft[BigDecimal](BigDecimal(0))((a, b) => a + (b.result.getOrElse(BigDecimal(0)) * b.weight))
          analysis.result = Option(analysisResult)
          Library.recommendationAnalysis.update(analysis)
      }

      context.stop(self)
    }
  }
}
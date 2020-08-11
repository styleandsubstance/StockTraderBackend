package actors

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import java.util.Date
import javax.inject.Inject

import accuracy.IRecommendationAnalysisAccuracyRun
import model._
import org.squeryl._
import model.SquerylEntryPoint._
import web.{EarningsData, StockQuote, YahooFinanceAPI}
import web.YahooFinanceAPI._
import org.joda.time.DateTime
import play.api.libs.json._
import web.NasdaqEarningsDateScraper._

import scala.util.{Failure, Try}


case class RunAccuracyEngine(recommendationAnalysisAccuracyId: Long, accuracyEngine: IRecommendationAnalysisAccuracyRun)

/**
  * Created by sdoshi on 11/28/2016.
  */
class RecommendationAnalysisAccuracyActor extends Actor {
  def receive = {
    case RunAccuracyEngine(recommendationAnalysisAccuracyId, accuracyEngineToRun) => {
      val success = Try(accuracyEngineToRun.run(recommendationAnalysisAccuracyId, None))
      success match {
        case Failure(f) => f.printStackTrace()
        case default => println("Success")
      }

      context.stop(self)
    }
  }
}

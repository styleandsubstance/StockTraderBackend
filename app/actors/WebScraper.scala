package actors

import akka.actor.{Actor, ActorSystem, Props}
import org.apache.commons.mail.Email
import org.apache.commons.mail.SimpleEmail
import play.Play
import play.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import org.joda.time.DateTime
import web._
import org.joda.time.format.DateTimeFormat
import java.util.Date

import model._
import org.squeryl._
import model.SquerylEntryPoint._
import java.util.ArrayList
import javax.inject.Inject

import org.joda.time.DateTime
import akka.actor.Actor
import org.joda.time.Days
import play.api.libs.json.Json
import web.NasdaqEarningsDateScraper._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import util.DateUtils
import web.YahooFinanceAPI._


case class BeginAmericanBullsScraping();
case class BeginNasdaqEarnignsDateScraping();
case class RetrieveStockQuotes();


class WebScraper extends Actor {
  def receive = {
    case BeginAmericanBullsScraping => {
      Logger.info("Running American Bull Scraper");
      
//      val recommendations = AmericanBullsScraper.getTradingHistory("DSKX")
//      
//      val lastKnownRecommendationDate = DateTime.parse("02/12/2016", DateTimeFormat.forPattern("MM/dd/yyyy")).toDate()
//      
//      if ( recommendations(0).date.after(lastKnownRecommendationDate) ) {
//        println("There is new info!")
//        try {
//          var email: Email = new SimpleEmail();
//          email.setHostName(Play.application().configuration().getString("notifications.smtp.address"));
//          email.setSmtpPort(Play.application().configuration().getString("notifications.smtp.port").toInt);
//          email.setAuthenticator(new DefaultAuthenticator(
//          		Play.application().configuration().getString("notifications.username"), 
//          		Play.application().configuration().getString("notifications.password")));
//          email.setSSLOnConnect(true);
//          email.setFrom(Play.application().configuration().getString("notifications.username"));
//          email.setSubject("New DSKX Trade");
//          email.setMsg("Hello Watson\n\n");
//          email.addTo("sdoshi@halcyoncs.onmicrosoft.com");
//          Logger.info("Sending e-mail");
//          email.send();
//          Logger.info("Done sending e-mail");
//        } catch {
//          case e: Exception => {
//          	Logger.error("Error while sending e-mail", e);
//          }
//        }
//        
//      }
    }
//    case UpdateIndustrySectorPeRatios => {
//      println("Making Yahoo request")
//      val url = "https://biz.yahoo.com/p/csv/sum_conameu.csv"
//      
//      WS.url(url).get().map {
//        response => {
//          println(response.body.toString())
//        }
//      }
//    }
    case RetrieveStockQuotes => {

      val startDate = new DateTime(new Date()).plusDays(-8)
      val endDate = new DateTime(new Date()).plusDays(8)

      val stocksToQuote = transaction {
        Stock.findStocksByNextEarningsDate(startDate.toDate, endDate.toDate)
      }

      val listsOfSymbols = stocksToQuote.map(s => s.symbol).grouped(100)

      listsOfSymbols.foreach(x => {
        val stockQuotes : List[StockQuote] = YahooFinanceAPI.getStockQuote(x)
        transaction {
          stockQuotes.foreach(x => {
            val json = Json.toJson(x).toString()
            TimeSeriesData.add(x.symbol, DataSource.StockQuote, json)
          })
        }
      })
    }
    
    case BeginNasdaqEarnignsDateScraping => {
      
      //determine the latest earnings date that we have gotten data for
      val lastDateOfEarningsData: Date = transaction {
        Stock.findLatestEarningsDate().getOrElse(new Date())
      }
      val startDate1 = new DateTime(lastDateOfEarningsData)
      val startDate = startDate1.plusDays(1)
      
      println(startDate)
      
      val endDate = new DateTime(new Date()).plusDays(14)
      
      println(endDate)
      
      val daysToScrape: Int = Days.daysBetween(startDate, endDate).getDays
      
      println(daysToScrape)
      
      val dateRange: List[Date] = List.range(1, daysToScrape).map(d => startDate.plusDays(d).toDate())
      //val dateRange: List[Date] = List(new Date())
      
      println("Staring BeginNasdaqEarnignsDateScraping")
      
      println("Num Dates: " + dateRange.length)
      
      val recommendations: List[EarningsData] = 
        dateRange.map(d => {
          println(d)
          NasdaqEarningsDateScraper.getStocksByEarningsDate(d)
        }).flatten

      val filteredRecommendations = recommendations.filter(earningsData => {
        earningsData.symbol == earningsData.symbol.toUpperCase() &&
          earningsData.symbol.contains(" ") == false
      })

      filteredRecommendations.foreach { r =>
        val analysis = transaction {
          val stockOption: Option[Stock] = Stock.findBySymbol(r.symbol)

          val stock: Stock = {
            if (stockOption.isDefined) {
              Stock.updateNextEarningsDate(stockOption.get.id, r.earnings_date)
              Stock.getBySymbol(stockOption.get.symbol)
            }
            else {
              Library.stock.insert(new Stock(0, r.symbol, r.company_name, r.earnings_date, "NASDAQ"))
            }
          }

//          val stock: Stock = stockOption.getOrElse(
//
//          )
          
          val nasdaqEarnings = TimeSeriesData.add(
              stock.symbol, DataSource.NasdaqEarnings, Json.toJson(r).toString())
          
          //create a default analysis to be run
          val recommendationAnalysis = 
            Library.recommendationAnalysis.insert(new RecommendationAnalysis(0, stock.id, "Default", r.earnings_date, new Date(), None, 7, 0, None, None));
          Library.recommendationAnalysisEngine.insert(
              new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.AmericanBulls.id, 0.15, None, None))
          Library.recommendationAnalysisEngine.insert(
              new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.NasdaqEarningsPositiveEpsHistory.id, 0.25, None, None))
          Library.recommendationAnalysisEngine.insert(
              new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.NasdaqEarningsTimeOfDay.id, 0.10, None, None))
          Library.recommendationAnalysisEngine.insert(
              new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.YahooFinanceCompanyValue.id, 0.15, None, None))
          Library.recommendationAnalysisEngine.insert(
              new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.IndustryPriceToEarningsRatio.id, 0.10, None, None))
          Library.recommendationAnalysisEngine.insert(
            new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.QuarterToQuarterRevenueGrowth.id, 0.20, None, None))
          Library.recommendationAnalysisEngine.insert(
            new RecommendationAnalysisEngine(0, recommendationAnalysis.id, RecommendationEngineEnum.RevenuePerEmployee.id, 0.05, None, None))


          Library.recommendationAnalysisAccuracy.insert(
            new RecommendationAnalysisAccuracy(0, DateUtils.subtractBusinessDays(r.earnings_date, 0, r.is_premarket),
              recommendationAnalysis.id, AccuracyEngineEnum.FiveTradingDaysRunUpAccuracy.id, None))
          Library.recommendationAnalysisAccuracy.insert(
            new RecommendationAnalysisAccuracy(0, DateUtils.addBusinessDays(r.earnings_date, 0, r.is_premarket),
              recommendationAnalysis.id, AccuracyEngineEnum.AfterEarningsReportedAnalysisAccuracy.id, None))

          recommendationAnalysis    
        }
        
        
        println("Starting analysis for: " + analysis.id)
        
        val analysisActor = context.system.actorOf(Props[RecommendationAnalysisActor])


        analysisActor ! StartRecommendationAnalysis(analysis.id)
      }   
    }
  }
}
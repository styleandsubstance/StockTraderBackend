package controllers

import java.io.InputStream
import java.net.{HttpURLConnection, URL, URLConnection}

import play.api._
import play.api.mvc._
import web._
import java.util.Date

import web.YahooFinanceAPI._
import model._
import org.squeryl._
import model.SquerylEntryPoint._
import java.text.SimpleDateFormat
import javax.inject.Inject

import accuracy.FiveTradingDaysRunUpAccuracy
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.{ActorSystem, Props}
import actors.{BeginNasdaqEarnignsDateScraping, _}
import akka.util.Timeout
import analysis.{QuarterToQuarterRevenueGrowth, RevenuePerEmployee}
import play.api.libs.EventSource
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}

import scala.concurrent.duration._
import util.DateUtils

import scala.concurrent.Await
//import actors.LoadData
import model.DataSource
import model.TimeSeriesData
import model.Stock
import org.joda.time.DateTime
import play.api.libs.json._
import actors.AccuracyActor
import actors.RetrieveStockQuotes
import web.NasdaqEarningsDateScraper._


class Application @Inject()(system: ActorSystem) extends Controller {


  def test = Action {
    //Iteratee.ignore[String]
   // Enumerator()
    val (sachin, doshi) = ("Sachin", 1)
    Ok
  }



  def stream = Action { implicit req =>
    val (out, channel) = Concurrent.broadcast[String]
    Ok.feed(out &> EventSource()).as("text/event-stream")
  }

  def index = Action {

    //YahooFinanceAPI.getSymbolIndustry("msft")
    //println(YahooFinanceAPI.getKeyStatistics("XPLR"))

    //println(YahooFinanceAPI.getSymbolIndustry("XPLR"))


    //println(MarketWatchScraper.getKeyStatistics("AAPL"))

    //println(new RevenuePerEmployee().run("AAPL"))


//    val con : URLConnection = new URL( "http://biz.yahoo.com/p/rr/m/msft.html" ).openConnection();
//    System.out.println( "orignal url: " + con.getURL() );
//    con.connect();
//    System.out.println( "connected url: " + con.getURL() );
//    val is: InputStream = con.getInputStream();
//    System.out.println( "redirected url: " + con.getURL() );
//    is.close();




//    system.actorOf(Props[WebScraper]) ! BeginNasdaqEarnignsDateScraping

//    MarketWatchScraper.getRevenuePerEmployee("MSFT");
    
//    val today = new DateTime("2016-08-18").toDate
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


//    val dateTime = new DateTime("2016-08-18")
////    Akka.system.actorOf(Props[AccuracyActor]) ! PerformAccuracyAnalysis(dateTime.toDate)
//////////////////////
//    Akka.system.actorOf(Props[AccuracyActor]) ! PerformEngineAccuracyAnalysis(dateTime.toDate)


//    transaction {
//      val recommendationAnalysis = RecommendationAnalysis.findCompletedAnalysesForStockForDate(68, dateTime.toDate)
//      val stockQuote =  {
//        Json.parse(TimeSeriesData.load("CRI", DataSource.StockQuote, None, 1).head.data).validate[StockQuote].get
//      }
//      println(AccuracyActor.calculateAccuracy(recommendationAnalysis.head, stockQuote))
//    }

    //val test = NasdaqEarningsDateScraper.getStocksByEarningsDate(new Date())
    //val test = NasdaqEarningsDateScraper.getStockEarningsHistory("AAL")
    //println(test)


//    MarketWatchScraper.getQuarterlyIncomeStatement("bby")

    //val something: List[Option[Long]] = Some(4l) :: Some(3l) :: None :: Nil

    //val test = Json.toJson(something)


//    val revenueIncrease = transaction {
//      val increase = new QuarterToQuarterRevenueGrowth
//      increase.run("LEN")
//    }
//
//    println(revenueIncrease)

//    println(YahooFinanceAPI.getStockQuote(List("YHOO", "AAPL")))


//    val test = new FiveTradingDaysRunUpAccuracy
//    test.run(1, None)

    //println(DateUtils.addBusinessDays(new Date(), 1, false))
    //println(DateUtils.subtractBusinessDays(new Date(), 0, true))

//    transaction {
//      val reportingStocks = Stock.findStocksByNextEarningsDate(new DateTime("2016-09-05").toDate, new DateTime("2016-09-16").toDate)
//      reportingStocks.foreach(s => {
//
//        val nasdaqEarningsData = TimeSeriesData.loadLatest(
//          s.symbol, DataSource.NasdaqEarnings)
//
//        //val nasdaqEarnings: Na
//        val earningsData = Json.parse(nasdaqEarningsData.data).validate[EarningsData].get
//
//
//
//
//        val analyses = RecommendationAnalysis.findByStockId(s.id)
//        println("Stock: " + s.symbol + " is_premarket: " + earningsData.is_premarket + " num_analyses: " + analyses.size)
//              analyses.foreach(a => {
//                Library.recommendationAnalysisAccuracy.insert(
//                  new RecommendationAnalysisAccuracy(0, DateUtils.subtractBusinessDays(s.next_earnings_date, 0, earningsData.is_premarket),
//                    a.id, AccuracyEngineEnum.FiveTradingDaysRunUpAccuracy.id, None))
//                Library.recommendationAnalysisAccuracy.insert(
//                  new RecommendationAnalysisAccuracy(0, DateUtils.addBusinessDays(s.next_earnings_date, 0, earningsData.is_premarket),
//                    a.id, AccuracyEngineEnum.AfterEarningsReportedAnalysisAccuracy.id, None))
//
//              })
//
//
//      })
//
//    }

//
//    transaction {
//
//      val allStocks: List[Stock] = Stock.findAll
//
//      allStocks.foreach(s => {
//
//        val ra: List[RecommendationAnalysis] = RecommendationAnalysis.findByStockId(s.id)
//
//        val sorted = ra.sortBy(ra => ra.scheduled_date).reverse
//
//        //println(sorted(0).scheduled_date)
//        //println(sorted(1).scheduled_date)
//
//        if ( sorted.length > 1 ) {
//
//
//          val scheduledDate = new DateTime(s.next_earnings_date).minusDays(90).toDate
//
//          //println("Original Date:" + sorted(1).scheduled_date + " New Date: " + scheduledDate)
//          RecommendationAnalysis.updateScheduledEarningsDate(sorted(1).id, Some(scheduledDate));
//
//          //sorted(1)
//        }
//
//
//
//      })
//
//
//    }


//    val accuracyEngineToRun = new FiveTradingDaysRunUpAccuracy
//    accuracyEngineToRun.run(64, None)

    //val jodaTime = new DateTime(2016, 11, 11, 0, 0);
    //println(jodaTime)

    //val resolveTimeout2 = Timeout(5 seconds)

    //val actorRef = Await.result(system.actorSelection("user/AccuracyActor").resolveOne(), resolveTimeout2.duration)

//    val actorRef = system.actorSelection("user/WebScraper")
//    actorRef ! BeginNasdaqEarnignsDateScraping
    //actorRef ! LaunchRecommendationAnalysisAccuracyTasks(new Date())
    //system.actorOf(Props[WebScraper]) ! RetrieveStockQuotes

//    transaction {
//      println(RecommendationAnalysisAccuracy.findByExecutionDate(new Date).length)
//    }

    //println(Recomm)

    Ok(views.html.index("Yo mama ready"))
  }

}

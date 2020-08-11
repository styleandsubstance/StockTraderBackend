package web

import java.util.Date

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import web.RecommendationStatus.RecommendationStatus
import web.TransactionType.TransactionType
import play.api.libs.json._
import play.api.libs.functional.syntax._


object TransactionType extends Enumeration {
  type TransactionType = Value
  val BUY = Value("BUY")
  val SELL = Value("SELL")
  val SHORT = Value("SHORT")
  
  implicit val transactionTypeFormat = new Format[TransactionType] {
    def reads(json: JsValue) = JsSuccess(TransactionType.withName(json.as[String]))
    def writes(myEnum: TransactionType) = JsString(myEnum.toString)
  }
}

object RecommendationStatus extends Enumeration {
  type RecommendationStatus = Value
  val CORRECT = Value("CORRECT")
  val INCORRECT = Value("INCORRECT")
  
  implicit val recommendationStatusFormat = new Format[RecommendationStatus] {
    def reads(json: JsValue) = JsSuccess(RecommendationStatus.withName(json.as[String]))
    def writes(myEnum: RecommendationStatus) = JsString(myEnum.toString)
  }
}

case class AmericanBullsRecommendation(date: Date, transactionType: TransactionType, recommendationStatus: RecommendationStatus, targetPrice: Float)
case class AmericanBullsRecommendationHistory(recommendations: List[AmericanBullsRecommendation])



/**
 * Created by sdoshi on 2/20/2016.
 */
object AmericanBullsScraper {
  implicit val americanBullsRecommendationReads = Json.reads[AmericanBullsRecommendation]
  implicit val americanBullsRecommendationWrites = Json.writes[AmericanBullsRecommendation]
  implicit val americanBullsRecommendationHistoryReads = Json.reads[AmericanBullsRecommendationHistory]
  implicit val americanBullsRecommendationHistoryWrites = Json.writes[AmericanBullsRecommendationHistory]

  def convertTableDataToRecommendationStatus(tableData: Element) : RecommendationStatus = {
    val imgTag = tableData >> element("img")
    
    if ( imgTag.attr("src") == "img/Check.gif")
      RecommendationStatus.CORRECT
    else
      RecommendationStatus.INCORRECT
  }

  def convertTableRowToRecommendation(tableRow: Element) : AmericanBullsRecommendation = {
    val tableData = tableRow >> elementList("td")

    return new AmericanBullsRecommendation(DateTime.parse(text(tableData(0)), DateTimeFormat.forPattern("MM/dd/yyyy")).toDate,
      TransactionType.withName(text(tableData(2))),
      convertTableDataToRecommendationStatus(tableData(3)),
      text(tableData(1)).toFloat
    )
  }


  def getTradingHistory(symbol:String): AmericanBullsRecommendationHistory = {
    //val browser = new Browser
    val browser = new JsoupBrowser()
    val doc = browser.get("https://www.americanbulls.com/SignalPage.aspx?lang=en&Ticker=" + symbol)

    val sixMonthSignalTable: Element = doc >> element("#MainContent_signalpagehistory_PatternHistory6_DXMainTable")
    val rows: List[Element] = sixMonthSignalTable >> elementList("tr")
    
    val dataRows = rows.filter(p => {
      p.attrs.contains("id") && p.attr("id").matches("MainContent_signalpagehistory_PatternHistory6_DXDataRow\\d+")
    })
    return AmericanBullsRecommendationHistory(dataRows.map(x => convertTableRowToRecommendation(x)))
  }
}

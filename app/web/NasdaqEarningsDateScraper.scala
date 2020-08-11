package web

import java.util.Date
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import model.NasdaqData
import java.text.SimpleDateFormat
import play.api.libs.json._
import play.Logger


case class EarningsData(symbol: String, company_name: String, earnings_date: Date, market_cap: Long, 
    eps_forecast: BigDecimal, eps_previous_year: BigDecimal,  num_analysts: Long,  is_premarket: Boolean); 

case class EpsData(fiscalQuarterEnd: String, dateReported: Date, epsReported: BigDecimal, 
    consensusEpsForecast: BigDecimal, percentSurprise: BigDecimal)
case class HistoricalEpsData(historicalEpsData: List[EpsData])

object NasdaqEarningsDateScraper {
  implicit val epsDataReads = Json.reads[EpsData]
  implicit val epsDataWrites = Json.writes[EpsData]
  implicit val historicalEpsDataReads = Json.reads[HistoricalEpsData]
  implicit val historicalEpsDataWrites = Json.writes[HistoricalEpsData]
  implicit val earningsDataReads = Json.reads[EarningsData]
  implicit val earningsDataWrites = Json.writes[EarningsData]
  
  
  def parseStockSymbol(tableData: Element) : String = {
    val pattern = """(?<=\()[^)]+(?=\))""".r
    val toParse = text(tableData)
    
    pattern.findFirstIn(toParse).getOrElse("")
  }
  
  def parseCompanyName(tableData: Element) : String = {
    val pattern = "^[^\\](,]*".r
    val toParse = text(tableData)
    
    pattern.findFirstIn(toParse).getOrElse("").trim()
  }
  
  def parseMarketCap(tableData: Element) : Long = {
    val pattern = """(?<=Market Cap: ).*""".r
    val toParse = text(tableData)
  
    val marketCap = pattern.findFirstIn(toParse).getOrElse("$0.00M")
    try {
      marketCap.replace("$", "").replace(".", "").replace("M", "0000").replace("B", "0000000").toLong;
    } catch {
      case e: Exception =>
        0
    }

  }
  
  def parseEps(tableData: Element) : BigDecimal = {
    try {
      val toParse = text(tableData)
      BigDecimal(toParse.replace("$", ""))
    } catch {
      case e: Exception =>
       BigDecimal(0)
    }
    
  }
  
  def parseNumEstimates(tableData: Element) : Long = {
    text(tableData).toInt
  }
  
  def parseIsMorningAnnouncement(tableData: Element) : Boolean = {
    val imgTag = tableData >?> element("a img")
    imgTag.map(x => {
      if ( x.attr("alt") == "Pre-Market Quotes")
        true
      else
        false
    }).getOrElse(false)
  }
  
  def epsInfoToBigDecimal(tableData: Element): BigDecimal = {
    try {
      val toParse = text(tableData)
      BigDecimal(toParse)
    } catch {
      case e: Exception =>
       BigDecimal(0)
    }
  }
  
  
  def getStockEarningsHistory(symbol: String) : Option[HistoricalEpsData] = {
    try {
      val url = "http://www.nasdaq.com/symbol/" + symbol + "/earnings-surprise";

      val doc = new JsoupBrowser().get(url)
      
      val earningsSurpriseTable: Element = doc >> element(".genTable") >> element("table")
      val rows: List[Element] = (earningsSurpriseTable >> elementList("tr")).drop(1)
      
      val rowsAsColumnList = rows.map( r => r >> elementList("td"))
      
      val historicalEpsData = rowsAsColumnList.map(row => {
        val term = text(row(0))
        val dateReported = DateTime.parse(text(row(1)), DateTimeFormat.forPattern("MM/dd/yyyy")).toDate()
        val epsReported = epsInfoToBigDecimal(row(2))
        val consensusEpsForecast = epsInfoToBigDecimal(row(3))    
        val percentSurprise = epsInfoToBigDecimal(row(4))
        
        EpsData(term, dateReported, epsReported, consensusEpsForecast, percentSurprise)
        
      })
    
      Some(HistoricalEpsData(historicalEpsData))
    } catch {
      case e: Exception => {
        Logger.error("Error while parsing earnings historical data for: " + symbol, e)
        None
      }
    }
  }
  
  
  def getStocksByEarningsDate(date: Date) : List[EarningsData] = {
    //val browser = new Browser
    val browser = JsoupBrowser()
    val url_test = "http://www.nasdaq.com/earnings/earnings-calendar.aspx?date=" + 
        new SimpleDateFormat("YYYY-MMM-dd").format(date).toString()
    
    println(url_test)
    
    val doc = browser.get(url_test)
    
    //println(doc)
    
    //get list of stocks/earnings time/market caps
    try {
    	val earningsReport: Element = doc >> element("#ECCompaniesTable")
    	val rows: List[Element] = earningsReport >> elementList("tr")

    	val columns = rows.map (row => row >> elementList("td"))
    	val validRows = columns.filter(c => c.length > 0)

      val nasdaqEarnings = validRows.map (columns => {
        val rowAsString = columns.foldLeft("Column: ")((x,y) => x + text(y) + " ")
        val stockSymbol = parseStockSymbol(columns(1))
        val companyName = parseCompanyName(columns(1))
        val marketCap = parseMarketCap(columns(1))
        val epsForecast = parseEps(columns(4))
        val numEstimates = parseNumEstimates(columns(5))
        val previousYearEps = parseEps(columns(7))
        val isPreMarketRelease = parseIsMorningAnnouncement(columns(0))
        
        EarningsData(stockSymbol, companyName, date, marketCap, epsForecast, previousYearEps, numEstimates, isPreMarketRelease)
      })
    
      nasdaqEarnings
    } catch {
      case e: Exception => {
        Logger.error("Error while parsing earnings data for: " + date, e)
        Nil
      }
    }
  }
}
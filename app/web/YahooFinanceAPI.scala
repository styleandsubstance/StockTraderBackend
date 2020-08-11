package web

import java.net.{HttpURLConnection, URL}
import javax.inject.Inject

import akka.util.Timeout
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

case class IndustryPeRatio(industry: String, oneDayPercenateChange: Float, marketCapitalization: String, peRatio: BigDecimal, 
    roePercentage: BigDecimal, divYield: Float, debtToEquity: Float, netProfit: BigDecimal, priceToFreeCashFlow: BigDecimal);
case class IndustryPeRatios( ratios: List[IndustryPeRatio])





//case class StockKeyStatistics(enterpriseValue: Option[String], enterpriseValueDate: Option[String]);
case class StockCompanyDetails(sector: Option[String], industry: Option[String], marketCapitalization: Option[String], 
    priceToEarningsTtm: Option[String])
//case class YahooFinanceData(stockKeyStatistics: StockKeyStatistics, stockCompanyDetails: StockCompanyDetails)


case class SymbolIndustry(sector: Option[String], industry: Option[String])


case class KeyStatistics(
    averageDailyVolume: Option[Long],
    bookValue: Option[BigDecimal],
    earningsShare: Option[BigDecimal],
    yearLow: Option[BigDecimal],
    yearHigh: Option[BigDecimal],
    marketCapitalization: Option[String],
    ebitda: Option[String],
    fiftydayMovingAverage: Option[BigDecimal],
    twoHundreddayMovingAverage: Option[BigDecimal],
    priceSales: Option[BigDecimal],
    priceBook: Option[BigDecimal],
    peRatio: Option[BigDecimal],
    pegRatio: Option[BigDecimal],
    priceEPSEstimateCurrentYear: Option[BigDecimal],
    priceEPSEstimateNextYear: Option[BigDecimal],
    volume: Option[Long]
)



case class StockQuote(symbol: String, 
    LastTradePriceOnly: Option[String], 
    LastTradeDate: Option[String], 
    LastTradeTime: Option[String], 
    Change: Option[String],
    Open: Option[String],
    DaysHigh: Option[String],
    DaysLow: Option[String],
    Volume: Option[String])
case class StockQuotes(lang: String, quote: List[StockQuote])

/**
 * Created by sdoshi on 2/20/2016.
 */
object YahooFinanceAPI {

  @Inject val wsClient: WSClient = null

  implicit val industryPeRatioReads = Json.reads[IndustryPeRatio]
  implicit val industryPeRatioWrites = Json.writes[IndustryPeRatio]
  implicit val industryPeRatiosReads = Json.reads[IndustryPeRatios]
  implicit val industryPeRatiosWrites = Json.writes[IndustryPeRatios]

//  implicit val stockKeyStatisticsReads = Json.reads[StockKeyStatistics]
//  implicit val stockKeyStatisticsWrites = Json.writes[StockKeyStatistics]
  implicit val stockCompanyDetailsReads = Json.reads[StockCompanyDetails]
  implicit val stockCompanyDetailsWrites = Json.writes[StockCompanyDetails]
//  implicit val yahooFinanceDataReads = Json.reads[YahooFinanceData]
//  implicit val yahooFinanceDataWrites = Json.writes[YahooFinanceData]
  implicit val keyStatisticsReads = Json.reads[KeyStatistics]
  implicit val keyStatisticsWrites = Json.writes[KeyStatistics]
  
  implicit val stockQuoteReads = Json.reads[StockQuote]
  implicit val stockQuoteWrites = Json.writes[StockQuote]

  implicit val symbolIndustryReads = Json.reads[SymbolIndustry]
  implicit val symbolIndustryWrites = Json.writes[SymbolIndustry]


  
  implicit val timeout = Timeout(5 minutes)

  
//  val yahooFinanceStockKeyStatisticsReads: Reads[StockKeyStatistics] = (
//      (JsPath \ "query" \ "results" \ "stats" \ "EnterpriseValue" \ "content").readNullable[String] and
//      (JsPath \ "query" \ "results" \ "stats" \ "EnterpriseValue" \ "term").readNullable[String]
//  )(StockKeyStatistics.apply _)
  
   val yahooFinanceStockCompanyDetailsReads: Reads[StockCompanyDetails] = (
      (JsPath \ "query" \ "results" \ "stock" \ "Sector").readNullable[String] and
      (JsPath \ "query" \ "results" \ "stock" \ "Industry").readNullable[String] and
      (JsPath \ "query" \ "results" \ "stock" \ "MarketCap").readNullable[String] and
      (JsPath \ "query" \ "results" \ "stock" \ "PEttm").readNullable[String]
  )(StockCompanyDetails.apply _) 
  
  val yahooFinanceStockQuotesReads: Reads[StockQuotes] = (
      (JsPath \ "query" \ "lang").read[String] and
      (JsPath \ "query" \ "results" \ "quote").read[List[StockQuote]]
  )(StockQuotes.apply _)

  def tryToOptionLong(s: Option[String]) : Option[Long] = {
    if (s.isEmpty) None else scala.util.Try(s.get.toLong).toOption
  }

  def tryToOptionBigDecimal(s: Option[String]) : Option[BigDecimal] = {
    if (s.isEmpty) None else scala.util.Try(BigDecimal(s.get.toDouble)).toOption
  }


  val yahooFinanceKeyStatistics: Reads[KeyStatistics] = (
    (JsPath \ "query" \ "results" \ "quote" \ "AverageDailyVolume").readNullable[String].map(x=> tryToOptionLong(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "BookValue").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "EarningsShare").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "YearLow").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "YearHigh").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "MarketCapitalization").readNullable[String] and
    (JsPath \ "query" \ "results" \ "quote" \ "EBITDA").readNullable[String] and
    (JsPath \ "query" \ "results" \ "quote" \ "FiftydayMovingAverage").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "TwoHundreddayMovingAverage").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PriceSales").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PriceBook").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PERatio").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PEGRatio").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PriceEPSEstimateCurrentYear").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "PriceEPSEstimateNextYear").readNullable[String].map(x=> tryToOptionBigDecimal(x)) and
    (JsPath \ "query" \ "results" \ "quote" \ "Volume").readNullable[String].map(x=> tryToOptionLong(x))
    )(KeyStatistics.apply _)



  val apiUrl = "https://query.yahooapis.com/v1/public/yql?format=json&callback="
  val apiKeyStatisticsEnv = "env=https://raw.githubusercontent.com/cynwoody/yql-tables/finance-1/tables.env"
  val apiDetailsEnv = "env=store://datatables.org/alltableswithkeys"
  
  
  def yahooStringToFloat(value: String) : Float = {
    try {
      value.toFloat
    } catch {
      case _: Throwable =>
        0f
    }
  }
  
  
  def industryPeRatios(): IndustryPeRatios = {
    println("Making Yahoo request")
    val url = "https://biz.yahoo.com/p/csv/sum_conameu.csv"
    
    implicit val timeout = Timeout(5 minutes)
        
    val result = Await.result(WS.url(url).get(), timeout.duration)

    val industries: List[IndustryPeRatio] = {
      //val response = result.response
        
        val lines: Array[String] = result.body.toString().split("[\\r\\n]+")
        val noHeaders = lines.drop(1).dropRight(1)
        
        noHeaders.toList.map { 
          x => {
            val lastQuote = x.indexOf('\"', 1)
            val industry = x.substring(1, lastQuote)
		        val restOfLine = x.substring(lastQuote + 2)
            
            val cols = restOfLine.split(",").map(_.trim)

            IndustryPeRatio(
                industry,
                yahooStringToFloat(cols(0)),
                cols(1),
                BigDecimal(cols(2)),
                BigDecimal(cols(3)),
                yahooStringToFloat(cols(4)),
                yahooStringToFloat(cols(5)),
                BigDecimal(cols(6)),
                BigDecimal(cols(7)))
                
          }
        }
    }
    IndustryPeRatios(industries)
  }
  
//  def getKeyStatistics(symbol: String): StockKeyStatistics = {
//
//    val query = "SELECT * FROM yahoo.finance.keystats WHERE symbol=\"" + symbol + "\""
//    val url = apiUrl + "&q=" + query + "&" + apiKeyStatisticsEnv;
//
//    println(url)
//
//    val response = Await.result(WS.url(url).get(), timeout.duration)
//
//    println(response)
//
//    response.json.as[StockKeyStatistics](yahooFinanceStockKeyStatisticsReads)
//  }
  
  def getStockCompanyDetails(symbol: String) : StockCompanyDetails = {
    val query = "SELECT * FROM yahoo.finance.stocks WHERE symbol=\"" + symbol + "\""
    val url = apiUrl + "&q=" + query + "&" + apiKeyStatisticsEnv;
    
    println(url)
    
    val response = Await.result(WS.url(url).get(), timeout.duration)
    
    println(response.json)

    response.json.as[StockCompanyDetails](yahooFinanceStockCompanyDetailsReads)
  }
//
//  def getYahooFinanceData(symbol: String) : YahooFinanceData = {
//    YahooFinanceData(getKeyStatistics(symbol),
//        getStockCompanyDetails(symbol))
//  }
  
  def getStockQuote(symbols: List[String]):  List[StockQuote] = {
    val stockListAsString = "( " + symbols.map(x => "\"" + x + "\"").mkString(",") + " )"
    
    val query = "SELECT * FROM yahoo.finance.quoteslist WHERE symbol IN " + stockListAsString
    val url = apiUrl + "&q=" + query + "&" + apiDetailsEnv;
    
    println(url)
    
    val response = Await.result(WS.url(url).get(), timeout.duration)
    //val response = Await.result(wsClient.url(url).get(), timeout.duration)
    
    println(response.json)
    
    val stockQuote = response.json.as[StockQuotes](yahooFinanceStockQuotesReads)
    
    stockQuote.quote
  }

  def getKeyStatistics(symbol: String): KeyStatistics = {
    val symbolIn = "(\"" + symbol.toUpperCase + "\")"

    val query = "SELECT * FROM yahoo.finance.quotes WHERE symbol IN " + symbolIn
    val url = apiUrl + "&q=" + query + "&" + apiDetailsEnv;

    println(url)

    val response = Await.result(WS.url(url).get(), timeout.duration)

    println(response.json)

    response.json.as[KeyStatistics](yahooFinanceKeyStatistics)
  }


  def getSymbolIndustry(symbol: String): SymbolIndustry = {
    val url = "https://biz.yahoo.com/p/rr/" + symbol.toLowerCase.charAt(0) + "/" + symbol.toLowerCase + ".html"

    val connection: HttpURLConnection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]


    connection.setInstanceFollowRedirects(true);
    connection.connect();
    val responseCode = connection.getResponseCode();
    //System.out.println( responseCode );
    val location = connection.getHeaderField("Location");
    println(location);

    val industryId = location.substring(location.lastIndexOf('/'))
    val industryCsv = industryId.replace(".html", ".csv")

    val csvUrl = "https://biz.yahoo.com/p/csv/" + industryCsv;

    println(csvUrl)

    val result = Await.result(WS.url(csvUrl).get(), timeout.duration)

    val symbolIndustry: SymbolIndustry = {
      val lines: Array[String] = result.body.toString().split("[\\r\\n]+")
      val noHeaders = lines.drop(1).dropRight(1)

      println(noHeaders.length)

      if (noHeaders.length < 2) {
        throw new Exception("Not enough information")
      }

      val sectorLine = noHeaders(0)
      val lastQuoteForSector = sectorLine.indexOf('\"', 1)
      val sector = sectorLine.substring(1, lastQuoteForSector)

      val industryLine = noHeaders(1)

      val lastQuote = industryLine.indexOf('\"', 1)
      val industry = industryLine.substring(1, lastQuote)

      SymbolIndustry(Some(sector), Some(industry))
    }

    symbolIndustry
  }
}

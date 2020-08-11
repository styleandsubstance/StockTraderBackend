package analysis

import model.TimeSeriesData
import model.DataSource
import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json._
import web.YahooFinanceAPI._
import web.{IndustryPeRatio, IndustryPeRatios, KeyStatistics, SymbolIndustry}
import model.MarketDataSource
import model.MarketDataSource._
import model.MarketTimeSeriesData
import org.joda.time.DateTime
import java.sql.Timestamp

case class IndustryPriceToEarningsRatioAnalysisData(yahooFinanceTimeSeriesData: TimeSeriesData,
                                                    yahooFinanceIndustryTimeSeriesData: TimeSeriesData,
                                                    industryPeRatiosTimeSeriesData: MarketTimeSeriesData)

class IndustryPriceToEarningsRatio extends IRecommendationAnalysisRun {
  
  def getOldestDataTimestamp = {
    val dateTime = new DateTime
    val yesterday = dateTime.plusDays(-1)
    new Timestamp(yesterday.toDate().getTime)
  }
  
  
  def run(symbol: String) : BigDecimal = {
    println("Running Yahoo Finance Industry PE Ratio analysis for " + symbol)
    
    val timeSeriesData = transaction {
      IndustryPriceToEarningsRatioAnalysisData(
          TimeSeriesData.loadLatest(symbol, DataSource.YahooFinance),
          TimeSeriesData.loadLatest(symbol, DataSource.SymbolIndustry),
          MarketTimeSeriesData.loadLatest(MarketDataSource.IndustryPeRatio, getOldestDataTimestamp))
    }

    val yahooFinanceData: KeyStatistics =
      Json.parse(timeSeriesData.yahooFinanceTimeSeriesData.data).validate[KeyStatistics].get


    val yahooFinanceIndustryData: SymbolIndustry =
      Json.parse(timeSeriesData.yahooFinanceIndustryTimeSeriesData.data).validate[SymbolIndustry].get


    val industryPeRatios: IndustryPeRatios =
      Json.parse(timeSeriesData.industryPeRatiosTimeSeriesData.data).validate[IndustryPeRatios].get
    
    println(yahooFinanceIndustryData.industry)
    val industryPeRatio: Option[IndustryPeRatio] = 
      industryPeRatios.ratios.find { ratio => ratio.industry == yahooFinanceIndustryData.industry.getOrElse("NONE") }
    
    println("Industry PE: " + industryPeRatio)
    
    val industryPeRatioForCompany: Float =
      industryPeRatio.map{x => x.peRatio}.getOrElse(BigDecimal(12)).toString().toFloat
    
    println("Industry PE 2: " + industryPeRatio)  
      
    val companyPeRatio: Float = 
      yahooFinanceData.peRatio.getOrElse(BigDecimal(0)).toFloat
      
    println("CompanyPeRatio: " + companyPeRatio)  
    
    val ratio =  companyPeRatio/industryPeRatioForCompany
    
    println(ratio)
    
    ratioStepFunction(ratio)
  }
  
}
package model

object DataSource extends Enumeration {
  type DataSourceEnum = Value
  
  val AmericanBulls = Value(1, "AmericanBulls")
  val YahooFinance = Value(2, "YahooFinance")
  val NasdaqHistoricalEps = Value(3, "NasdaqHistoricalEps")
  val NasdaqEarnings = Value(4, "NasdaqEarnings")
  val StockQuote = Value(5, "StockQuote")
  val MarketWatchQuarterly = Value(6, "MarketWatchQuarterly")
  val SymbolIndustry = Value(7, "SymbolIndustry")
  val MarketWatchQuarterlyBalanceSheet = Value(8, "MarketWatchQuarterlyBalanceSheet")
  val MarketWatchKeyStatistics = Value(9, "MarketWatchKeyStatistics")
}

package model

import org.squeryl._
import model.SquerylEntryPoint._
import java.util.Date

import play.api.libs.json.{JsValue, Json, Writes}

class Stock(var id: Long,
    var symbol: String,
    var company_name: String,
    var next_earnings_date: Date,
    var exchange: String) extends KeyedEntity[Long]{
  
}

object Stock {
  implicit val stockWrites = new Writes[Stock] {
    def writes(value: Stock): JsValue = {
      Json.obj(
        "id" -> value.id,
        "symbol" -> value.symbol,
        "company_name" -> value.company_name,
        "next_earnings_date" -> value.next_earnings_date,
        "exchange" -> value.exchange
      )
    }
  }


  def findBySymbol(symbol: String) : Option[Stock] =
	  from(Library.stock)(s => where(s.symbol === symbol) select(s)).singleOption

	def getBySymbol(symbol: String) : Stock = {
    val stock = findBySymbol(symbol)
    stock match {
      case Some(stock) => stock
      case None => throw new Exception("No Such stock: " + symbol)
    }
    
  }
  
  def findStocksByNextEarningsDate(beginningDate: Date, endDate: Date): List[Stock] = {
      from(Library.stock)(s => 
      where(
          (s.next_earnings_date >= beginningDate) and 
          (s.next_earnings_date <= endDate)
      ) 
      select(s)).toList
  }
  
  
	def findLatestEarningsDate() : Option[Date] =
	  from(Library.stock)(s => select(s.next_earnings_date) orderBy(s.next_earnings_date desc) ).page(0, 1).singleOption

  def updateNextEarningsDate(stockId: Long, nextEarningsDate: Date) = {
    update(Library.stock)(s =>
      where(s.id === stockId)
        set(s.next_earnings_date := nextEarningsDate))
  }

  def findAll: List[Stock] = {
    Library.stock.allRows.toList
  }
}
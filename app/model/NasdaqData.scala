package model

import java.util.Date
import org.squeryl.KeyedEntity
import org.squeryl._
import model.SquerylEntryPoint._


class NasdaqData(
  var id: Long,
  var stock_id: Long,
  var earnings_date: Date,
  var market_cap: Long,
  var eps_forecast: BigDecimal,
  var eps_previous_year: BigDecimal,
  var num_analysts: Integer,
  var is_premarket: Boolean


  ) extends KeyedEntity[Long] {
  
}

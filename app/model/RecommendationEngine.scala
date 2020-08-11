package model

import java.util.Date

import org.squeryl.KeyedEntity
import play.api.libs.json.{JsValue, Json, Writes}

class RecommendationEngine(var id: Long, var engine: String) extends KeyedEntity[Long]  {

}

object RecommendationEngine {
  implicit val recommendationEngineWrites = new Writes[RecommendationEngine] {
    def writes(value: RecommendationEngine): JsValue = {
      Json.obj(
        "id" -> value.id,
        "name" -> value.engine
      )
    }
  }
}


object RecommendationEngineEnum extends Enumeration {
  type RecommendationEngineEnum = Value

  val AmericanBulls = Value(1, "AmericanBulls")
  val YahooFinanceCompanyValue = Value(2, "YahooFinanceCompanyValue")
  val NasdaqEarningsTimeOfDay = Value(3, "NasdaqEarningsTimeOfDay")
  val NasdaqEarningsPositiveEpsHistory = Value(4, "NasdaqEarningsPositiveEpsHistory")
  val IndustryPriceToEarningsRatio = Value(5, "IndustryPriceToEarningsRatio")
  val QuarterToQuarterRevenueGrowth = Value(6, "QuarterToQuarterRevenueGrowth")
  val RevenuePerEmployee = Value(7, "RevenuePerEmployee")
}

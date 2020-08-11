package web

import java.text.SimpleDateFormat

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.apache.commons.lang3.StringEscapeUtils
import play.api.libs.json.{Format, JsPath, Json, Reads}
import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Income(revenue: Option[Long],
                   salesGrowth: Option[BigDecimal],
                   costOfGoodsSold: Option[Long],
                   cogsExcludingDAndA: Option[Long],
                   amoritizationExpense: Option[Long],
                   depreciation: Option[Long],
                   amorizationOfIntangibles: Option[Long],
                   cogsGrowth: Option[BigDecimal],
                   grossIncome: Option[Long],
                   grossIncomeGrowth: Option[BigDecimal])

case class Expenses(sgaExpense: Option[Long],
                    researchAndDevelopment: Option[Long],
                    otherSga: Option[Long],
                    sgaGrowth: Option[BigDecimal],
                    otherOperatingExpense: Option[Long],
                    unusualExpense: Option[Long],
                    ebitAfterUnusualExpense: Option[Long],
                    nonOpertatingIncomeExpense: Option[Long],
                    nonOpertatingInterestIncome: Option[Long],
                    equityInAffiliatesPretax: Option[Long],
                    interestExpense: Option[Long],
                    interestExpenseGrowth: Option[BigDecimal],
                    grossInterestExpense: Option[Long],
                    interestCapitalized: Option[Long])

case class NetIncome(pretaxIncome: Option[Long],
                     pretaxIncomeGrowth: Option[BigDecimal],
                     pretaxMargin: Option[BigDecimal],
                     incomeTax: Option[Long],
                     incomeTaxDomestic: Option[Long],
                     incomeTaxForeign: Option[Long],
                     incomeTaxDeferredDomestic: Option[Long],
                     incomeTaxDeferredForeign: Option[Long],
                     incomeTaxCredits: Option[Long],
                     equityInAffiliates: Option[Long],
                     otherAfterTaxIncome: Option[Long],
                     consolidatedNetIncome: Option[Long],
                     minorityInterestExpense: Option[Long],
                     netIncome: Option[Long],
                     netIncomeGrowth: Option[BigDecimal],
                     netMarginGrowth: Option[BigDecimal])

case class Extraordinaries(extraordinariesDiscontinuedOperations: Option[Long],
                           extraItems: Option[Long],
                           cumulativeEffectAccounting: Option[Long],
                           discontinuedOperations: Option[Long],
                           netIncomeAfterExtraordinaries: Option[Long],
                           preferredDividends: Option[Long])

case class QuarterResult(income: Income,
                         expenses: Expenses,
                         netIncome: NetIncome,
                         extraordinaries: Extraordinaries,
                         netIncomeToCommonShares: Option[Long],
                         epsBasic: Option[BigDecimal],
                         epsBasicGrowth: Option[BigDecimal],
                         basicSharesOutstanding: Option[Long],
                         epsDiluted: Option[BigDecimal],
                         epsDilutedGrowth: Option[BigDecimal],
                         dilutedSharesOutstanding: Option[Long],
                         ebitda: Option[Long],
                         ebitdaGrowth: Option[BigDecimal],
                         ebitdaMargin: Option[BigDecimal])


case class MarketWatchKeyStatistics(revenuePerEmployee: Option[BigDecimal],
                                    peRatio: Option[BigDecimal]);


case class ChartValuesLong(chartValues: List[Option[Long]])
case class ChartValuesBigDecimal(chartValues: List[Option[BigDecimal]])

case class QuarterlyResults(results: List[QuarterResult])


object QuarterlyResults {

  implicit val incomeReads = Json.reads[Income]
  implicit val incomeWrites = Json.writes[Income]

  implicit val expenseReads = Json.reads[Expenses]
  implicit val expenseWrites = Json.writes[Expenses]

  implicit val netIncomeReads = Json.reads[NetIncome]
  implicit val netIncomeWrites = Json.writes[NetIncome]

  implicit val extraordinariesReads = Json.reads[Extraordinaries]
  implicit val extraordinariesWrites = Json.writes[Extraordinaries]

  implicit val quarterResultReads = Json.reads[QuarterResult]
  implicit val quarterResultWrites = Json.writes[QuarterResult]

  implicit val quarterlyResultsReads = Json.reads[QuarterlyResults]
  implicit val quarterlyResultsWrites = Json.writes[QuarterlyResults]
}


object MarketWatchKeyStatistics {
  implicit val marketWatchKeyStatisticsReads = Json.reads[MarketWatchKeyStatistics]
  implicit val marketWatchKeyStatisticsWrites = Json.writes[MarketWatchKeyStatistics]
}

/**
  * Created by sdoshi on 6/4/2016.
  */


object MarketWatchScraper {

  val optionLongReader: Reads[Option[Long]] = Reads[Option[Long]](value => JsSuccess(value match {
    case n : JsNumber  => Some(n.value.toLong)
    case JsNull        => None
    case u             => None
  }))

  val listOptionLongReads = Reads.list[Option[Long]](optionLongReader)

  implicit val chartValuesLongsReads: Reads[ChartValuesLong] =
    (__ \ "chartValues").read[List[Option[Long]]](listOptionLongReads).map { name => ChartValuesLong(name) }

  val optionBigDecimalReader: Reads[Option[BigDecimal]] = Reads[Option[BigDecimal]](value => JsSuccess(value match {
    case n : JsNumber  => Some(n.value)
    case JsNull        => None
    case u             => None
  }))

  val listOptionBigDecimalReads = Reads.list[Option[BigDecimal]](optionBigDecimalReader)

  implicit val chartValuesBigDecimalReads: Reads[ChartValuesBigDecimal] =
    (__ \ "chartValues").read[List[Option[BigDecimal]]](listOptionBigDecimalReads).map { name => ChartValuesBigDecimal(name) }

  def getLongFromChartData(chartData: String) : ChartValuesLong = {

    val jsonArray = StringEscapeUtils.unescapeHtml3(chartData)
    Json.parse(jsonArray).validate[ChartValuesLong].getOrElse(ChartValuesLong(List(None, None, None, None, None)))

  }

  def getBigDecimalFromChartData(chartData: String) : ChartValuesBigDecimal = {
    val jsonArray = StringEscapeUtils.unescapeHtml3(chartData)
    Json.parse(jsonArray).validate[ChartValuesBigDecimal].getOrElse(ChartValuesBigDecimal(List(None, None, None, None, None)))
  }


  def buildQuarterlyResults(revenueTable: Element, resultsTable: Element) : List[QuarterResult]  = {

    val incomeRows: List[Element] = revenueTable >> element("tbody") >> elementList("tr")
    val resultsRows: List[Element] = resultsTable >> element("tbody") >> elementList("tr")

    //val incomeData = (incomeRows >> elementList(".miniGraph")).flatten.map( x => x.attr("data-chart"))
    //val resultsData = (resultsRows >> elementList(".miniGraph")).flatten.map( x => x.attr("data-chart"))

    val incomeData2 = incomeRows.map(r => r >?> element(".miniGraph"))
    val incomeData3 = incomeData2.map(x => x.map(y => y.attr("data-chart")))
    val incomeData = incomeData3.map(x => x.getOrElse("{\"chartValues\":[null,null,null,null,null]}"))

    val resultsData2 = resultsRows.map(r => r >?> element(".miniGraph"))
    val resultsData3 = resultsData2.map(x => x.map(y => y.attr("data-chart")))
    val resultsData = resultsData3.map(x => x.getOrElse("{\"chartValues\":[null,null,null,null,null]}"))


    val revenue = getLongFromChartData(incomeData(0))
    val salesGrowth = getBigDecimalFromChartData(incomeData(1))
    val costOfGoodsSold = getLongFromChartData(incomeData(2))
    val cogsExcludingDAndA = getLongFromChartData(incomeData(3))
    val amoritizationExpense = getLongFromChartData(incomeData(4))
    val depreciation = getLongFromChartData(incomeData(5))
    val amorizationOfIntangibles = getLongFromChartData(incomeData(6))
    val cogsGrowth = getBigDecimalFromChartData(incomeData(7))
    val grossIncome = getLongFromChartData(incomeData(8))
    val grossIncomeGrowth = getBigDecimalFromChartData(incomeData(9))

    val sgaExpense = getLongFromChartData(resultsData(0))
    val researchAndDevelopment = getLongFromChartData(resultsData(1))
    val otherSga = getLongFromChartData(resultsData(2))
    val sgaGrowth = getBigDecimalFromChartData(resultsData(3))
    val otherOperatingExpense = getLongFromChartData(resultsData(4))
    val unusualExpense = getLongFromChartData(resultsData(5))
    val ebitAfterUnusualExpense = getLongFromChartData(resultsData(6))
    val nonOpertatingIncomeExpense = getLongFromChartData(resultsData(7))
    val nonOpertatingInterestIncome = getLongFromChartData(resultsData(8))
    val equityInAffiliatesPretax = getLongFromChartData(resultsData(9))
    val interestExpense = getLongFromChartData(resultsData(10))
    val interestExpenseGrowth = getBigDecimalFromChartData(resultsData(11))
    val grossInterestExpense = getLongFromChartData(resultsData(12))
    val interestCapitalized = getLongFromChartData(resultsData(13))
    val pretaxIncome = getLongFromChartData(resultsData(14))
    val pretaxIncomeGrowth = getBigDecimalFromChartData(resultsData(15))
    val pretaxMargin = getBigDecimalFromChartData(resultsData(16))
    val incomeTax = getLongFromChartData(resultsData(17))
    val incomeTaxDomestic = getLongFromChartData(resultsData(18))
    val incomeTaxForeign = getLongFromChartData(resultsData(19))
    val incomeTaxDeferredDomestic = getLongFromChartData(resultsData(20))
    val incomeTaxDeferredForeign = getLongFromChartData(resultsData(21))
    val incomeTaxCredits = getLongFromChartData(resultsData(22))
    val equityInAffiliates = getLongFromChartData(resultsData(23))
    val otherAfterTaxIncome = getLongFromChartData(resultsData(24))
    val consolidatedNetIncome = getLongFromChartData(resultsData(25))
    val minorityInterestExpense = getLongFromChartData(resultsData(26))
    val netIncome = getLongFromChartData(resultsData(27))
    val netIncomeGrowth = getBigDecimalFromChartData(resultsData(28))
    val netMarginGrowth = getBigDecimalFromChartData(resultsData(29))
    val extraordinariesDiscontinuedOperations = getLongFromChartData(resultsData(30))
    val extraItems = getLongFromChartData(resultsData(31))
    val cumulativeEffectAccounting = getLongFromChartData(resultsData(32))
    val discontinuedOperations = getLongFromChartData(resultsData(33))
    val netIncomeAfterExtraordinaries = getLongFromChartData(resultsData(34))
    val preferredDividends = getLongFromChartData(resultsData(35))
    val netIncomeToCommonShares = getLongFromChartData(resultsData(36))
    val epsBasic = getBigDecimalFromChartData(resultsData(37))
    val epsBasicGrowth = getBigDecimalFromChartData(resultsData(38))
    val basicSharesOutstanding = getLongFromChartData(resultsData(39))
    val epsDiluted = getBigDecimalFromChartData(resultsData(40))
    val epsDilutedGrowth = getBigDecimalFromChartData(resultsData(41))
    val dilutedSharesOutstanding = getLongFromChartData(resultsData(42))
    val ebitda = getLongFromChartData(resultsData(43))
    val ebitdaGrowth = getBigDecimalFromChartData(resultsData(44))
    val ebitdaMargin = getBigDecimalFromChartData(resultsData(45))


    val range = 0 to 4

    range.map( i =>  {
      QuarterResult(
        Income(revenue.chartValues(i),
          salesGrowth.chartValues(i),
          costOfGoodsSold.chartValues(i),
          cogsExcludingDAndA.chartValues(i),
          amoritizationExpense.chartValues(i),
          depreciation.chartValues(i),
          amorizationOfIntangibles.chartValues(i),
          cogsGrowth.chartValues(i),
          grossIncome.chartValues(i),
          grossIncomeGrowth.chartValues(i)),
        Expenses(
          sgaExpense.chartValues(i),
          researchAndDevelopment.chartValues(i),
          otherSga.chartValues(i),
          sgaGrowth.chartValues(i),
          otherOperatingExpense.chartValues(i),
          unusualExpense.chartValues(i),
          ebitAfterUnusualExpense.chartValues(i),
          nonOpertatingIncomeExpense.chartValues(i),
          nonOpertatingInterestIncome.chartValues(i),
          equityInAffiliatesPretax.chartValues(i),
          interestExpense.chartValues(i),
          interestExpenseGrowth.chartValues(i),
          grossInterestExpense.chartValues(i),
          interestCapitalized.chartValues(i)),
        NetIncome(
          pretaxIncome.chartValues(i),
          pretaxIncomeGrowth.chartValues(i),
          pretaxMargin.chartValues(i),
          incomeTax.chartValues(i),
          incomeTaxDomestic.chartValues(i),
          incomeTaxForeign.chartValues(i),
          incomeTaxDeferredDomestic.chartValues(i),
          incomeTaxDeferredForeign.chartValues(i),
          incomeTaxCredits.chartValues(i),
          equityInAffiliates.chartValues(i),
          otherAfterTaxIncome.chartValues(i),
          consolidatedNetIncome.chartValues(i),
          minorityInterestExpense.chartValues(i),
          netIncome.chartValues(i),
          netIncomeGrowth.chartValues(i),
          netMarginGrowth.chartValues(i)),
        Extraordinaries(
          extraordinariesDiscontinuedOperations.chartValues(i),
          extraItems.chartValues(i),
          cumulativeEffectAccounting.chartValues(i),
          discontinuedOperations.chartValues(i),
          netIncomeAfterExtraordinaries.chartValues(i),
          preferredDividends.chartValues(i)),
        netIncomeToCommonShares.chartValues(i),
        epsBasic.chartValues(i),
        epsBasicGrowth.chartValues(i),
        basicSharesOutstanding.chartValues(i),
        epsDiluted.chartValues(i),
        epsDilutedGrowth.chartValues(i),
        dilutedSharesOutstanding.chartValues(i),
        ebitda.chartValues(i),
        ebitdaGrowth.chartValues(i),
        ebitdaMargin.chartValues(i))
    }).toList
  }


  def getQuarterlyIncomeStatement(symbol: String): QuarterlyResults  = {

    val browser = JsoupBrowser()
    val url_test = "http://www.marketwatch.com/investing/stock/" + symbol.toLowerCase + "/financials/income/quarter";

    println(url_test)

    val doc = browser.get(url_test)

    val dataTables: List[Element] = doc >> elementList(".crDataTable")

    val revenueTable: Element = dataTables(0)
    val resultsTable: Element = dataTables(1)

    QuarterlyResults(buildQuarterlyResults(revenueTable, resultsTable))



  }


  def getKeyStatistics(symbol: String): MarketWatchKeyStatistics = {
    val browser = JsoupBrowser();
    val url = "http://www.marketwatch.com/investing/stock/" + symbol.toLowerCase;

    val doc = browser.get(url)

    val dataElements: List[Element] = doc >> elementList(".data")

    val moneyRevPerEmployee = text(dataElements(13))

    val revPerEmployee = moneyRevPerEmployee.replace("$", "").replace(",", "")

    val revenuePerEmployee: Option[BigDecimal] = {
      try {
        if ( revPerEmployee.endsWith(("B")))
          Some(BigDecimal(revPerEmployee.replace("B", "0000000").replace(".", "")))
        else if ( revPerEmployee.endsWith(("M")))
         Some(BigDecimal(revPerEmployee.replace("M", "0000").replace(".", "")))
        else
          Some(BigDecimal(revPerEmployee))
      } catch {
        case e: Exception => None
      }
    }

    MarketWatchKeyStatistics(revenuePerEmployee, None)
  }
}

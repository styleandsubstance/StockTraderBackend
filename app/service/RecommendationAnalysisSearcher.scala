package service

import java.lang.reflect.Field
import java.util.Date

import com.google.inject.Singleton
import model._
import org.squeryl.dsl.ast.{ExpressionNode, OrderByExpression}
import org.squeryl.dsl.fsm.{Conditioned, WhereState}
import org.squeryl._
import model.SquerylEntryPoint._

case class SearchParams(startDate: Date, endDate: Date, globalSearch: Option[String])


/**
  * Created by sdoshi on 5/14/2016.
  */
@Singleton
class RecommendationAnalysisSearcher extends Searcher[(RecommendationAnalysis, Stock), SearchParams] {

  def search_where_clause(element: RecommendationAnalysis, stock: Stock, params: SearchParams): WhereState[Conditioned] = {
    where(
        ((lower(stock.symbol) like params.globalSearch)).inhibitWhen(params.globalSearch == None) and
        (element.scheduled_earnings_date >= params.startDate) and
        (element.scheduled_earnings_date <= params.endDate)
    )
  }

  def order_by(element: RecommendationAnalysis, stock: Stock, sortParams: SortParams): ExpressionNode = {
    val orderByArg: OrderByExpression = {
      if ( sortParams.field.getName() == "id" ) {
        new OrderByExpression(element.id);
      }
      else if ( sortParams.field.getName() == "result") {
        new OrderByExpression(element.result);
      }
      else if ( sortParams.field.getName() == "accuracy") {
        new OrderByExpression(element.accuracy);
      }
      else if ( sortParams.field.getName() == "symbol") {
        new OrderByExpression(stock.symbol);
      }
      else if ( sortParams.field.getName() == "next_earnings_date") {
        new OrderByExpression(stock.next_earnings_date);
      }
      else {
        new OrderByExpression(element.analysis_run_time);
      }
    }

    if ( sortParams.descending == true ) {
      orderByArg.inverse
    }
    else {
      orderByArg
    }
  }

  override def count(searchParams: SearchParams): Long = {
    join(Library.recommendationAnalysis, Library.stock)((ra, s) =>
      search_where_clause(ra, s, searchParams)
      compute(countDistinct(ra.id))
      on(ra.stock_id === s.id)
    )
  }

  override def search(searchParams: SearchParams, sortParams: SortParams, numResults: Int, offset: Int): List[(RecommendationAnalysis, Stock)] = {
    join(Library.recommendationAnalysis, Library.stock)((ra, s) =>
      search_where_clause(ra, s, searchParams)
      select(ra, s)
      orderBy(order_by(ra, s, sortParams))
      on(ra.stock_id === s.id)
    ).page(offset, numResults).toList
  }
}

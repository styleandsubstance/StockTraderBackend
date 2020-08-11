package service

import javax.inject.Singleton

import model.SquerylEntryPoint._
import model._
import org.squeryl.dsl.ast.{ExpressionNode, OrderByExpression}
import org.squeryl.dsl.fsm.{Conditioned, WhereState}

/**
  * Created by sdoshi on 5/28/2016.
  */
@Singleton
class RecommendationAnalysisEngineSearcher extends Searcher[(RecommendationAnalysisEngine, RecommendationAnalysis, Stock), SearchParams]  {
  def search_where_clause(element: RecommendationAnalysisEngine, analysis: RecommendationAnalysis, stock: Stock, params: SearchParams): WhereState[Conditioned] = {
    where(
      (stock.next_earnings_date >= params.startDate) and
      (stock.next_earnings_date <= params.endDate)
    )
  }

  def order_by(element: RecommendationAnalysisEngine, analysis: RecommendationAnalysis, sortParams: SortParams): ExpressionNode = {
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
      else if ( sortParams.field.getName() == "scheduled_time") {
        new OrderByExpression(analysis.scheduled_date);
      }
      else {
        new OrderByExpression(analysis.scheduled_date);
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
    join(Library.recommendationAnalysisEngine, Library.recommendationAnalysis, Library.stock)((rae, ra, s) =>
      search_where_clause(rae, ra, s, searchParams)
        compute(countDistinct(rae.id))
        on(rae.analysis_id === ra.id, ra.stock_id === s.id)
    )
  }

  override def search(searchParams: SearchParams, sortParams: SortParams, numResults: Int, offset: Int): List[(RecommendationAnalysisEngine, RecommendationAnalysis, Stock)] = {
    join(Library.recommendationAnalysisEngine, Library.recommendationAnalysis, Library.stock)((rae, ra, s) =>
      search_where_clause(rae, ra, s, searchParams)
        select(rae, ra, s)
        orderBy(order_by(rae, ra, sortParams))
        on(rae.analysis_id === ra.id, ra.stock_id === s.id)
    ).page(offset, numResults).toList
  }
}

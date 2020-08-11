package model

import java.lang.reflect.Field

import org.squeryl.dsl.ast.ExpressionNode
import org.squeryl.dsl.fsm.{Conditioned, WhereState}


case class SortParams(field: Field, descending: Boolean)

/**
  * Created by sdoshi on 5/7/2016.
  */
trait Searcher[T, V] {

  def search(searchParams: V, sortParams: SortParams, numResults: Int, offset: Int): List[T]

  def count(searchParams: V): Long

}

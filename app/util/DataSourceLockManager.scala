package util

import model.DataSource._
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import model.MarketDataSource
import model.MarketDataSource._

case class DataSourceLock(symbol: String, dataSource: DataSourceEnum)
case class MarketDataSourceLock(marketDataSource: MarketDataSource)

object DataSourceLockManager {
  //val map = new ConcurrentHashMap[DataSourceLock, Object]()
  val synchroSet = new mutable.HashSet[DataSourceLock] with
      mutable.SynchronizedSet[DataSourceLock]

  val marketDataSourceSet = new mutable.HashSet[MarketDataSourceLock] with
      mutable.SynchronizedSet[MarketDataSourceLock]
  
  def getLock(dataSourceLock: DataSourceLock) : Object = {
    println("Getting lock: " + dataSourceLock)
    if ( !synchroSet.contains(dataSourceLock) ) {
      println("Map doesn't contain an object for this...creating");
      synchroSet.add(dataSourceLock);
    }
    //map.get(dataSourceLock);
    synchroSet.find { x => x == dataSourceLock }.get
  }
  
  def getLock(marketDataSourceLock: MarketDataSourceLock) : Object = {
    println("Getting lock: " + marketDataSourceLock)
    if ( !marketDataSourceSet.contains(marketDataSourceLock) ) {
      println("Map doesn't contain an object for this...creating");
      marketDataSourceSet.add(marketDataSourceLock);
    }
    //
    marketDataSourceSet.find { x => x == marketDataSourceLock }.get
  }
}
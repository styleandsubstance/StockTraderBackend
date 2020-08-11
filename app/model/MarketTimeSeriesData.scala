package model

import java.util.Date
import org.squeryl._
import model.SquerylEntryPoint._
import java.sql.Timestamp
import model.MarketDataSource._
import util.DataSourceLockManager
import util.MarketDataSourceLock
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import actors.LoadMarketData
import scala.concurrent.duration._
import scala.concurrent.Await
import actors.MarketDataActor


class MarketTimeSeriesData (var id: Long,
    var collection_time: Timestamp,
    var market_data_source_id: Long,
    var data: String) extends KeyedEntity[Long]{
}

object MarketTimeSeriesData {
  def add(marketDataSource: MarketDataSource, data: String) : MarketTimeSeriesData = {
    Library.marketTimeSeriesData.insert(
        new MarketTimeSeriesData(0,  new Timestamp(new Date().getTime()), marketDataSource.id, data))
  }
  
  
  def load(dataSource: MarketDataSource, oldestTimestamp: Option[Timestamp], numResults: Int) = {
    //see if there is at least one row that meets the criteria
    from(Library.marketTimeSeriesData)(s => 
      where(
          (s.market_data_source_id === dataSource.id) and 
          (s.collection_time >= oldestTimestamp).inhibitWhen(oldestTimestamp == None)
      ) 
      select(s)
      orderBy(s.collection_time desc)
    ).page(0, numResults)
  }
  
  def loadLatest(marketDataSource: MarketDataSource, oldestTimestamp: Timestamp) = {
    DataSourceLockManager.getLock(MarketDataSourceLock(marketDataSource)).synchronized {
      val data = load(marketDataSource, Some(oldestTimestamp), 1).toList
      if ( data.length == 0 ) {
        println("No data...loading manually")
//        implicit val timeout = Timeout(5 minutes)
//        //need to load the data manually
//        val promise = Akka.system.actorOf(Props[MarketDataActor]) ? 
//            LoadMarketData(marketDataSource)
//        val marketTimeSeriesData: Option[MarketTimeSeriesData] = 
//          Await.result(promise, timeout.duration).asInstanceOf[Option[MarketTimeSeriesData]]
        
        val marketTimeSeriesData: Option[MarketTimeSeriesData] = 
          MarketDataActor.LoadMarketData(marketDataSource)

        
        marketTimeSeriesData match {
          case Some(data) => data
          case None => throw new Exception("Unable to load latest time series data")
        }
      }
      else {
        println("Actually have data...returning")
        data(0)
      }
    }
  }
}
package model

import java.util.Date
import org.squeryl._
import model.SquerylEntryPoint._
import model.DataSource._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import actors.StockDataActor
//import actors.LoadData
import scala.concurrent.duration._
import scala.concurrent.Await
import util.DataSourceLockManager
import util.DataSourceLock
import java.sql.Timestamp


class TimeSeriesData(var id: Long,
    var stock_id : Long,
    var collection_time: Timestamp,
    var data_source_id: Long,
    var data: String) extends KeyedEntity[Long]{
}

object TimeSeriesData {
  def add(symbol: String, dataSource: DataSourceEnum, data: String) : TimeSeriesData = {
    //val stock = Stock.findBySymbol(symbol)
    val stock = Stock.getBySymbol(symbol)
    Library.timeSeriesData.insert(
        new TimeSeriesData(0, stock.id, new Timestamp(new Date().getTime()), dataSource.id, data))
  }
  
  
  def load(symbol: String, dataSource: DataSourceEnum, oldestTimestamp: Option[Timestamp], numResults: Int) : List[TimeSeriesData] = {
    val stock = Stock.findBySymbol(symbol).get
    //see if there is at least one row that meets the criteria
    from(Library.timeSeriesData)(s => 
      where(
          (s.stock_id === stock.id) and
          (s.data_source_id === dataSource.id) and 
          (s.collection_time >= oldestTimestamp).inhibitWhen(oldestTimestamp == None)
      ) 
      select(s)
      orderBy(s.collection_time desc)
    ).page(0, numResults).toList
  }
  
  def loadLatest(symbol: String, dataSource: DataSourceEnum) : TimeSeriesData = {
    DataSourceLockManager.getLock(DataSourceLock(symbol, dataSource)).synchronized {
      println("Lock Acquired")
      val data = load(symbol, dataSource, None, 1)
      println("Lock Acquired 1")
      if ( data.length == 0 ) {
        println("No data...loading manually")
//        implicit val timeout = Timeout(5 minutes)
        //need to load the data manually
        //val promise = Akka.system.actorOf(Props[StockDataActor]) ? 
        //    LoadData(symbol, dataSource)
//        val timeSeriesData: Option[TimeSeriesData] = 
//          Await.result(promise, timeout.duration).asInstanceOf[Option[TimeSeriesData]]
        val timeSeriesData: Option[TimeSeriesData] = 
          StockDataActor.LoadData(symbol, dataSource);
        
        timeSeriesData match {
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
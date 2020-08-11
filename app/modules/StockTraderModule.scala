package modules

/**
  * Created by sdoshi on 8/28/2016.
  */

import com.google.inject.AbstractModule

class StockTraderModule extends AbstractModule{

  def configure() = {

    bind(classOf[DatabaseModule])
      .to(classOf[PostgresModule]).asEagerSingleton()

    bind(classOf[SchedulingModule])
      .to(classOf[QuartzSchedulingModule]).asEagerSingleton()
  }

}

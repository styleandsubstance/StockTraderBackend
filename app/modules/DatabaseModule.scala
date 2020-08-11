package modules

import javax.inject.Inject

import com.google.inject.ImplementedBy
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.PostgreSqlAdapter
import play.api.db.DBApi


/**
  * Created by sdoshi on 8/28/2016.
  */
@ImplementedBy(classOf[PostgresModule])
trait DatabaseModule {

}

class PostgresModule @Inject() (dbApi: DBApi) extends DatabaseModule{

  val dbAdapter = new PostgreSqlAdapter();

  println("In PostgresModule settings")
//  SessionFactory.concreteFactory = Some(() => Session.create(DB.getConnection()(app),
//    dbAdapter));

  SessionFactory.concreteFactory = Some(() => Session.create(dbApi.database("default").getConnection(),
    dbAdapter));
}
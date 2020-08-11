package modules

import java.util.Date
import javax.inject.Inject

import actors._
import akka.actor.{ActorSystem, Props}
import com.google.inject.ImplementedBy
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
  * Created by sdoshi on 8/29/2016.
  */

@ImplementedBy(classOf[QuartzSchedulingModule])
trait SchedulingModule {

}

class QuartzSchedulingModule @Inject()(system: ActorSystem) extends SchedulingModule {
  println("Scheduling Quartz tasks")

  val webScraperActor = system.actorOf(Props[WebScraper], "WebScraper")
  QuartzSchedulerExtension(system).createSchedule("PerformStockAnalysis", Some("A cron job that executes all scheduled analyses to run"), "0 30 7 ? * *",  None)
  QuartzSchedulerExtension(system).schedule("PerformStockAnalysis", webScraperActor, BeginNasdaqEarnignsDateScraping)

  QuartzSchedulerExtension(system).createSchedule("RetrieveStockQuotes", Some("A cron job that retrieves stock quotes"), "0 30 16 ? * MON-FRI",  None)
  QuartzSchedulerExtension(system).schedule("RetrieveStockQuotes", webScraperActor, RetrieveStockQuotes)


  val accuracyActor = system.actorOf(Props[AccuracyActor], "AccuracyActor")
  QuartzSchedulerExtension(system).createSchedule("ExecuteAccuracyCalculations", Some("A cron job that calculates accuracy statistics for each analysis"), "0 0 17 ? * *",  None)
  QuartzSchedulerExtension(system).schedule("ExecuteAccuracyCalculations", accuracyActor, LaunchRecommendationAnalysisAccuracyTasks(new Date()))

}

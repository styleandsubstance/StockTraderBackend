package util

import java.util.Date

import org.joda.time.{DateTime, DateTimeConstants}

/**
  * Created by sdoshi on 9/1/2016.
  */
object DateUtils {


  def addBusinessDays(date: Date, numDaysRequested: Int, isPremarket: Boolean) : Date = {
    var jodaDate = new DateTime(date)
    var numDays = numDaysRequested
    var numDaysAdded = 0;

    if ( isPremarket == false ) {
      numDays = numDays + 1
    }

    while(numDaysAdded < numDays) {
      jodaDate = jodaDate.plusDays(1)
      if ( jodaDate.getDayOfWeek != DateTimeConstants.SATURDAY && jodaDate.getDayOfWeek != DateTimeConstants.SUNDAY) {
        numDaysAdded = numDaysAdded + 1;
      }
    }

    jodaDate.toDate
  }

  def subtractBusinessDays(date: Date, numDaysRequested: Int, isPremarket: Boolean) : Date = {
    var jodaDate = new DateTime(date)
    var numDays = numDaysRequested
    var numDaysSubtracted = 0;

    if ( isPremarket == true ) {
      numDays = numDays + 1
    }

    while(numDaysSubtracted < numDays) {
      jodaDate = jodaDate.minusDays(1)
      if ( jodaDate.getDayOfWeek != DateTimeConstants.SATURDAY && jodaDate.getDayOfWeek != DateTimeConstants.SUNDAY) {
        numDaysSubtracted = numDaysSubtracted + 1;
      }
    }

    jodaDate.toDate
  }

}

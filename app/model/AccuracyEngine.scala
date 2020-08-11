package model


object AccuracyEngineEnum extends Enumeration {
  type AccuracyEngineEnum = Value

  val FiveTradingDaysRunUpAccuracy = Value(1, "FiveTradingDaysRunUpAccuracy")
  val AfterEarningsReportedAnalysisAccuracy = Value(2, "AfterEarningsReportedAnalysisAccuracy")
}


/**
  * Created by sdoshi on 9/1/2016.
  */
class AccuracyEngine {

}

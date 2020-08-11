package analysis

import play.api.libs.json._


trait IRecommendationAnalysisRun {
  
  def ratioStepFunction(ratio: Float) : BigDecimal = {
    if ( 0 <= ratio && ratio < 0.25)
      BigDecimal(60)
    else if ( 0.25 <= ratio && ratio < 0.5)
      BigDecimal(70)
    else if ( 0.5 <= ratio && ratio < 0.75)
      BigDecimal(80)
    else if (0.75 <= ratio && ratio < 1)
      BigDecimal(100)
    else if (1 <= ratio && ratio < 1.25)
      BigDecimal(75)
    else if (1.25 <= ratio && ratio < 1.5)
      BigDecimal(50)
    else if (ratio > 1.5)
      BigDecimal(25)  
    else
      BigDecimal(0)
  }
  
  
  def run(symbol: String) : BigDecimal
  
}
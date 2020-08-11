package accuracy

import model.{RecommendationAnalysis, RecommendationAnalysisAccuracy, TimeSeriesData}

/**
  * Created by sdoshi on 8/28/2016.
  */
case class AccuracyData(recommendationAnalysisAccuracy: RecommendationAnalysisAccuracy,
                        recommendationAnalysis: RecommendationAnalysis,
                        timeSeriesData: List[TimeSeriesData])

trait IRecommendationAnalysisAccuracyRun {


  def run(recommendationAnalysisAccuracyId: Long, params: Option[String])


}

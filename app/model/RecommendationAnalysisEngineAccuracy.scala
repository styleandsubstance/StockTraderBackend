package model

import java.util.Date
import org.squeryl.KeyedEntity
/**
  * Created by sdoshi on 9/2/2016.
  */
class RecommendationAnalysisEngineAccuracy(var id: Long,
                                           var execution_date: Date,
                                           var analysis_id: Long,
                                           var accuracy_engine_id: Long,
                                           var analysis_engine_id: Long,
                                           var accuracy: Option[BigDecimal]) extends KeyedEntity[Long] {


}

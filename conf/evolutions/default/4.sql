# --- !Ups
ALTER TABLE RecommendationAnalysis ADD COLUMN accuracy decimal;
ALTER TABLE RecommendationAnalysisEngine ADD COLUMN accuracy decimal;

# --- !Downs
ALTER TABLE RecommendationAnalysis DROP COLUMN accuracy;
ALTER TABLE RecommendationAnalysisEngine DROP COLUMN accuracy decimal;
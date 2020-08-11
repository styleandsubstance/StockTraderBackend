# --- !Ups
CREATE TABLE AccuracyEngine (
  id serial PRIMARY KEY,
  name text NOT NULL,
  default_params text
);

INSERT INTO AccuracyEngine VALUES ( nextval('accuracyengine_id_seq'), 'FiveTradingDaysRunUpAccuracy', NULL);
INSERT INTO AccuracyEngine VALUES ( nextval('accuracyengine_id_seq'), 'OneTradingDayAnalysisAccuracy', NULL);


CREATE TABLE RecommendationAnalysisAccuracy (
	id serial PRIMARY KEY,
	execution_date date NOT NULL,
	analysis_id bigint REFERENCES RecommendationAnalysis(id) NOT NULL,
	accuracy_engine_id bigint REFERENCES AccuracyEngine(id) NOT NULL,
	accuracy decimal
);


# --- !Downs
DROP TABLE RecommendationAnalysisAccuracy;
DROP TABLE AccuracyEngine;
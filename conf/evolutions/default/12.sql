# --- !Ups
CREATE TABLE RecommendationAnalysisEngineAccuracy (
	id serial PRIMARY KEY,
	execution_date date NOT NULL,
	analysis_id bigint REFERENCES RecommendationAnalysis(id) NOT NULL,
	accuracy_engine_id bigint REFERENCES AccuracyEngine(id) NOT NULL,
	analysis_engine_id bigint REFERENCES RecommendationAnalysisEngine(id) NOT NULL,
	accuracy decimal
);


# --- !Downs
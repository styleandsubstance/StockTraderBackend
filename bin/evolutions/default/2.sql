# --- !Ups
CREATE Table RecommendationEngine (
  id serial PRIMARY KEY,
  engine text NOT NULL
);

CREATE TABLE RecommendationAnalysis (
  id serial PRIMARY KEY,
  stock_id bigint REFERENCES Stock(id) NOT NULL,
  name text NOT NULL,
  scheduled_time timestamp NOT NULL,
  analysis_run_date date,
  num_engines bigint NOT NULL,
  num_engines_completed bigint NOT NULL,
  result decimal
);

CREATE TABLE RecommendationAnalysisEngine (
  id serial PRIMARY KEY,
  analysis_id bigint REFERENCES RecommendationAnalysis(id) NOT NULL,
  engine_id bigint REFERENCES RecommendationEngine(id) NOT NULL,
  weight decimal NOT NULL,
  result decimal,
  unique(analysis_id, engine_id)
);


INSERT INTO RecommendationEngine(engine) VALUES ('AmericanBulls');
INSERT INTO RecommendationEngine(engine) VALUES ('YahooFinanceCompanyValue');
INSERT INTO RecommendationEngine(engine) VALUES ('NasdaqEarningsTimeOfDay');
INSERT INTO RecommendationEngine(engine) VALUES ('NasdaqEarningsPositiveEpsHistory');
INSERT INTO RecommendationEngine(engine) VALUES ('IndustryPriceToEarningsRatio');

# --- !Downs
DROP TABLE RecommendationAnalysisEngine;
DROP TABLE RecommendationAnalysis;
DROP TABLE RecommendationEngine;
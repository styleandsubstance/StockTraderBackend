# --- !Ups
INSERT INTO RecommendationEngine(engine) VALUES ('QuarterToQuarterRevenueGrowth');
INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'MarketWatchQuarterly');

# --- !Downs

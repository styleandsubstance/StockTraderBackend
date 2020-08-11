# --- !Ups

ALTER TABLE Stock ADD COLUMN next_earnings_date date NOT NULL;


CREATE TABLE DataSource (
	id serial PRIMARY KEY,
	name text NOT NULL
);

INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'AmericanBulls');
INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'YahooFinance');
INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'NasdaqHistoricalEps');
INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'NasdaqEarnings');
INSERT INTO DataSource VALUES (nextval('datasource_id_seq'), 'StockQuote');

CREATE TABLE TimeSeriesData(
	id serial PRIMARY KEY,
	stock_id bigint REFERENCES Stock(id) NOT NULL,
	collection_time timestamp NOT NULL,
	data_source_id bigint REFERENCES DataSource(id) NOT NULL,
	data jsonb NOT NULL
);

CREATE TABLE MarketDataSource (
	id serial PRIMARY KEY,
	name text NOT NULL
);

INSERT INTO MarketDataSource VALUES (nextval('marketdatasource_id_seq'), 'IndustryPeRatio');

CREATE TABLE MarketTimeSeriesData(
	id serial PRIMARY KEY,
	collection_time timestamp NOT NULL,
	market_data_source_id bigint REFERENCES MarketDataSource(id) NOT NULL,
	data jsonb NOT NULL
);


# --- !Downs
DROP TABLE MarketTimeSeriesData;
DROP TABLE MarketDataSource;
DROP TABLE TimeSeriesData;
DROP TABLE DataSource;
ALTER TABLE Stock DROP COLUMN next_earnings_date;

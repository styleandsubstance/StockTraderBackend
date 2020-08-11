# --- !Ups

CREATE TABLE Stock (
  id serial PRIMARY KEY,
  symbol text NOT NULL UNIQUE,
  company_name text NOT NULL,
  exchange text NOT NULL
);

# --- !Downs
DROP TABLE Stock;
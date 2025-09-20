-- V1__init.sql
CREATE TABLE IF NOT EXISTS providers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS hotels (
  id BIGINT PRIMARY KEY,
  name VARCHAR(512),
  country VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS reviews (
  id BIGSERIAL PRIMARY KEY,
  provider_id INTEGER NOT NULL REFERENCES providers(id),
  hotel_id BIGINT NOT NULL REFERENCES hotels(id),
  external_review_id BIGINT NOT NULL,
  review_date TIMESTAMP WITH TIME ZONE,
  rating NUMERIC(4,1),
  rating_text VARCHAR(64),
  language VARCHAR(16),
  title TEXT,
  positives TEXT,
  negatives TEXT,
  comments TEXT,
  raw_json JSONB,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(provider_id, external_review_id)
);

CREATE TABLE IF NOT EXISTS review_aspects (
  id BIGSERIAL PRIMARY KEY,
  review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  aspect_key VARCHAR(128) NOT NULL,
  aspect_value NUMERIC(4,1),
  UNIQUE(review_id, aspect_key)
);

CREATE TABLE IF NOT EXISTS processed_files (
  id BIGSERIAL PRIMARY KEY,
  s3_key TEXT UNIQUE NOT NULL,
  etag VARCHAR(128) NOT NULL,
  processed_at TIMESTAMPTZ DEFAULT now()
);

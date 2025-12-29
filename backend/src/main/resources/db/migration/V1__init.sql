-- Initial schema for the Flight Spotter Logbook

-- Create sightings table
CREATE TABLE IF NOT EXISTS sightings (
    id SERIAL PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    airport_iata_or_icao VARCHAR(10) NOT NULL,
    location_text VARCHAR(255),
    airline VARCHAR(255),
    callsign VARCHAR(50),
    icao24 VARCHAR(10),
    registration VARCHAR(50),
    aircraft_model VARCHAR(255),
    notes TEXT,
    visibility VARCHAR(16) NOT NULL CHECK (visibility IN ('PUBLIC','PRIVATE')),
    enrichment_status VARCHAR(16) NOT NULL CHECK (enrichment_status IN ('ENRICHING','ENRICHED','FAILED')) DEFAULT 'ENRICHING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create photos table
CREATE TABLE IF NOT EXISTS photos (
    id SERIAL PRIMARY KEY,
    sighting_id INTEGER NOT NULL REFERENCES sightings(id) ON DELETE CASCADE,
    owner_user_id VARCHAR(255) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    secure_url VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create OpenSky cache table
CREATE TABLE IF NOT EXISTS opensky_cache (
    id BIGSERIAL PRIMARY KEY,
    query_hash VARCHAR(255) NOT NULL UNIQUE,
    response TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_opensky_cache_expires ON opensky_cache (expires_at);
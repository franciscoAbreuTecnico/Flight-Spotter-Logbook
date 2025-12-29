-- Fix column types to match Java Long entities
-- Change SERIAL (INTEGER) to BIGSERIAL (BIGINT) for id columns

-- First, alter sightings table id column
ALTER TABLE photos DROP CONSTRAINT IF EXISTS photos_sighting_id_fkey;
ALTER TABLE sightings ALTER COLUMN id TYPE BIGINT;
ALTER TABLE photos ALTER COLUMN id TYPE BIGINT;
ALTER TABLE photos ALTER COLUMN sighting_id TYPE BIGINT;
ALTER TABLE photos ADD CONSTRAINT photos_sighting_id_fkey FOREIGN KEY (sighting_id) REFERENCES sightings(id) ON DELETE CASCADE;

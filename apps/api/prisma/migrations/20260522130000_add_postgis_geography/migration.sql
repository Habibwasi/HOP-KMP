-- Enable PostGIS (safe no-op if already enabled)
CREATE EXTENSION IF NOT EXISTS postgis SCHEMA extensions;

-- Add geography columns (idempotent)
ALTER TABLE "Trip" ADD COLUMN IF NOT EXISTS origin_geo extensions.geography(Point, 4326);
ALTER TABLE "Trip" ADD COLUMN IF NOT EXISTS dest_geo   extensions.geography(Point, 4326);

-- Backfill existing rows
UPDATE "Trip"
SET origin_geo = extensions.ST_SetSRID(extensions.ST_MakePoint("originLng", "originLat"), 4326)::extensions.geography,
    dest_geo   = extensions.ST_SetSRID(extensions.ST_MakePoint("destLng",   "destLat"),   4326)::extensions.geography
WHERE origin_geo IS NULL;

-- GiST spatial indexes
CREATE INDEX IF NOT EXISTS "Trip_origin_geo_idx" ON "Trip" USING GIST (origin_geo);
CREATE INDEX IF NOT EXISTS "Trip_dest_geo_idx"   ON "Trip" USING GIST (dest_geo);

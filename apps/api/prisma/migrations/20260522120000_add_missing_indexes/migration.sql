-- Add missing indexes identified pre-launch

-- Booking: index on tripId and passengerId (most-queried foreign keys)
CREATE INDEX "Booking_tripId_idx" ON "Booking" ("tripId");
CREATE INDEX "Booking_passengerId_idx" ON "Booking" ("passengerId");

-- Trip: index on driverId, composite (status, departureAt), composite (isActive, departureAt)
CREATE INDEX "Trip_driverId_idx" ON "Trip" ("driverId");
CREATE INDEX "Trip_status_departureAt_idx" ON "Trip" ("status", "departureAt");
CREATE INDEX "Trip_isActive_departureAt_idx" ON "Trip" ("isActive", "departureAt");

-- Rating: index on rateeId (used for every user rating score lookup)
CREATE INDEX "Rating_rateeId_idx" ON "Rating" ("rateeId");

-- PushToken: index on userId (used for every push notification dispatch)
CREATE INDEX "PushToken_userId_idx" ON "PushToken" ("userId");

-- Migration: 20260508120000_confirmed_status_user_reports
--
-- 1. Add CONFIRMED to the TripStatus enum so the API can persist trips that
--    have had their threshold met and are confirmed to run.
--    (Mobile domain model has always had CONFIRMED; this closes the DB gap.)
--
-- 2. Create the UserReport table that was added to schema.prisma but never
--    had a corresponding migration run against the database.

-- ── 1. TripStatus CONFIRMED ──────────────────────────────────────────────────
ALTER TYPE "TripStatus" ADD VALUE IF NOT EXISTS 'CONFIRMED';

-- ── 2. UserReport table ──────────────────────────────────────────────────────
CREATE TABLE "UserReport" (
    "id"         TEXT         NOT NULL,
    "reporterId" TEXT         NOT NULL,
    "reportedId" TEXT         NOT NULL,
    "reason"     TEXT         NOT NULL,
    "createdAt"  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "UserReport_pkey" PRIMARY KEY ("id")
);

CREATE INDEX "UserReport_reportedId_idx"  ON "UserReport"("reportedId");
CREATE INDEX "UserReport_reporterId_idx"  ON "UserReport"("reporterId");

ALTER TABLE "UserReport"
    ADD CONSTRAINT "UserReport_reporterId_fkey"
    FOREIGN KEY ("reporterId") REFERENCES "User"("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "UserReport"
    ADD CONSTRAINT "UserReport_reportedId_fkey"
    FOREIGN KEY ("reportedId") REFERENCES "User"("id")
    ON DELETE CASCADE ON UPDATE CASCADE;


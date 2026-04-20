-- CreateEnum
CREATE TYPE "TripModel" AS ENUM ('A', 'B');

-- CreateEnum
CREATE TYPE "TripStatus" AS ENUM ('ACTIVE', 'CANCELLED', 'COMPLETED', 'THRESHOLD_NOT_MET');

-- AlterTable
ALTER TABLE "Trip" ADD COLUMN     "distanceKm" DOUBLE PRECISION,
ADD COLUMN     "minPassengers" INTEGER,
ADD COLUMN     "model" "TripModel" NOT NULL DEFAULT 'A',
ADD COLUMN     "recurringDays" INTEGER[],
ADD COLUMN     "status" "TripStatus" NOT NULL DEFAULT 'ACTIVE',
ADD COLUMN     "thresholdDeadline" TIMESTAMP(3);

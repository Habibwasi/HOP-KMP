/*
  Warnings:

  - You are about to drop the `Payment` table. If the table is not empty, all the data it contains will be lost.
  - You are about to drop the `TaxRecord` table. If the table is not empty, all the data it contains will be lost.

*/
-- AlterEnum
-- This migration adds more than one value to an enum.
-- With PostgreSQL versions 11 and earlier, this is not possible
-- in a single migration. This can be worked around by creating
-- multiple migrations, each migration adding only one value to
-- the enum.


ALTER TYPE "BookingStatus" ADD VALUE 'AWAITING_PAYMENT';
ALTER TYPE "BookingStatus" ADD VALUE 'COMPLETED';
ALTER TYPE "BookingStatus" ADD VALUE 'DISPUTED';

-- AlterEnum
-- This migration adds more than one value to an enum.
-- With PostgreSQL versions 11 and earlier, this is not possible
-- in a single migration. This can be worked around by creating
-- multiple migrations, each migration adding only one value to
-- the enum.


ALTER TYPE "NotificationType" ADD VALUE 'RIDE_AWAITING_PAYMENT';
ALTER TYPE "NotificationType" ADD VALUE 'PAYMENT_MARKED_PAID';
ALTER TYPE "NotificationType" ADD VALUE 'PAYMENT_CONFIRMED';

-- DropForeignKey
ALTER TABLE "Payment" DROP CONSTRAINT "Payment_bookingId_fkey";

-- DropForeignKey
ALTER TABLE "TaxRecord" DROP CONSTRAINT "TaxRecord_bookingId_fkey";

-- DropForeignKey
ALTER TABLE "TaxRecord" DROP CONSTRAINT "TaxRecord_driverId_fkey";

-- DropForeignKey
ALTER TABLE "TaxRecord" DROP CONSTRAINT "TaxRecord_tripId_fkey";

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "mobilepayNumber" TEXT;

-- DropTable
DROP TABLE "Payment";

-- DropTable
DROP TABLE "TaxRecord";

-- DropEnum
DROP TYPE "PaymentProvider";

-- DropEnum
DROP TYPE "PaymentStatus";

-- CreateTable
CREATE TABLE "RideSettlement" (
    "bookingId" TEXT NOT NULL,
    "suggestedAmountOere" INTEGER NOT NULL,
    "mobilepayNumber" TEXT NOT NULL,
    "passengerPaidAt" TIMESTAMP(3),
    "driverConfirmedAt" TIMESTAMP(3),
    "disputedAt" TIMESTAMP(3),
    "disputeReason" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "RideSettlement_pkey" PRIMARY KEY ("bookingId")
);

-- CreateTable
CREATE TABLE "SearchAlert" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "origin" TEXT NOT NULL,
    "dest" TEXT NOT NULL,
    "seats" INTEGER NOT NULL DEFAULT 1,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "SearchAlert_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "SearchAlert_isActive_origin_dest_idx" ON "SearchAlert"("isActive", "origin", "dest");

-- CreateIndex
CREATE UNIQUE INDEX "SearchAlert_userId_origin_dest_key" ON "SearchAlert"("userId", "origin", "dest");

-- AddForeignKey
ALTER TABLE "RideSettlement" ADD CONSTRAINT "RideSettlement_bookingId_fkey" FOREIGN KEY ("bookingId") REFERENCES "Booking"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "SearchAlert" ADD CONSTRAINT "SearchAlert_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

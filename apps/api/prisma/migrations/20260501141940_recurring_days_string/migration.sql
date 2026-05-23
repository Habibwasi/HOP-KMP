/*
  Warnings:

  - You are about to drop the `SearchAlert` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "SearchAlert" DROP CONSTRAINT "SearchAlert_userId_fkey";

-- AlterTable
ALTER TABLE "Trip" ALTER COLUMN "recurringDays" SET DATA TYPE TEXT[];

-- DropTable
DROP TABLE "SearchAlert";

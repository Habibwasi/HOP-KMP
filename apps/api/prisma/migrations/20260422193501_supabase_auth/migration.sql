/*
  Warnings:

  - You are about to drop the column `passwordHash` on the `User` table. All the data in the column will be lost.
  - You are about to drop the `OtpCode` table. If the table is not empty, all the data it contains will be lost.
  - You are about to drop the `RefreshToken` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "OtpCode" DROP CONSTRAINT "OtpCode_userId_fkey";

-- DropForeignKey
ALTER TABLE "RefreshToken" DROP CONSTRAINT "RefreshToken_userId_fkey";

-- AlterTable
ALTER TABLE "User" DROP COLUMN "passwordHash",
ALTER COLUMN "phone" DROP NOT NULL;

-- DropTable
DROP TABLE "OtpCode";

-- DropTable
DROP TABLE "RefreshToken";

-- DropEnum
DROP TYPE "OtpPurpose";

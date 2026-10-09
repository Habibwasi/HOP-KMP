-- CreateEnum
CREATE TYPE "WaitlistRole" AS ENUM ('PASSENGER', 'DRIVER', 'BOTH');

-- CreateTable
CREATE TABLE "WaitlistSignup" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "role" "WaitlistRole" NOT NULL,
    "origin" TEXT,
    "dest" TEXT,
    "locale" TEXT NOT NULL DEFAULT 'da',
    "source" TEXT,
    "token" TEXT NOT NULL,
    "consentAt" TIMESTAMP(3) NOT NULL,
    "confirmSentAt" TIMESTAMP(3),
    "confirmedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "WaitlistSignup_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "WaitlistSignup_email_key" ON "WaitlistSignup"("email");

-- CreateIndex
CREATE UNIQUE INDEX "WaitlistSignup_token_key" ON "WaitlistSignup"("token");

-- CreateIndex
CREATE INDEX "WaitlistSignup_confirmedAt_idx" ON "WaitlistSignup"("confirmedAt");

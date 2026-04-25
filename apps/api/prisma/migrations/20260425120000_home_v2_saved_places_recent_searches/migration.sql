-- CreateEnum
CREATE TYPE "SavedPlaceKind" AS ENUM ('HOME', 'WORK', 'CUSTOM');

-- CreateTable
CREATE TABLE "SavedPlace" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "label" TEXT NOT NULL,
    "address" TEXT NOT NULL,
    "lat" DOUBLE PRECISION,
    "lng" DOUBLE PRECISION,
    "kind" "SavedPlaceKind" NOT NULL DEFAULT 'CUSTOM',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "SavedPlace_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "SavedPlace_userId_createdAt_idx" ON "SavedPlace"("userId", "createdAt");

-- CreateIndex
CREATE UNIQUE INDEX "SavedPlace_userId_label_key" ON "SavedPlace"("userId", "label");

-- AddForeignKey
ALTER TABLE "SavedPlace" ADD CONSTRAINT "SavedPlace_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- CreateTable
CREATE TABLE "RecentSearch" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "originLabel" TEXT NOT NULL,
    "destLabel" TEXT NOT NULL,
    "lastUsedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "useCount" INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT "RecentSearch_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "RecentSearch_userId_originLabel_destLabel_key" ON "RecentSearch"("userId", "originLabel", "destLabel");

-- CreateIndex
CREATE INDEX "RecentSearch_userId_lastUsedAt_idx" ON "RecentSearch"("userId", "lastUsedAt");

-- AddForeignKey
ALTER TABLE "RecentSearch" ADD CONSTRAINT "RecentSearch_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- CreateIndex
CREATE INDEX "Notification_userId_isRead_idx" ON "Notification"("userId", "isRead");

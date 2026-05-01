-- AlterEnum
ALTER TYPE "NotificationType" ADD VALUE 'SEARCH_ALERT';

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
ALTER TABLE "SearchAlert" ADD CONSTRAINT "SearchAlert_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

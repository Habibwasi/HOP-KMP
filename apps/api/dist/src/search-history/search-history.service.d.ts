import { PrismaService } from '../prisma/prisma.service';
export declare class SearchHistoryService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    list(userId: string, limit: number): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        userId: string;
        originLabel: string;
        destLabel: string;
        lastUsedAt: Date;
        useCount: number;
    }[]>;
    private static readonly MAX_RECENT_SEARCHES;
    record(userId: string, originLabel: string, destLabel: string): Promise<{
        id: string;
        userId: string;
        originLabel: string;
        destLabel: string;
        lastUsedAt: Date;
        useCount: number;
    }>;
    remove(userId: string, id: string): Promise<{
        ok: boolean;
    }>;
}

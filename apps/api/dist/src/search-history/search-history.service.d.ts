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
    record(userId: string, originLabel: string, destLabel: string): import("@prisma/client").Prisma.Prisma__RecentSearchClient<{
        id: string;
        userId: string;
        originLabel: string;
        destLabel: string;
        lastUsedAt: Date;
        useCount: number;
    }, never, import("@prisma/client/runtime/client").DefaultArgs, import("@prisma/client").Prisma.PrismaClientOptions>;
    remove(userId: string, id: string): Promise<{
        ok: boolean;
    }>;
}

import { SearchHistoryService } from './search-history.service';
export declare class RecordSearchDto {
    originLabel: string;
    destLabel: string;
}
declare class ListQueryDto {
    limit?: number;
}
export declare class SearchHistoryController {
    private readonly history;
    constructor(history: SearchHistoryService);
    list(req: any, q: ListQueryDto): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        userId: string;
        originLabel: string;
        destLabel: string;
        lastUsedAt: Date;
        useCount: number;
    }[]>;
    record(req: any, dto: RecordSearchDto): Promise<{
        id: string;
        userId: string;
        originLabel: string;
        destLabel: string;
        lastUsedAt: Date;
        useCount: number;
    }>;
    remove(req: any, id: string): Promise<{
        ok: boolean;
    }>;
}
export {};

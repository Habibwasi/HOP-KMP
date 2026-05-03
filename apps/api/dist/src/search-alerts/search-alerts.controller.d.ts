import { SearchAlertsService } from './search-alerts.service';
import { CreateSearchAlertDto } from './dto/create-search-alert.dto';
export declare class SearchAlertsController {
    private readonly service;
    constructor(service: SearchAlertsService);
    create(req: any, dto: CreateSearchAlertDto): Promise<{
        id: string;
        createdAt: Date;
        userId: string;
        seats: number;
        isActive: boolean;
        origin: string;
        dest: string;
    }>;
    list(req: any): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        createdAt: Date;
        userId: string;
        seats: number;
        isActive: boolean;
        origin: string;
        dest: string;
    }[]>;
    remove(req: any, id: string): Promise<{
        ok: boolean;
    }>;
}

import { SearchAlertsService } from './search-alerts.service';
import { CreateSearchAlertDto } from './dto/create-search-alert.dto';
export declare class SearchAlertsController {
    private readonly service;
    constructor(service: SearchAlertsService);
    create(req: any, dto: CreateSearchAlertDto): Promise<{
        id: string;
        userId: string;
        createdAt: Date;
        origin: string;
        dest: string;
        seats: number;
        isActive: boolean;
    }>;
    list(req: any): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        userId: string;
        createdAt: Date;
        origin: string;
        dest: string;
        seats: number;
        isActive: boolean;
    }[]>;
    remove(req: any, id: string): Promise<{
        ok: boolean;
    }>;
}

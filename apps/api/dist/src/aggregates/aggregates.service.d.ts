import { PrismaService } from '../prisma/prisma.service';
export declare class AggregatesService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    getDriverEarningsSeries(driverId: string, days: number): Promise<{
        series: {
            date: string;
            earningsOere: number;
        }[];
        totalOere: number;
    }>;
    getPopularRoutes(limit: number): Promise<{
        routes: {
            origin: string;
            destination: string;
            tripsThisWeek: number;
        }[];
    }>;
    getDemandHotspots(): Promise<{
        hotspots: {
            areaName: string;
            demandCount: number;
            lat: number;
            lng: number;
        }[];
    }>;
    getUserCo2Saved(userId: string): Promise<{
        co2SavedKg: number;
    }>;
}

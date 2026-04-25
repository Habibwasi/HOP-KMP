import { AggregatesService } from './aggregates.service';
export declare class AggregatesController {
    private readonly aggregates;
    constructor(aggregates: AggregatesService);
    driverEarningsSeries(req: any, daysStr?: string): Promise<{
        series: {
            date: string;
            earningsOere: number;
        }[];
        totalOere: number;
    }>;
    popularRoutes(limitStr?: string): Promise<{
        routes: {
            origin: string;
            destination: string;
            tripsThisWeek: number;
        }[];
    }>;
    demandHotspots(): Promise<{
        hotspots: {
            areaName: string;
            demandCount: number;
            lat: number;
            lng: number;
        }[];
    }>;
    co2Saved(req: any): Promise<{
        co2SavedKg: number;
    }>;
}

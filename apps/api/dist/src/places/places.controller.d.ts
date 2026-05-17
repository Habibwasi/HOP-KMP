import { PlacesService } from './places.service';
export declare class UpsertPlaceDto {
    label: string;
    address: string;
    lat?: number;
    lng?: number;
    kind?: 'HOME' | 'WORK' | 'CUSTOM';
}
export declare class PlacesController {
    private readonly places;
    constructor(places: PlacesService);
    list(req: any): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
        createdAt: Date;
        updatedAt: Date;
    }[]>;
    geocode(address: string): Promise<{
        lat: number;
        lng: number;
        formattedAddress: string;
    }>;
    route(origin: string, dest: string): Promise<{
        distanceMetres: number;
        originLat: number;
        originLng: number;
        destLat: number;
        destLng: number;
    }>;
    autocomplete(q: string): Promise<{
        suggestions: {
            name: string;
            fullAddress: string;
            lat: number;
            lng: number;
        }[];
    }>;
    create(req: any, dto: UpsertPlaceDto): import("@prisma/client").Prisma.Prisma__SavedPlaceClient<{
        id: string;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
        createdAt: Date;
        updatedAt: Date;
    }, never, import("@prisma/client/runtime/client").DefaultArgs, import("@prisma/client").Prisma.PrismaClientOptions>;
    update(req: any, id: string, dto: UpsertPlaceDto): Promise<{
        id: string;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
        createdAt: Date;
        updatedAt: Date;
    }>;
    remove(req: any, id: string): Promise<{
        ok: boolean;
    }>;
}

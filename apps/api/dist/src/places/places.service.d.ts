import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
import { UpsertPlaceDto } from './places.controller';
export declare class PlacesService {
    private readonly prisma;
    private readonly config;
    constructor(prisma: PrismaService, config: ConfigService);
    listForUser(userId: string): import("@prisma/client").Prisma.PrismaPromise<{
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
    create(userId: string, dto: UpsertPlaceDto): import("@prisma/client").Prisma.Prisma__SavedPlaceClient<{
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
    update(userId: string, id: string, dto: UpsertPlaceDto): Promise<{
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
    remove(userId: string, id: string): Promise<{
        ok: boolean;
    }>;
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
    autocomplete(query: string): Promise<{
        suggestions: {
            name: string;
            fullAddress: string;
            lat: number;
            lng: number;
        }[];
    }>;
    private mapboxKey;
}

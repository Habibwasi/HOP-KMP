import { PrismaService } from '../prisma/prisma.service';
import { UpsertPlaceDto } from './places.controller';
export declare class PlacesService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    listForUser(userId: string): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
    }[]>;
    create(userId: string, dto: UpsertPlaceDto): import("@prisma/client").Prisma.Prisma__SavedPlaceClient<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
    }, never, import("@prisma/client/runtime/client").DefaultArgs, import("@prisma/client").Prisma.PrismaClientOptions>;
    update(userId: string, id: string, dto: UpsertPlaceDto): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
        label: string;
        address: string;
        lat: number | null;
        lng: number | null;
        kind: import("@prisma/client").$Enums.SavedPlaceKind;
    }>;
    remove(userId: string, id: string): Promise<{
        ok: boolean;
    }>;
}

"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AggregatesService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
const CO2_KG_PER_TRIP = 4.2;
const toLocalDateKey = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
let AggregatesService = class AggregatesService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getDriverEarningsSeries(driverId, days) {
        const since = new Date();
        since.setHours(0, 0, 0, 0);
        since.setDate(since.getDate() - (days - 1));
        const bookings = await this.prisma.booking.findMany({
            where: {
                status: { in: [client_1.BookingStatus.AWAITING_PAYMENT, client_1.BookingStatus.COMPLETED] },
                trip: {
                    driverId,
                    status: client_1.TripStatus.COMPLETED,
                    departureAt: { gte: since },
                },
            },
            select: { totalOere: true, trip: { select: { departureAt: true } } },
        });
        const dailyMap = new Map();
        let totalOere = 0;
        for (const b of bookings) {
            const dateKey = toLocalDateKey(b.trip.departureAt);
            dailyMap.set(dateKey, (dailyMap.get(dateKey) ?? 0) + b.totalOere);
            totalOere += b.totalOere;
        }
        const series = [];
        for (let i = days - 1; i >= 0; i--) {
            const d = new Date();
            d.setHours(0, 0, 0, 0);
            d.setDate(d.getDate() - i);
            const key = toLocalDateKey(d);
            series.push({ date: key, earningsOere: dailyMap.get(key) ?? 0 });
        }
        return { series, totalOere };
    }
    async getPopularRoutes(limit) {
        const since = new Date();
        since.setDate(since.getDate() - 7);
        const trips = await this.prisma.trip.findMany({
            where: { departureAt: { gte: since } },
            select: {
                originAddress: true,
                destAddress: true,
                bookings: {
                    where: { status: client_1.BookingStatus.CONFIRMED },
                    select: { id: true },
                },
            },
        });
        const map = new Map();
        for (const t of trips) {
            const key = `${t.originAddress}|${t.destAddress}`;
            const cur = map.get(key) ?? {
                origin: t.originAddress,
                destination: t.destAddress,
                count: 0,
            };
            cur.count += t.bookings.length;
            map.set(key, cur);
        }
        const routes = Array.from(map.values())
            .sort((a, b) => b.count - a.count)
            .slice(0, limit)
            .map((r) => ({
            origin: r.origin,
            destination: r.destination,
            tripsThisWeek: r.count,
        }));
        return { routes };
    }
    async getDemandHotspots() {
        const since = new Date();
        since.setDate(since.getDate() - 7);
        const trips = await this.prisma.trip.findMany({
            where: { departureAt: { gte: since } },
            orderBy: { createdAt: 'desc' },
            select: {
                originAddress: true,
                originLat: true,
                originLng: true,
                bookings: {
                    where: { status: client_1.BookingStatus.CONFIRMED },
                    select: { id: true },
                },
            },
        });
        const map = new Map();
        for (const t of trips) {
            const cur = map.get(t.originAddress) ?? {
                areaName: t.originAddress,
                lat: t.originLat,
                lng: t.originLng,
                count: 0,
            };
            cur.count += t.bookings.length;
            map.set(t.originAddress, cur);
        }
        const hotspots = Array.from(map.values())
            .filter((h) => h.count > 0)
            .sort((a, b) => b.count - a.count)
            .slice(0, 5)
            .map((h) => ({
            areaName: h.areaName,
            demandCount: h.count,
            lat: h.lat,
            lng: h.lng,
        }));
        return { hotspots };
    }
    async getUserCo2Saved(userId) {
        const completedTrips = await this.prisma.booking.count({
            where: {
                passengerId: userId,
                status: client_1.BookingStatus.CONFIRMED,
                trip: { status: client_1.TripStatus.COMPLETED },
            },
        });
        const co2SavedKg = Math.round(completedTrips * CO2_KG_PER_TRIP);
        return { co2SavedKg };
    }
};
exports.AggregatesService = AggregatesService;
exports.AggregatesService = AggregatesService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], AggregatesService);
//# sourceMappingURL=aggregates.service.js.map
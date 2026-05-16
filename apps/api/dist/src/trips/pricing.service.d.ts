export declare class PricingService {
    private readonly RATE_PER_KM;
    calculatePricePerSeat(distanceKm: number, seats: number): number;
    calculateDistance(originLat: number, originLng: number, destLat: number, destLng: number): number;
    private toRad;
}

export declare class PricingService {
    private readonly RATE_PER_KM;
    private readonly PLATFORM_FEE_RATE;
    calculatePricePerSeat(distanceKm: number, seats: number): number;
    calculateDistance(originLat: number, originLng: number, destLat: number, destLng: number): number;
    private toRad;
}

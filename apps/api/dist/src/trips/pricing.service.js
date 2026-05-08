"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PricingService = void 0;
const common_1 = require("@nestjs/common");
const tax_constants_1 = require("../common/tax-constants");
let PricingService = class PricingService {
    RATE_PER_KM = tax_constants_1.SKAT_RATE_DKK_PER_KM;
    PLATFORM_FEE_RATE = 0.15;
    calculatePricePerSeat(distanceKm, seats) {
        const totalTripCostOere = Math.round(distanceKm * this.RATE_PER_KM * 100);
        const driverNetPerSeatOere = Math.floor(totalTripCostOere / seats);
        const passengerPaysPerSeatOere = Math.round(driverNetPerSeatOere / (1 - this.PLATFORM_FEE_RATE));
        return Math.max(passengerPaysPerSeatOere, 100);
    }
    calculateDistance(originLat, originLng, destLat, destLng) {
        const R = 6371;
        const dLat = this.toRad(destLat - originLat);
        const dLng = this.toRad(destLng - originLng);
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(this.toRad(originLat)) *
                Math.cos(this.toRad(destLat)) *
                Math.sin(dLng / 2) *
                Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    toRad(deg) {
        return (deg * Math.PI) / 180;
    }
};
exports.PricingService = PricingService;
exports.PricingService = PricingService = __decorate([
    (0, common_1.Injectable)()
], PricingService);
//# sourceMappingURL=pricing.service.js.map
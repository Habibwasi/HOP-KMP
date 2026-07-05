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
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.BookingsController = void 0;
const common_1 = require("@nestjs/common");
const class_validator_1 = require("class-validator");
const supabase_guard_1 = require("../auth/supabase.guard");
const bookings_service_1 = require("./bookings.service");
const ratings_service_1 = require("../ratings/ratings.service");
const create_booking_dto_1 = require("./dto/create-booking.dto");
class RateBookingDto {
    stars;
    comment;
}
__decorate([
    (0, class_validator_1.IsInt)(),
    (0, class_validator_1.Min)(1),
    (0, class_validator_1.Max)(5),
    __metadata("design:type", Number)
], RateBookingDto.prototype, "stars", void 0);
__decorate([
    (0, class_validator_1.IsOptional)(),
    (0, class_validator_1.IsString)(),
    (0, class_validator_1.MaxLength)(500),
    __metadata("design:type", String)
], RateBookingDto.prototype, "comment", void 0);
let BookingsController = class BookingsController {
    bookings;
    ratings;
    constructor(bookings, ratings) {
        this.bookings = bookings;
        this.ratings = ratings;
    }
    create(req, dto) {
        return this.bookings.create(req.user.id, dto);
    }
    myBookings(req) {
        return this.bookings.findByPassenger(req.user.id);
    }
    activeForMe(req) {
        return this.bookings.findActiveForPassenger(req.user.id);
    }
    myChats(req) {
        return this.bookings.findMyChats(req.user.id);
    }
    findOne(id, req) {
        return this.bookings.findByIdAuthorized(id, req.user.id);
    }
    cancel(id, req) {
        return this.bookings.cancel(id, req.user.id);
    }
    confirm(id, req) {
        return this.bookings.confirm(id, req.user.id);
    }
    async rate(bookingId, req, body) {
        const booking = await this.bookings.findById(bookingId);
        const raterId = req.user.id;
        const rateeId = raterId === booking.passengerId ? booking.trip.driverId : booking.passengerId;
        return this.ratings.create(raterId, {
            rateeId,
            tripId: booking.tripId,
            score: body.stars,
            comment: body.comment,
        });
    }
};
exports.BookingsController = BookingsController;
__decorate([
    (0, common_1.Post)(),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_booking_dto_1.CreateBookingDto]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "create", null);
__decorate([
    (0, common_1.Get)('my'),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "myBookings", null);
__decorate([
    (0, common_1.Get)('me/active'),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "activeForMe", null);
__decorate([
    (0, common_1.Get)('my-chats'),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "myChats", null);
__decorate([
    (0, common_1.Get)(':id'),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "findOne", null);
__decorate([
    (0, common_1.Patch)(':id/cancel'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "cancel", null);
__decorate([
    (0, common_1.Patch)(':id/confirm'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], BookingsController.prototype, "confirm", null);
__decorate([
    (0, common_1.Post)(':id/rate'),
    (0, common_1.HttpCode)(201),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Req)()),
    __param(2, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, RateBookingDto]),
    __metadata("design:returntype", Promise)
], BookingsController.prototype, "rate", null);
exports.BookingsController = BookingsController = __decorate([
    (0, common_1.Controller)('bookings'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __metadata("design:paramtypes", [bookings_service_1.BookingsService,
        ratings_service_1.RatingsService])
], BookingsController);
//# sourceMappingURL=bookings.controller.js.map
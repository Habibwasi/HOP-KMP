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
var UsersController_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.UsersController = void 0;
const common_1 = require("@nestjs/common");
const supabase_js_1 = require("@supabase/supabase-js");
const supabase_guard_1 = require("../auth/supabase.guard");
const users_service_1 = require("./users.service");
const ratings_service_1 = require("../ratings/ratings.service");
const notifications_service_1 = require("../notifications/notifications.service");
const create_profile_dto_1 = require("./dto/create-profile.dto");
const update_user_dto_1 = require("./dto/update-user.dto");
const create_car_details_dto_1 = require("./dto/create-car-details.dto");
const class_validator_1 = require("class-validator");
class ReportDto {
    reason;
}
__decorate([
    (0, class_validator_1.IsString)(),
    (0, class_validator_1.MinLength)(1),
    __metadata("design:type", String)
], ReportDto.prototype, "reason", void 0);
class PushTokenDto {
    token;
    platform;
}
__decorate([
    (0, class_validator_1.IsString)(),
    __metadata("design:type", String)
], PushTokenDto.prototype, "token", void 0);
__decorate([
    (0, class_validator_1.IsString)(),
    __metadata("design:type", String)
], PushTokenDto.prototype, "platform", void 0);
let UsersController = UsersController_1 = class UsersController {
    users;
    ratings;
    notifications;
    supabase;
    logger = new common_1.Logger(UsersController_1.name);
    constructor(users, ratings, notifications, supabase) {
        this.users = users;
        this.ratings = ratings;
        this.notifications = notifications;
        this.supabase = supabase;
    }
    async createProfile(req, dto) {
        const auth = req.headers?.authorization;
        if (!auth?.startsWith('Bearer '))
            throw new common_1.UnauthorizedException();
        const token = auth.slice(7);
        const { data: { user: supabaseUser }, error } = await this.supabase.auth.getUser(token);
        if (error || !supabaseUser)
            throw new common_1.UnauthorizedException();
        try {
            return await this.users.createProfile(supabaseUser.id, {
                firstName: dto.firstName,
                lastName: dto.lastName,
                phone: dto.phone,
                email: dto.email ?? supabaseUser.email,
            });
        }
        catch (err) {
            const { error: deleteError } = await this.supabase.auth.admin.deleteUser(supabaseUser.id);
            if (deleteError) {
                this.logger.error(`Failed to delete dangling Supabase user ${supabaseUser.id}: ${deleteError.message}`);
            }
            throw err;
        }
    }
    async getMe(req) {
        const user = await this.users.findById(req.user.id);
        if (!user)
            throw new common_1.NotFoundException('User not found');
        return user;
    }
    async getMyStats(req) {
        const userId = req.user.id;
        const [ratingSummary, completedTrips] = await Promise.all([
            this.ratings.getUserRatings(userId),
            this.users.completedTripCount(userId),
        ]);
        return {
            averageRating: ratingSummary.averageScore,
            totalRatings: ratingSummary.totalRatings,
            completedTrips,
        };
    }
    async updateMe(req, dto) {
        const data = {};
        if (dto.fullName?.trim()) {
            const [firstName, ...rest] = dto.fullName.trim().split(' ');
            data.firstName = firstName;
            data.lastName = rest.join(' ') || '.';
        }
        if (dto.mobilepayNumber !== undefined) {
            data.mobilepayNumber = dto.mobilepayNumber;
        }
        return this.users.updateProfile(req.user.id, data);
    }
    async savePushToken(req, dto) {
        const platform = dto.platform === 'ios' ? 'ios' : 'android';
        await this.notifications.registerToken(req.user.id, dto.token, platform);
    }
    async getUserById(id) {
        const user = await this.users.findById(id);
        if (!user)
            throw new common_1.NotFoundException('User not found');
        return user;
    }
    async getUserReviews(id) {
        const result = await this.ratings.getUserRatings(id);
        return result.ratings.map((r) => ({
            id: r.id,
            raterName: `${r.rater.firstName} ${r.rater.lastName}`.trim(),
            stars: r.score,
            comment: r.comment ?? null,
            roleRated: 'PASSENGER',
        }));
    }
    async getCarDetails(id) {
        const car = await this.users.getCarDetails(id);
        if (!car)
            throw new common_1.NotFoundException('No car details found');
        return car;
    }
    async saveMyCarDetails(req, dto) {
        return this.users.saveCarDetails(req.user.id, {
            make: dto.make,
            model: dto.model,
            year: dto.year,
            licensePlate: dto.license_plate,
            colour: dto.colour,
            seatsAvailable: dto.seats_available,
        });
    }
    async reportUser(req, id, dto) {
        await this.users.reportUser(id, req.user.id, dto.reason);
    }
};
exports.UsersController = UsersController;
__decorate([
    (0, common_1.Post)('profile'),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_profile_dto_1.CreateProfileDto]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "createProfile", null);
__decorate([
    (0, common_1.Get)('me'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "getMe", null);
__decorate([
    (0, common_1.Get)('me/stats'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "getMyStats", null);
__decorate([
    (0, common_1.Patch)('me'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, update_user_dto_1.UpdateUserDto]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "updateMe", null);
__decorate([
    (0, common_1.Post)('push-token'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    (0, common_1.HttpCode)(common_1.HttpStatus.OK),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, PushTokenDto]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "savePushToken", null);
__decorate([
    (0, common_1.Get)(':id'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "getUserById", null);
__decorate([
    (0, common_1.Get)(':id/reviews'),
    __param(0, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "getUserReviews", null);
__decorate([
    (0, common_1.Get)(':id/car'),
    __param(0, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "getCarDetails", null);
__decorate([
    (0, common_1.Post)('me/car-details'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    (0, common_1.HttpCode)(common_1.HttpStatus.OK),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_car_details_dto_1.CreateCarDetailsDto]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "saveMyCarDetails", null);
__decorate([
    (0, common_1.Post)(':id/report'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    (0, common_1.HttpCode)(common_1.HttpStatus.OK),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Param)('id')),
    __param(2, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, ReportDto]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "reportUser", null);
exports.UsersController = UsersController = UsersController_1 = __decorate([
    (0, common_1.Controller)('users'),
    __param(3, (0, common_1.Inject)('SUPABASE_CLIENT')),
    __metadata("design:paramtypes", [users_service_1.UsersService,
        ratings_service_1.RatingsService,
        notifications_service_1.NotificationsService,
        supabase_js_1.SupabaseClient])
], UsersController);
//# sourceMappingURL=users.controller.js.map
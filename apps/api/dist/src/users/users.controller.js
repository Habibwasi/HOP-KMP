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
const platform_express_1 = require("@nestjs/platform-express");
const supabase_js_1 = require("@supabase/supabase-js");
const supabase_guard_1 = require("../auth/supabase.guard");
const users_service_1 = require("./users.service");
const ratings_service_1 = require("../ratings/ratings.service");
const notifications_service_1 = require("../notifications/notifications.service");
const create_profile_dto_1 = require("./dto/create-profile.dto");
const update_user_dto_1 = require("./dto/update-user.dto");
const create_car_details_dto_1 = require("./dto/create-car-details.dto");
const class_validator_1 = require("class-validator");
const app_exception_1 = require("../common/errors/app-exception");
const api_error_codes_1 = require("../common/errors/api-error-codes");
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
    async onModuleInit() {
        const { error } = await this.supabase.storage.createBucket('avatars', {
            public: true,
            allowedMimeTypes: ['image/jpeg', 'image/png', 'image/webp'],
            fileSizeLimit: 5 * 1024 * 1024,
        });
        if (error) {
            if (error.message.toLowerCase().includes('already exist')) {
                const { error: updateError } = await this.supabase.storage.updateBucket('avatars', {
                    public: true,
                    allowedMimeTypes: ['image/jpeg', 'image/png', 'image/webp'],
                    fileSizeLimit: 5 * 1024 * 1024,
                });
                if (updateError)
                    this.logger.warn(`avatars bucket update: ${updateError.message}`);
            }
            else {
                this.logger.warn(`avatars bucket: ${error.message}`);
            }
        }
    }
    async createProfile(req, dto) {
        const auth = req.headers?.authorization;
        if (!auth?.startsWith('Bearer '))
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.TOKEN_MISSING);
        const token = auth.slice(7);
        const { data: { user: supabaseUser }, error } = await this.supabase.auth.getUser(token);
        if (error || !supabaseUser)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.TOKEN_INVALID);
        try {
            return await this.users.createProfile(supabaseUser.id, {
                firstName: dto.firstName,
                lastName: dto.lastName,
                phone: dto.phone,
                email: dto.email ?? supabaseUser.email,
            });
        }
        catch (err) {
            const MAX_DELETE_ATTEMPTS = 3;
            let lastDeleteError = null;
            for (let attempt = 1; attempt <= MAX_DELETE_ATTEMPTS; attempt++) {
                const { error: deleteError } = await this.supabase.auth.admin.deleteUser(supabaseUser.id);
                if (!deleteError) {
                    lastDeleteError = null;
                    break;
                }
                lastDeleteError = deleteError;
                if (attempt < MAX_DELETE_ATTEMPTS) {
                    await new Promise(r => setTimeout(r, 200 * attempt));
                }
            }
            if (lastDeleteError) {
                this.logger.error(`DANGLING_AUTH_USER supabaseId=${supabaseUser.id} email=${supabaseUser.email} ` +
                    `deleteError="${lastDeleteError.message}" originalError="${err.message}" ` +
                    `— manual cleanup required in Supabase dashboard`);
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.DANGLING_AUTH_USER);
            }
            throw err;
        }
    }
    async getMe(req) {
        const user = await this.users.findById(req.user.id);
        if (!user)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.USER_NOT_FOUND);
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
    async uploadAvatar(req, file) {
        if (!file)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.VALIDATION_ERROR);
        const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!allowedMimes.includes(file.mimetype))
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.VALIDATION_ERROR);
        const ext = file.mimetype === 'image/png' ? 'png' : file.mimetype === 'image/webp' ? 'webp' : 'jpg';
        const path = `${req.user.id}/avatar.${ext}`;
        const { error: uploadError } = await this.supabase.storage
            .from('avatars')
            .upload(path, file.buffer, { contentType: file.mimetype, upsert: true, cacheControl: '3600' });
        if (uploadError) {
            this.logger.error(`Supabase avatar upload failed: ${uploadError.message}`);
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.INTERNAL_ERROR);
        }
        const { data: { publicUrl: rawPublicUrl } } = this.supabase.storage.from('avatars').getPublicUrl(path);
        const publicUrl = `${rawPublicUrl}?t=${Date.now()}`;
        return this.users.updateAvatarUrl(req.user.id, publicUrl);
    }
    async savePushToken(req, dto) {
        const platform = dto.platform === 'ios' ? 'ios' : 'android';
        await this.notifications.registerToken(req.user.id, dto.token, platform);
    }
    async getUserById(id) {
        const user = await this.users.findById(id);
        if (!user)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.USER_NOT_FOUND);
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
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.CAR_NOT_FOUND);
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
    (0, common_1.Post)('me/avatar'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    (0, common_1.UseInterceptors)((0, platform_express_1.FileInterceptor)('file', { limits: { fileSize: 5 * 1024 * 1024 } })),
    (0, common_1.HttpCode)(common_1.HttpStatus.OK),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.UploadedFile)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "uploadAvatar", null);
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
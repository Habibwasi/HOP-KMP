"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AppModule = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const bullmq_1 = require("@nestjs/bullmq");
const schedule_1 = require("@nestjs/schedule");
const prisma_module_1 = require("./prisma/prisma.module");
const auth_module_1 = require("./auth/auth.module");
const users_module_1 = require("./users/users.module");
const trips_module_1 = require("./trips/trips.module");
const bookings_module_1 = require("./bookings/bookings.module");
const settlements_module_1 = require("./settlements/settlements.module");
const ratings_module_1 = require("./ratings/ratings.module");
const notifications_module_1 = require("./notifications/notifications.module");
const admin_module_1 = require("./admin/admin.module");
const places_module_1 = require("./places/places.module");
const search_history_module_1 = require("./search-history/search-history.module");
const aggregates_module_1 = require("./aggregates/aggregates.module");
const chat_module_1 = require("./chat/chat.module");
const health_controller_1 = require("./health.controller");
let AppModule = class AppModule {
};
exports.AppModule = AppModule;
exports.AppModule = AppModule = __decorate([
    (0, common_1.Module)({
        controllers: [health_controller_1.HealthController],
        imports: [
            config_1.ConfigModule.forRoot({ isGlobal: true }),
            schedule_1.ScheduleModule.forRoot(),
            bullmq_1.BullModule.forRoot({
                connection: process.env.REDIS_URL
                    ? (() => {
                        const u = new URL(process.env.REDIS_URL);
                        return {
                            host: u.hostname,
                            port: parseInt(u.port || '6379'),
                            username: u.username ? decodeURIComponent(u.username) : undefined,
                            password: u.password ? decodeURIComponent(u.password) : undefined,
                            tls: u.protocol === 'rediss:' ? {} : undefined,
                        };
                    })()
                    : {
                        host: process.env.REDIS_HOST ?? 'localhost',
                        port: parseInt(process.env.REDIS_PORT ?? '6379'),
                        password: process.env.REDIS_PASSWORD || undefined,
                    },
            }),
            prisma_module_1.PrismaModule,
            auth_module_1.AuthModule,
            users_module_1.UsersModule,
            trips_module_1.TripsModule,
            bookings_module_1.BookingsModule,
            settlements_module_1.SettlementsModule,
            ratings_module_1.RatingsModule,
            notifications_module_1.NotificationsModule,
            admin_module_1.AdminModule,
            places_module_1.PlacesModule,
            search_history_module_1.SearchHistoryModule,
            aggregates_module_1.AggregatesModule,
            chat_module_1.ChatModule,
        ],
    })
], AppModule);
//# sourceMappingURL=app.module.js.map
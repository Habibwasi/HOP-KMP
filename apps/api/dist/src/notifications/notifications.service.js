"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var NotificationsService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.NotificationsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const prisma_service_1 = require("../prisma/prisma.service");
const node_fs_1 = require("node:fs");
const apn = __importStar(require("apn"));
const admin = __importStar(require("firebase-admin"));
let NotificationsService = NotificationsService_1 = class NotificationsService {
    prisma;
    config;
    logger = new common_1.Logger(NotificationsService_1.name);
    apnProvider = null;
    fcmInitialised = false;
    constructor(prisma, config) {
        this.prisma = prisma;
        this.config = config;
        this.initApn();
        this.initFcm();
    }
    initApn() {
        const keyPath = this.config.get('APNS_KEY_PATH');
        const keyId = this.config.get('APNS_KEY_ID');
        const teamId = this.config.get('APNS_TEAM_ID');
        if (!keyPath || !keyId || !teamId || !(0, node_fs_1.existsSync)(keyPath)) {
            this.logger.warn('APNs not initialised - credentials are missing in this environment');
            return;
        }
        try {
            this.apnProvider = new apn.Provider({
                token: {
                    key: keyPath,
                    keyId,
                    teamId,
                },
                production: this.config.get('NODE_ENV') === 'production',
            });
        }
        catch (e) {
            this.logger.error(`APNs not initialised - ${e.message}`);
        }
    }
    initFcm() {
        const projectId = this.config.get('FIREBASE_PROJECT_ID');
        const clientEmail = this.config.get('FIREBASE_CLIENT_EMAIL');
        const privateKey = this.config.get('FIREBASE_PRIVATE_KEY');
        if (!projectId || !clientEmail || !privateKey || privateKey === 'your_private_key') {
            this.logger.warn('FCM not initialised - credentials are missing in this environment');
            return;
        }
        try {
            if (!admin.apps.length) {
                admin.initializeApp({
                    credential: admin.credential.cert({
                        projectId,
                        clientEmail,
                        privateKey: privateKey.replace(/\\n/g, '\n'),
                    }),
                });
            }
            this.fcmInitialised = true;
        }
        catch (e) {
            this.logger.error(`FCM not initialised - ${e.message}`);
        }
    }
    async sendToUser(userId, title, body, data) {
        const tokens = await this.prisma.pushToken.findMany({ where: { userId } });
        if (!tokens.length)
            return;
        const ios = tokens.filter((t) => t.platform === 'ios');
        const android = tokens.filter((t) => t.platform === 'android');
        await Promise.allSettled([
            ...ios.map((t) => this.sendApns(t.token, title, body, data)),
            ...android.map((t) => this.sendFcm(t.token, title, body, data)),
        ]);
    }
    async sendApns(token, title, body, data) {
        if (!this.apnProvider)
            return;
        const note = new apn.Notification();
        note.alert = { title, body };
        note.topic = this.config.getOrThrow('APNS_BUNDLE_ID');
        note.sound = 'default';
        note.payload = data ?? {};
        const result = await this.apnProvider.send(note, token);
        if (result.failed.length) {
            this.logger.error(`APNs failed: ${JSON.stringify(result.failed)}`);
            const unregistered = result.failed
                .filter((f) => f.response?.reason === 'Unregistered' || f.response?.reason === 'BadDeviceToken')
                .map((f) => f.device);
            if (unregistered.length) {
                await this.prisma.pushToken.deleteMany({ where: { token: { in: unregistered } } });
            }
        }
    }
    async sendFcm(token, title, body, data) {
        if (!this.fcmInitialised)
            return;
        try {
            await admin.messaging().send({
                token,
                notification: { title, body },
                data: data ?? {},
                android: { priority: 'high' },
            });
        }
        catch (e) {
            this.logger.error(`FCM failed for token ${token}: ${e?.message ?? e}`);
            if (e?.code === 'messaging/registration-token-not-registered' ||
                e?.code === 'messaging/invalid-registration-token') {
                await this.prisma.pushToken.deleteMany({ where: { token } });
            }
        }
    }
    async registerToken(userId, token, platform) {
        return this.prisma.pushToken.upsert({
            where: { token },
            create: { userId, token, platform },
            update: { userId },
        });
    }
    async removeToken(token) {
        return this.prisma.pushToken.deleteMany({ where: { token } });
    }
    async getForUser(userId) {
        return this.prisma.notification.findMany({
            where: { userId },
            orderBy: { createdAt: 'desc' },
            take: 50,
        });
    }
    async unreadCount(userId) {
        const count = await this.prisma.notification.count({
            where: { userId, isRead: false },
        });
        return { count };
    }
    async markRead(notificationId, userId) {
        return this.prisma.notification.updateMany({
            where: { id: notificationId, userId },
            data: { isRead: true },
        });
    }
};
exports.NotificationsService = NotificationsService;
exports.NotificationsService = NotificationsService = NotificationsService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        config_1.ConfigService])
], NotificationsService);
//# sourceMappingURL=notifications.service.js.map
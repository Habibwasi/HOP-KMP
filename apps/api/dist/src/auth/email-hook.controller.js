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
var EmailHookController_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.EmailHookController = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const standardwebhooks_1 = require("standardwebhooks");
const mail_service_1 = require("../mail/mail.service");
const auth_email_template_1 = require("../mail/templates/auth-email.template");
let EmailHookController = EmailHookController_1 = class EmailHookController {
    mailService;
    config;
    logger = new common_1.Logger(EmailHookController_1.name);
    constructor(mailService, config) {
        this.mailService = mailService;
        this.config = config;
    }
    async handleEmailHook(req) {
        const payload = this.verifyWebhook(req);
        const recipientEmail = payload?.user?.email;
        const emailData = payload?.email_data;
        if (!recipientEmail || !emailData?.email_action_type) {
            throw new common_1.BadRequestException('Invalid hook payload');
        }
        const actionType = emailData.email_action_type;
        const apiBaseUrl = this.config.get('API_BASE_URL', 'https://hop-kmp-production.up.railway.app');
        const html = (0, auth_email_template_1.buildAuthEmail)({
            actionType,
            recipientEmail,
            token: emailData.token,
            tokenHash: emailData.token_hash,
            redirectTo: emailData.redirect_to,
            siteUrl: emailData.site_url,
            apiBaseUrl,
        });
        const subject = (0, auth_email_template_1.buildSubject)(actionType);
        this.logger.log(`Sending [${actionType}] email to ${recipientEmail}`);
        await this.mailService.sendWithFallback({ to: recipientEmail, subject, html });
        return {};
    }
    verifyWebhook(req) {
        const hookSecret = this.config.get('SUPABASE_HOOK_SECRET');
        if (!hookSecret) {
            this.logger.warn('SUPABASE_HOOK_SECRET is not set — hook is unprotected!');
            return req.body;
        }
        const signingSecret = hookSecret.replace(/^v\d+,whsec_/, '');
        const wh = new standardwebhooks_1.Webhook(signingSecret);
        const rawBody = req.rawBody;
        if (!rawBody) {
            throw new common_1.UnauthorizedException('Raw body unavailable');
        }
        try {
            const verified = wh.verify(rawBody.toString(), req.headers);
            return verified;
        }
        catch (err) {
            this.logger.warn(`Hook signature verification failed: ${err.message}`);
            throw new common_1.UnauthorizedException('Invalid webhook signature');
        }
    }
};
exports.EmailHookController = EmailHookController;
__decorate([
    (0, common_1.Post)('email'),
    (0, common_1.HttpCode)(common_1.HttpStatus.OK),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], EmailHookController.prototype, "handleEmailHook", null);
exports.EmailHookController = EmailHookController = EmailHookController_1 = __decorate([
    (0, common_1.Controller)('auth/hook'),
    __metadata("design:paramtypes", [mail_service_1.MailService,
        config_1.ConfigService])
], EmailHookController);
//# sourceMappingURL=email-hook.controller.js.map
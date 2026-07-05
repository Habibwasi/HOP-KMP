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
var MailService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.MailService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const resend_1 = require("resend");
let MailService = MailService_1 = class MailService {
    config;
    logger = new common_1.Logger(MailService_1.name);
    fromAddress;
    fromName;
    resend;
    constructor(config) {
        this.config = config;
        this.fromAddress = config.getOrThrow('MAIL_FROM_ADDRESS');
        this.fromName = config.getOrThrow('MAIL_FROM_NAME');
        this.resend = new resend_1.Resend(config.get('RESEND_API_KEY'));
    }
    async sendWithFallback(opts) {
        const failures = [];
        const providers = [
            { name: 'Brevo', fn: () => this.sendViaBrevo(opts) },
            { name: 'Resend', fn: () => this.sendViaResend(opts) },
            { name: 'MailerSend', fn: () => this.sendViaMailerSend(opts) },
        ];
        for (const provider of providers) {
            try {
                await provider.fn();
                this.logger.log(`Email sent via ${provider.name} → ${opts.to}`);
                return;
            }
            catch (err) {
                const message = err instanceof Error ? err.message : String(err);
                this.logger.warn(`${provider.name} failed: ${message}`);
                failures.push({ provider: provider.name, error: message });
            }
        }
        throw new Error(`All email providers failed: ${JSON.stringify(failures)}`);
    }
    async sendViaBrevo({ to, subject, html }) {
        const apiKey = this.config.get('BREVO_API_KEY');
        if (!apiKey)
            throw new Error('BREVO_API_KEY not configured');
        const res = await fetch('https://api.brevo.com/v3/smtp/email', {
            method: 'POST',
            headers: {
                'api-key': apiKey,
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
            body: JSON.stringify({
                sender: { name: this.fromName, email: this.fromAddress },
                to: [{ email: to }],
                subject,
                htmlContent: html,
            }),
        });
        if (!res.ok) {
            const body = await res.text();
            throw new Error(`Brevo HTTP ${res.status}: ${body}`);
        }
    }
    async sendViaResend({ to, subject, html }) {
        const apiKey = this.config.get('RESEND_API_KEY');
        if (!apiKey)
            throw new Error('RESEND_API_KEY not configured');
        const { error } = await this.resend.emails.send({
            from: `${this.fromName} <${this.fromAddress}>`,
            to,
            subject,
            html,
        });
        if (error)
            throw new Error(`Resend error: ${error.message}`);
    }
    async sendViaMailerSend({ to, subject, html }) {
        const apiKey = this.config.get('MAILERSEND_API_KEY');
        if (!apiKey)
            throw new Error('MAILERSEND_API_KEY not configured');
        const res = await fetch('https://api.mailersend.com/v1/email', {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${apiKey}`,
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
            body: JSON.stringify({
                from: { email: this.fromAddress, name: this.fromName },
                to: [{ email: to }],
                subject,
                html,
            }),
        });
        if (!res.ok) {
            const body = await res.text();
            throw new Error(`MailerSend HTTP ${res.status}: ${body}`);
        }
    }
};
exports.MailService = MailService;
exports.MailService = MailService = MailService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService])
], MailService);
//# sourceMappingURL=mail.service.js.map
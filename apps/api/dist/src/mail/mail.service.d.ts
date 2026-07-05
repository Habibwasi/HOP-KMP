import { ConfigService } from '@nestjs/config';
interface SendOptions {
    to: string;
    subject: string;
    html: string;
}
export declare class MailService {
    private readonly config;
    private readonly logger;
    private readonly fromAddress;
    private readonly fromName;
    private readonly resend;
    constructor(config: ConfigService);
    sendWithFallback(opts: SendOptions): Promise<void>;
    private sendViaBrevo;
    private sendViaResend;
    private sendViaMailerSend;
}
export {};

import { ConfigService } from '@nestjs/config';
import { MailService } from '../mail/mail.service';
export declare class EmailHookController {
    private readonly mailService;
    private readonly config;
    private readonly logger;
    constructor(mailService: MailService, config: ConfigService);
    handleEmailHook(req: any): Promise<Record<string, never>>;
    private verifyWebhook;
}

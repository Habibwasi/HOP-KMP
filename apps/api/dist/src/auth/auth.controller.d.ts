import { ConfigService } from '@nestjs/config';
import type { Response } from 'express';
export declare class AuthController {
    private readonly config;
    constructor(config: ConfigService);
    confirmEmail(tokenHash: string, type: string, res: Response): void;
    confirmEmailPost(tokenHash: string, type: string, res: Response): Promise<void>;
}

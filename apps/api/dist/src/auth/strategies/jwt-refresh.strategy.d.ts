import { Strategy, StrategyOptionsWithRequest } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';
import { Request } from 'express';
declare const JwtRefreshStrategy_base: new (...args: [opt: StrategyOptionsWithRequest] | [opt: import("passport-jwt").StrategyOptionsWithoutRequest]) => Strategy & {
    validate(...args: any[]): unknown;
};
export declare class JwtRefreshStrategy extends JwtRefreshStrategy_base {
    private config;
    constructor(config: ConfigService);
    validate(req: Request, payload: {
        sub: string;
    }): Promise<{
        refreshToken: any;
        sub: string;
    }>;
}
export {};

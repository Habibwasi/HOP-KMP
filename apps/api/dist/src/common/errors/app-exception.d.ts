import { HttpException } from '@nestjs/common';
import { ApiErrorCode } from './api-error-codes';
export type ValidationDetail = {
    field: string;
    message: string;
};
export declare class AppException extends HttpException {
    readonly errorCode: ApiErrorCode;
    constructor(errorCode: ApiErrorCode, overrideMessage?: string, details?: ValidationDetail[]);
}

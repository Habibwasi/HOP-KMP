import { OtpPurpose } from '@prisma/client';
export declare class VerifyOtpDto {
    phone: string;
    code: string;
    purpose: OtpPurpose;
}

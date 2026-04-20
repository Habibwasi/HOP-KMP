import { OtpPurpose } from '@prisma/client';
export declare class SendOtpDto {
    phone: string;
    purpose: OtpPurpose;
}

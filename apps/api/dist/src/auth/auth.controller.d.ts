import { AuthService } from './auth.service';
import { SendOtpDto } from './dto/send-otp.dto';
import { VerifyOtpDto } from './dto/verify-otp.dto';
import { RegisterDto } from './dto/register.dto';
export declare class AuthController {
    private auth;
    constructor(auth: AuthService);
    sendOtp(dto: SendOtpDto): Promise<{
        message: string;
    }>;
    verifyOtp(dto: VerifyOtpDto): Promise<{
        verified: boolean;
    }>;
    register(dto: RegisterDto): Promise<{
        accessToken: string;
        refreshToken: string;
        user: any;
    }>;
    loginWithOtp(dto: VerifyOtpDto): Promise<{
        accessToken: string;
        refreshToken: string;
        user: any;
    }>;
    refresh(req: any): Promise<{
        accessToken: string;
        refreshToken: string;
    }>;
    logout(refreshToken: string): Promise<{
        message: string;
    }>;
}

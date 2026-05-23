import { PrismaService } from '../prisma/prisma.service';
export declare class BanExpiryService {
    private readonly prisma;
    private readonly logger;
    constructor(prisma: PrismaService);
    liftExpiredBans(): Promise<void>;
}

import { PrismaService } from '../prisma/prisma.service';
import { CreateRatingDto } from './dto/create-rating.dto';
export declare class RatingsService {
    private prisma;
    constructor(prisma: PrismaService);
    create(raterId: string, dto: CreateRatingDto): Promise<{
        id: string;
        score: number;
        comment: string | null;
        createdAt: Date;
        raterId: string;
        rateeId: string;
    }>;
    getUserRatings(userId: string): Promise<{
        userId: string;
        averageScore: number | null;
        totalRatings: number;
        ratings: ({
            rater: {
                id: string;
                firstName: string;
                lastName: string;
                avatarUrl: string | null;
            };
        } & {
            id: string;
            score: number;
            comment: string | null;
            createdAt: Date;
            raterId: string;
            rateeId: string;
        })[];
    }>;
}

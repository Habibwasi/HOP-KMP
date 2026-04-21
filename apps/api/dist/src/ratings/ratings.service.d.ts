import { PrismaService } from '../prisma/prisma.service';
import { CreateRatingDto } from './dto/create-rating.dto';
export declare class RatingsService {
    private prisma;
    constructor(prisma: PrismaService);
    create(raterId: string, dto: CreateRatingDto): Promise<{
        id: string;
        createdAt: Date;
        raterId: string;
        rateeId: string;
        score: number;
        comment: string | null;
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
            createdAt: Date;
            raterId: string;
            rateeId: string;
            score: number;
            comment: string | null;
        })[];
    }>;
}

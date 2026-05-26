package com.example.hop.data.repository.dev

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.HopNotification
import com.example.hop.domain.model.NotificationType
import com.example.hop.domain.model.RoleRated
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse

class DevUserRepository : UserRepository {

    override suspend fun getCurrentUserProfile(): ApiResponse<User> =
        ApiResponse.Success(DevAuthRepository.DEV_USER)

    override suspend fun getUserProfile(userId: String): ApiResponse<User> =
        ApiResponse.Success(
            DevAuthRepository.DEV_USER.copy(id = userId, fullName = "Other User")
        )

    override suspend fun getUserReviews(userId: String): ApiResponse<List<UserReview>> =
        ApiResponse.Success(
            listOf(
                UserReview("r1", "Mikkel Sørensen", 5, "Great ride, very punctual!", RoleRated.DRIVER),
                UserReview("r2", "Anna Nielsen", 4, "Friendly and clean car.", RoleRated.DRIVER),
                UserReview("r3", "Sofia Hansen", 5, "Perfect passenger, easy chat.", RoleRated.PASSENGER),
            )
        )

    override suspend fun getUserCarDetails(userId: String): ApiResponse<CarDetails?> =
        ApiResponse.Success(
            CarDetails(
                make = "Tesla",
                model = "Model 3",
                year = 2024,
                licensePlate = "AB 12 345",
                colour = "White",
                seatsAvailable = 3,
            )
        )

    override suspend fun updateFullName(name: String): ApiResponse<User> =
        ApiResponse.Success(DevAuthRepository.DEV_USER.copy(fullName = name))

    override suspend fun updateMobilepayNumber(number: String): ApiResponse<User> =
        ApiResponse.Success(DevAuthRepository.DEV_USER.copy(mobilepayNumber = number))

    override suspend fun reportUser(userId: String, reason: String): ApiResponse<Unit> =
        ApiResponse.Success(Unit)

    override suspend fun savePushToken(token: String, platform: String): ApiResponse<Unit> =
        ApiResponse.Success(Unit)

    override suspend fun getNotifications(): ApiResponse<List<HopNotification>> =
        ApiResponse.Success(
            listOf(
                HopNotification(
                    id = "n1",
                    type = NotificationType.BOOKING_CONFIRMED,
                    title = "Booking confirmed",
                    body = "Your seat on Copenhagen → Aarhus is confirmed.",
                    createdAt = "2026-04-20T09:00:00Z",
                    isRead = false,
                    deepLinkId = "dev-booking-1",
                ),
                HopNotification(
                    id = "n2",
                    type = NotificationType.NEW_RATING,
                    title = "New rating",
                    body = "You received a 5-star rating from Emma Jensen!",
                    createdAt = "2026-04-19T15:30:00Z",
                    isRead = true,
                ),
            )
        )

    override suspend fun markNotificationRead(notificationId: String): ApiResponse<Unit> =
        ApiResponse.Success(Unit)

    override suspend fun uploadAvatar(imageData: ByteArray, contentType: String): ApiResponse<User> =
        ApiResponse.Success(DevAuthRepository.DEV_USER)
}

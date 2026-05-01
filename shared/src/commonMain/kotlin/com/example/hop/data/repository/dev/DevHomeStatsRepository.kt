package com.example.hop.data.repository.dev

import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.UserStats
import com.example.hop.domain.repository.HomeStatsRepository
import com.example.hop.network.ApiResponse

class DevHomeStatsRepository : HomeStatsRepository {

    override suspend fun getMyStats(): ApiResponse<UserStats> =
        ApiResponse.Success(
            UserStats(
                averageRating = 4.8,
                totalRatings = 12,
                completedTrips = 7,
            )
        )

    override suspend fun getUnreadNotificationsCount(): ApiResponse<Int> =
        ApiResponse.Success(0)

    override suspend fun getActiveBooking(): ApiResponse<ActiveBooking?> =
        ApiResponse.Success(null)
}

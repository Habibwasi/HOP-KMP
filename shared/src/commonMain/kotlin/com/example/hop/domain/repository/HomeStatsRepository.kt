package com.example.hop.domain.repository

import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.UserStats
import com.example.hop.network.ApiResponse

interface HomeStatsRepository {
    /** GET /users/me/stats */
    suspend fun getMyStats(): ApiResponse<UserStats>

    /** GET /notifications/unread-count → returns the integer count. */
    suspend fun getUnreadNotificationsCount(): ApiResponse<Int>

    /** GET /bookings/me/active → next upcoming confirmed booking, or null. */
    suspend fun getActiveBooking(): ApiResponse<ActiveBooking?>
}

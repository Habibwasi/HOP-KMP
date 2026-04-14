package com.example.hop.domain.repository

import com.example.hop.domain.model.Booking
import com.example.hop.network.ApiResponse

interface BookingRepository {
    suspend fun createBooking(tripId: String, seats: Int): ApiResponse<Booking>
    suspend fun getBooking(id: String): ApiResponse<Booking>
    suspend fun cancelBooking(id: String): ApiResponse<Unit>
    suspend fun rateBooking(bookingId: String, stars: Int, comment: String?): ApiResponse<Unit>
}

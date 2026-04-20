package com.example.hop.data.repository.dev

import com.example.hop.domain.model.Booking
import com.example.hop.domain.model.BookingStatus
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.network.ApiResponse

class DevBookingRepository : BookingRepository {

    private var bookingCounter = 0

    override suspend fun createBooking(tripId: String, seats: Int): ApiResponse<Booking> {
        val booking = Booking(
            id = "dev-booking-${++bookingCounter}",
            tripId = tripId,
            passengerId = DevAuthRepository.DEV_USER.id,
            seats = seats,
            status = BookingStatus.CONFIRMED,
            paymentId = "dev-pay-$bookingCounter",
        )
        return ApiResponse.Success(booking)
    }

    override suspend fun getBooking(id: String): ApiResponse<Booking> = ApiResponse.Success(
        Booking(
            id = id,
            tripId = "dev-trip-1",
            passengerId = DevAuthRepository.DEV_USER.id,
            seats = 1,
            status = BookingStatus.CONFIRMED,
            paymentId = "dev-pay-1",
        )
    )

    override suspend fun cancelBooking(id: String): ApiResponse<Unit> = ApiResponse.Success(Unit)

    override suspend fun rateBooking(bookingId: String, stars: Int, comment: String?): ApiResponse<Unit> =
        ApiResponse.Success(Unit)
}

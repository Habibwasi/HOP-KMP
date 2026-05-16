package com.example.hop.data.repository.dev

import com.example.hop.domain.model.RideSettlement
import com.example.hop.domain.repository.SettlementRepository
import com.example.hop.network.ApiResponse
import kotlinx.datetime.Clock

class DevSettlementRepository : SettlementRepository {

    private val settlements = mutableMapOf<String, RideSettlement>()

    private fun now() = Clock.System.now().toString()

    override suspend fun getSettlement(bookingId: String): ApiResponse<RideSettlement> {
        val s = settlements[bookingId] ?: return ApiResponse.Error(404, "Settlement not found")
        return ApiResponse.Success(s)
    }

    override suspend fun markPaid(bookingId: String): ApiResponse<RideSettlement> {
        val s = settlements[bookingId]
            ?: RideSettlement(bookingId, 15000, "12345678", null, null, null, null, now())
        val updated = s.copy(passengerPaidAt = now())
        settlements[bookingId] = updated
        return ApiResponse.Success(updated)
    }

    override suspend fun confirmReceived(bookingId: String): ApiResponse<RideSettlement> {
        val s = settlements[bookingId] ?: return ApiResponse.Error(404, "Settlement not found")
        val updated = s.copy(driverConfirmedAt = now())
        settlements[bookingId] = updated
        return ApiResponse.Success(updated)
    }

    override suspend fun dispute(bookingId: String, reason: String): ApiResponse<RideSettlement> {
        val s = settlements[bookingId] ?: return ApiResponse.Error(404, "Settlement not found")
        val updated = s.copy(disputedAt = now(), disputeReason = reason)
        settlements[bookingId] = updated
        return ApiResponse.Success(updated)
    }
}

package com.example.hop.domain.repository

import com.example.hop.domain.model.RideSettlement
import com.example.hop.domain.model.TripSettlementEntry
import com.example.hop.network.ApiResponse

interface SettlementRepository {
    suspend fun getSettlement(bookingId: String): ApiResponse<RideSettlement>
    suspend fun getSettlementsForTrip(tripId: String): ApiResponse<List<TripSettlementEntry>>
    suspend fun markPaid(bookingId: String): ApiResponse<RideSettlement>
    suspend fun unmarkPaid(bookingId: String): ApiResponse<RideSettlement>
    suspend fun confirmReceived(bookingId: String): ApiResponse<RideSettlement>
    suspend fun dispute(bookingId: String, reason: String): ApiResponse<RideSettlement>
}

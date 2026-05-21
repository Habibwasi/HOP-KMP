package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.domain.model.RideSettlement
import com.example.hop.domain.model.TripSettlementEntry
import com.example.hop.domain.repository.SettlementRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
private data class DisputeRequest(val reason: String)

class SettlementRepositoryImpl(
    private val httpClient: HttpClient,
) : SettlementRepository {

    override suspend fun getSettlement(bookingId: String): ApiResponse<RideSettlement> =
        safeEnvelopeCall { httpClient.get("settlements/$bookingId").body() }

    override suspend fun getSettlementsForTrip(tripId: String): ApiResponse<List<TripSettlementEntry>> =
        safeEnvelopeCall { httpClient.get("settlements/trip/$tripId").body() }

    override suspend fun markPaid(bookingId: String): ApiResponse<RideSettlement> =
        safeEnvelopeCall { httpClient.post("settlements/$bookingId/mark-paid").body() }

    override suspend fun unmarkPaid(bookingId: String): ApiResponse<RideSettlement> =
        safeEnvelopeCall { httpClient.post("settlements/$bookingId/unmark-paid").body() }

    override suspend fun confirmReceived(bookingId: String): ApiResponse<RideSettlement> =
        safeEnvelopeCall { httpClient.post("settlements/$bookingId/confirm-received").body() }

    override suspend fun dispute(bookingId: String, reason: String): ApiResponse<RideSettlement> =
        safeEnvelopeCall {
            httpClient.post("settlements/$bookingId/dispute") {
                setBody(DisputeRequest(reason))
            }.body()
        }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        if (envelope.data != null) ApiResponse.Success(envelope.data)
        else ApiResponse.Error(envelope.error?.code ?: -1, envelope.error?.message ?: "Unknown error")
    } catch (e: HttpRequestTimeoutException) {
        ApiResponse.Error(ApiResponse.CODE_TIMEOUT, "Connection timed out. Please check your network and try again.")
    } catch (e: ServerResponseException) {
        ApiResponse.Error(e.response.status.value, "Server error (${e.response.status.value}). Please try again later.")
    } catch (e: ClientRequestException) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Request error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}

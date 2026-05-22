package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.LicenceStatusDto
import com.example.hop.data.dto.toDomain
import com.example.hop.data.dto.toSubmitLicenceRequestDto
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class DriverRepositoryImpl(
    private val httpClient: HttpClient,
) : DriverRepository {

    override suspend fun submitCarDetails(carDetails: CarDetails): ApiResponse<Unit> =
        safeApiCall {
            httpClient.post("users/me/car-details") {
                setBody(carDetails.toSubmitLicenceRequestDto())
            }
            Unit
        }

    override suspend fun getLicenceStatus(): ApiResponse<LicenceStatus> {
        val response = safeEnvelopeCall<LicenceStatusDto> {
            httpClient.get("driver/licence/status").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> {
        return try {
            val envelope = block()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}

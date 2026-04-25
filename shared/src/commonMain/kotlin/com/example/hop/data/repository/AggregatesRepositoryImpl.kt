package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.Co2SavedDto
import com.example.hop.data.dto.DemandHotspotsDto
import com.example.hop.data.dto.EarningsSeriesDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.DemandHotspot
import com.example.hop.domain.model.EarningsPoint
import com.example.hop.domain.repository.AggregatesRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get

class AggregatesRepositoryImpl(
    private val httpClient: HttpClient,
) : AggregatesRepository {

    override suspend fun getDriverEarningsSeries(days: Int): ApiResponse<List<EarningsPoint>> {
        val response = safeEnvelopeCall<EarningsSeriesDto> {
            httpClient.get("aggregates/driver/earnings-series?days=$days").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.series.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getDemandHotspots(): ApiResponse<List<DemandHotspot>> {
        val response = safeEnvelopeCall<DemandHotspotsDto> {
            httpClient.get("aggregates/demand-hotspots").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.hotspots.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getUserCo2Saved(): ApiResponse<Int> {
        val response = safeEnvelopeCall<Co2SavedDto> {
            httpClient.get("aggregates/co2-saved").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.co2SavedKg)
            is ApiResponse.Error -> response
        }
    }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        val error = envelope.error
        if (error != null) ApiResponse.Error(error.code, error.message)
        else ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
    } catch (e: ClientRequestException) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}

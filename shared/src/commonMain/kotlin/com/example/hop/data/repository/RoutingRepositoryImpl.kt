package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.domain.repository.RouteInfo
import com.example.hop.domain.repository.RoutingRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

// ── Repository implementation ─────────────────────────────────────────────────

/**
 * Calls Hop API route calculation so Google Maps credentials stay server-side
 * and route pricing behaves the same on Android and iOS.
 */
class RoutingRepositoryImpl(
    private val httpClient: HttpClient,
) : RoutingRepository {

    override suspend fun getDistanceMetres(
        origin: String,
        destination: String,
    ): ApiResponse<Int> = when (val result = getRouteInfo(origin, destination)) {
        is ApiResponse.Success -> ApiResponse.Success(result.data.distanceMetres)
        is ApiResponse.Error -> result
    }

    override suspend fun getRouteInfo(
        origin: String,
        destination: String,
    ): ApiResponse<RouteInfo> {
        val response = safeEnvelopeCall<RouteInfoDto> {
            httpClient.get("places/route") {
                parameter("origin", origin)
                parameter("dest", destination)
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        val error = envelope.error
        if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
        ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
    } catch (e: HttpRequestTimeoutException) {
        ApiResponse.Error(-1, "Request timed out. Please try again.")
    } catch (e: ServerResponseException) {
        ApiResponse.Error(e.response.status.value, "Server error. Please try again.")
    } catch (e: ClientRequestException) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}

@Serializable
private data class RouteInfoDto(
    val distanceMetres: Int,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
)

private fun RouteInfoDto.toDomain(): RouteInfo = RouteInfo(
    distanceMetres = distanceMetres,
    originLat = originLat,
    originLng = originLng,
    destLat = destLat,
    destLng = destLng,
)

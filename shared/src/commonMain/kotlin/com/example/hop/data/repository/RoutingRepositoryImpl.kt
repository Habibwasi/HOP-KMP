package com.example.hop.data.repository

import com.example.hop.domain.repository.RouteInfo
import com.example.hop.domain.repository.RoutingRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── Google Directions API DTOs ────────────────────────────────────────────────

@Serializable
private data class DirectionsResponse(
    val routes: List<DirectionsRoute> = emptyList(),
)

@Serializable
private data class DirectionsRoute(
    val legs: List<DirectionsLeg> = emptyList(),
)

@Serializable
private data class DirectionsLeg(
    val distance: DirectionsDistance,
    @SerialName("start_location") val startLocation: DirectionsLatLng,
    @SerialName("end_location") val endLocation: DirectionsLatLng,
)

@Serializable
private data class DirectionsLatLng(
    val lat: Double,
    val lng: Double,
)

@Serializable
private data class DirectionsDistance(
    val value: Int,     // metres
    val text: String,   // e.g. "45.2 km"
)

// ── Repository implementation ─────────────────────────────────────────────────

/**
 * Calls the Google Maps Directions REST API to obtain the real driving distance
 * between two free-text addresses.
 *
 * A dedicated lightweight [HttpClient] is created internally so that the app's
 * auth interceptor (which adds the Supabase JWT) is NOT sent to Google's servers.
 */
class RoutingRepositoryImpl(
    private val mapsApiKey: String,
) : RoutingRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun getDistanceMetres(
        origin: String,
        destination: String,
    ): ApiResponse<Int> = safeApiCall {
        val response: DirectionsResponse = client.get(
            "https://maps.googleapis.com/maps/api/directions/json"
        ) {
            url {
                parameters.append("origin", origin)
                parameters.append("destination", destination)
                parameters.append("mode", "driving")
                parameters.append("key", mapsApiKey)
            }
        }.body()

        val metres = response.routes.firstOrNull()?.legs?.firstOrNull()?.distance?.value
            ?: throw IllegalStateException("No driving route found between the selected addresses")
        metres
    }

    override suspend fun getRouteInfo(
        origin: String,
        destination: String,
    ): ApiResponse<RouteInfo> = safeApiCall {
        val response: DirectionsResponse = client.get(
            "https://maps.googleapis.com/maps/api/directions/json"
        ) {
            url {
                parameters.append("origin", origin)
                parameters.append("destination", destination)
                parameters.append("mode", "driving")
                parameters.append("key", mapsApiKey)
            }
        }.body()

        val leg = response.routes.firstOrNull()?.legs?.firstOrNull()
            ?: throw IllegalStateException("No driving route found between the selected addresses")
        RouteInfo(
            distanceMetres = leg.distance.value,
            originLat = leg.startLocation.lat,
            originLng = leg.startLocation.lng,
            destLat = leg.endLocation.lat,
            destLng = leg.endLocation.lng,
        )
    }
}

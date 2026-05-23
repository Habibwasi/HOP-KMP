package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.GeocodeResultDto
import com.example.hop.data.dto.SavedPlaceDto
import com.example.hop.data.dto.UpsertPlaceRequest
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.domain.repository.GeocodeResult
import com.example.hop.domain.repository.PlacesRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class PlacesRepositoryImpl(
    private val httpClient: HttpClient,
) : PlacesRepository {

    override suspend fun list(): ApiResponse<List<SavedPlace>> {
        val response = safeEnvelopeCall<List<SavedPlaceDto>> {
            httpClient.get("places").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun upsert(
        label: String,
        address: String,
        lat: Double?,
        lng: Double?,
        kind: SavedPlaceKind?,
    ): ApiResponse<SavedPlace> {
        val response = safeEnvelopeCall<SavedPlaceDto> {
            httpClient.post("places") {
                setBody(
                    UpsertPlaceRequest(
                        label = label,
                        address = address,
                        lat = lat,
                        lng = lng,
                        kind = kind?.name,
                    )
                )
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun delete(id: String): ApiResponse<Unit> = safeApiCall {
        httpClient.delete("places/$id")
        Unit
    }

    override suspend fun geocode(address: String): ApiResponse<GeocodeResult> {
        val response = safeEnvelopeCall<GeocodeResultDto> {
            httpClient.get("places/geocode") {
                url.parameters.append("address", address)
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
        if (error != null) ApiResponse.Error(error.code, error.message, error.errorCode)
        else ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
    } catch (e: HttpRequestTimeoutException) {
        ApiResponse.Error(ApiResponse.CODE_TIMEOUT, "Connection timed out. Please check your network and try again.")
    } catch (e: ServerResponseException) {
        ApiResponse.Error(e.response.status.value, "Server error (${e.response.status.value}). Please try again later.")
    } catch (e: ClientRequestException) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}

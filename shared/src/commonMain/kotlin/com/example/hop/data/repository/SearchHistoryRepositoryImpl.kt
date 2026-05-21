package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.RecentSearchDto
import com.example.hop.data.dto.RecordSearchRequest
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.RecentSearch
import com.example.hop.domain.repository.SearchHistoryRepository
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

class SearchHistoryRepositoryImpl(
    private val httpClient: HttpClient,
) : SearchHistoryRepository {

    override suspend fun list(limit: Int): ApiResponse<List<RecentSearch>> {
        val response = safeEnvelopeCall<List<RecentSearchDto>> {
            httpClient.get("search/recent") {
                url.parameters.append("limit", limit.toString())
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun record(originLabel: String, destLabel: String): ApiResponse<Unit> =
        safeApiCall {
            httpClient.post("search/recent") {
                setBody(RecordSearchRequest(originLabel = originLabel, destLabel = destLabel))
            }
            Unit
        }

    override suspend fun delete(id: String): ApiResponse<Unit> = safeApiCall {
        httpClient.delete("search/recent/$id")
        Unit
    }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        val error = envelope.error
        if (error != null) ApiResponse.Error(error.code, error.message)
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

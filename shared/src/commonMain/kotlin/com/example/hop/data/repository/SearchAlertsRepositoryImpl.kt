package com.example.hop.data.repository

import com.example.hop.data.dto.CreateSearchAlertRequest
import com.example.hop.data.dto.SearchAlertDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.SearchAlert
import com.example.hop.domain.repository.SearchAlertsRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SearchAlertsRepositoryImpl(
    private val httpClient: HttpClient,
) : SearchAlertsRepository {

    override suspend fun create(origin: String, dest: String, seats: Int): ApiResponse<SearchAlert> =
        safeApiCall {
            val dto: SearchAlertDto = httpClient.post("search-alerts") {
                setBody(CreateSearchAlertRequest(origin = origin, dest = dest, seats = seats))
            }.body()
            dto.toDomain()
        }

    override suspend fun list(): ApiResponse<List<SearchAlert>> = safeApiCall {
        val items: List<SearchAlertDto> = httpClient.get("search-alerts").body()
        items.map { it.toDomain() }
    }

    override suspend fun delete(id: String): ApiResponse<Unit> = safeApiCall {
        httpClient.delete("search-alerts/$id")
        Unit
    }
}

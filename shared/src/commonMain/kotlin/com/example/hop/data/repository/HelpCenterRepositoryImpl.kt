package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.FaqItemDto
import com.example.hop.data.dto.FaqListDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.FaqItem
import com.example.hop.domain.repository.HelpCenterRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get

class HelpCenterRepositoryImpl(
    private val httpClient: HttpClient,
) : HelpCenterRepository {

    override suspend fun getFaqs(): ApiResponse<List<FaqItem>> {
        return try {
            val envelope: ApiEnvelope<FaqListDto> =
                httpClient.get("help-center/faqs").body()
            val error = envelope.error
            if (error != null) {
                ApiResponse.Error(error.code, error.message, error.errorCode)
            } else {
                val items = envelope.data?.faqs?.map { it.toDomain() } ?: emptyList()
                ApiResponse.Success(items)
            }
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}

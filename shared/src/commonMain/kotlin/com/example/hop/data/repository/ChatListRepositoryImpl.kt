package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.domain.model.ChatThread
import com.example.hop.domain.repository.ChatListRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ChatListRepositoryImpl(
    private val httpClient: HttpClient,
) : ChatListRepository {

    override suspend fun getMyChats(): ApiResponse<List<ChatThread>> = try {
        val envelope = httpClient.get("bookings/my-chats").body<ApiEnvelope<List<ChatThread>>>()
        ApiResponse.Success(envelope.data ?: emptyList())
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}

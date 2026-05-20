package com.example.hop.domain.repository

import com.example.hop.domain.model.ChatThread
import com.example.hop.network.ApiResponse

interface ChatListRepository {
    suspend fun getMyChats(): ApiResponse<List<ChatThread>>
}

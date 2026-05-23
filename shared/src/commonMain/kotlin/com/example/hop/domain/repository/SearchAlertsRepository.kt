package com.example.hop.domain.repository

import com.example.hop.domain.model.SearchAlert
import com.example.hop.network.ApiResponse

interface SearchAlertsRepository {
    suspend fun create(origin: String, dest: String, seats: Int): ApiResponse<SearchAlert>
    suspend fun list(): ApiResponse<List<SearchAlert>>
    suspend fun delete(id: String): ApiResponse<Unit>
}

package com.example.hop.domain.repository

import com.example.hop.domain.model.RecentSearch
import com.example.hop.network.ApiResponse

interface SearchHistoryRepository {
    /** GET /search/recent?limit= */
    suspend fun list(limit: Int = 5): ApiResponse<List<RecentSearch>>

    /** POST /search/recent — idempotent upsert; bumps useCount + lastUsedAt. */
    suspend fun record(originLabel: String, destLabel: String): ApiResponse<Unit>

    /** DELETE /search/recent/:id */
    suspend fun delete(id: String): ApiResponse<Unit>
}

package com.example.hop.data.repository.dev

import com.example.hop.domain.model.RecentSearch
import com.example.hop.domain.repository.SearchHistoryRepository
import com.example.hop.network.ApiResponse

class DevSearchHistoryRepository : SearchHistoryRepository {

    private val entries = mutableListOf(
        RecentSearch(id = "sh-1", originLabel = "Copenhagen Central", destLabel = "Aarhus H", useCount = 3),
        RecentSearch(id = "sh-2", originLabel = "Odense Station", destLabel = "Aalborg Station", useCount = 1),
    )

    override suspend fun list(limit: Int): ApiResponse<List<RecentSearch>> =
        ApiResponse.Success(entries.take(limit))

    override suspend fun record(originLabel: String, destLabel: String): ApiResponse<Unit> {
        val existing = entries.indexOfFirst { it.originLabel == originLabel && it.destLabel == destLabel }
        if (existing >= 0) {
            entries[existing] = entries[existing].copy(useCount = entries[existing].useCount + 1)
        } else {
            entries.add(0, RecentSearch(id = "sh-${entries.size + 1}", originLabel = originLabel, destLabel = destLabel, useCount = 1))
        }
        return ApiResponse.Success(Unit)
    }

    override suspend fun delete(id: String): ApiResponse<Unit> {
        entries.removeAll { it.id == id }
        return ApiResponse.Success(Unit)
    }
}

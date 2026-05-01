package com.example.hop.data.repository.dev

import com.example.hop.domain.model.SearchAlert
import com.example.hop.domain.repository.SearchAlertsRepository
import com.example.hop.network.ApiResponse

class DevSearchAlertsRepository : SearchAlertsRepository {

    private val alerts = mutableListOf<SearchAlert>()

    override suspend fun create(origin: String, dest: String, seats: Int): ApiResponse<SearchAlert> {
        val alert = SearchAlert(
            id = "dev-alert-${alerts.size + 1}",
            origin = origin,
            dest = dest,
            seats = seats,
            createdAt = "2026-05-01T00:00:00Z",
        )
        alerts.add(alert)
        return ApiResponse.Success(alert)
    }

    override suspend fun list(): ApiResponse<List<SearchAlert>> =
        ApiResponse.Success(alerts.toList())

    override suspend fun delete(id: String): ApiResponse<Unit> {
        alerts.removeAll { it.id == id }
        return ApiResponse.Success(Unit)
    }
}

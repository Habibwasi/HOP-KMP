package com.example.hop.data.repository.dev

import com.example.hop.domain.model.DemandHotspot
import com.example.hop.domain.model.EarningsPoint
import com.example.hop.domain.repository.AggregatesRepository
import com.example.hop.network.ApiResponse

class DevAggregatesRepository : AggregatesRepository {

    override suspend fun getDriverEarningsSeries(days: Int): ApiResponse<List<EarningsPoint>> =
        ApiResponse.Success(
            listOf(
                EarningsPoint(date = "2025-01-13", earningsOere = 12000),
                EarningsPoint(date = "2025-01-14", earningsOere = 8500),
                EarningsPoint(date = "2025-01-15", earningsOere = 15000),
                EarningsPoint(date = "2025-01-16", earningsOere = 6000),
                EarningsPoint(date = "2025-01-17", earningsOere = 18000),
                EarningsPoint(date = "2025-01-18", earningsOere = 11000),
                EarningsPoint(date = "2025-01-19", earningsOere = 9500),
            ).takeLast(days)
        )

    override suspend fun getDemandHotspots(): ApiResponse<List<DemandHotspot>> =
        ApiResponse.Success(
            listOf(
                DemandHotspot(areaName = "Nørrebro", demandCount = 34, lat = 55.6897, lng = 12.5490),
                DemandHotspot(areaName = "Frederiksberg", demandCount = 21, lat = 55.6796, lng = 12.5342),
                DemandHotspot(areaName = "Ørestad", demandCount = 15, lat = 55.6291, lng = 12.5768),
            )
        )

    override suspend fun getUserCo2Saved(): ApiResponse<Int> =
        ApiResponse.Success(42)
}

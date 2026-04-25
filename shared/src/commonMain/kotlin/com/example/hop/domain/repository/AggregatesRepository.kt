package com.example.hop.domain.repository

import com.example.hop.domain.model.DemandHotspot
import com.example.hop.domain.model.EarningsPoint
import com.example.hop.network.ApiResponse

interface AggregatesRepository {
    /** GET /aggregates/driver/earnings-series?days= */
    suspend fun getDriverEarningsSeries(days: Int = 7): ApiResponse<List<EarningsPoint>>

    /** GET /aggregates/demand-hotspots */
    suspend fun getDemandHotspots(): ApiResponse<List<DemandHotspot>>

    /** GET /aggregates/co2-saved → kg integer */
    suspend fun getUserCo2Saved(): ApiResponse<Int>
}

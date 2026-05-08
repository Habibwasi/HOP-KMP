package com.example.hop.data.repository.dev

import com.example.hop.domain.repository.RouteInfo
import com.example.hop.domain.repository.RoutingRepository
import com.example.hop.network.ApiResponse

/**
 * Dev stub — returns a fixed 45 km driving distance so the price preview
 * works offline in [com.example.hop.BuildConfig.DEV_MODE].
 */
class DevRoutingRepository : RoutingRepository {
    override suspend fun getDistanceMetres(
        origin: String,
        destination: String,
    ): ApiResponse<Int> = ApiResponse.Success(45_000)

    override suspend fun getRouteInfo(
        origin: String,
        destination: String,
    ): ApiResponse<RouteInfo> = ApiResponse.Success(
        RouteInfo(
            distanceMetres = 45_000,
            originLat = 55.6761,
            originLng = 12.5683,
            destLat = 56.1629,
            destLng = 10.2039,
        )
    )
}

package com.example.hop.domain.repository

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.network.ApiResponse

interface DriverRepository {
    suspend fun submitCarDetails(carDetails: CarDetails): ApiResponse<Unit>
    suspend fun getLicenceStatus(): ApiResponse<LicenceStatus>
}

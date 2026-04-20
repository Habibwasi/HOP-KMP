package com.example.hop.data.repository.dev

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.network.ApiResponse

class DevDriverRepository : DriverRepository {

    override suspend fun submitLicence(carDetails: CarDetails, photoUrl: String): ApiResponse<Unit> =
        ApiResponse.Success(Unit)

    override suspend fun getLicenceStatus(): ApiResponse<LicenceStatus> =
        ApiResponse.Success(LicenceStatus.APPROVED)
}

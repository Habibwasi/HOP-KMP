package com.example.hop.domain.repository

import com.example.hop.domain.model.FaqItem
import com.example.hop.network.ApiResponse

interface HelpCenterRepository {
    suspend fun getFaqs(): ApiResponse<List<FaqItem>>
}

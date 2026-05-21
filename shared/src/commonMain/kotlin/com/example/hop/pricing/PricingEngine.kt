package com.example.hop.pricing

import kotlin.math.roundToInt

object PricingEngine {
    private const val SKAT_RATE_OERE_PER_METRE = 0.228 // DKK 2.28/km

    data class PriceResult(
        val totalTripCostOere: Int,
        val pricePerSeatOere: Int,
    )

    fun calculate(distanceMetres: Int, seatsTotal: Int): PriceResult {
        require(seatsTotal in 1..4)
        require(distanceMetres > 0)
        // Use roundToInt() instead of toInt() (truncation) to match the server-side
        // Math.round() used in pricing.service.ts. Avoids a 1-øre display/store mismatch
        // caused by floating-point representation of 0.228.
        val totalTripCostOere = (distanceMetres * SKAT_RATE_OERE_PER_METRE).roundToInt()
        val pricePerSeatOere = maxOf(totalTripCostOere / seatsTotal, 100)
        return PriceResult(totalTripCostOere, pricePerSeatOere)
    }
}

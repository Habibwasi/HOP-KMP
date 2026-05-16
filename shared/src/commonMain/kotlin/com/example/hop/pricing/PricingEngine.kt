package com.example.hop.pricing

object PricingEngine {
    private const val SKAT_RATE_OERE_PER_METRE = 0.228 // DKK 2.28/km

    data class PriceResult(
        val totalTripCostOere: Int,
        val pricePerSeatOere: Int,
    )

    fun calculate(distanceMetres: Int, seatsTotal: Int): PriceResult {
        require(seatsTotal in 1..4)
        require(distanceMetres > 0)
        val totalTripCostOere = (distanceMetres * SKAT_RATE_OERE_PER_METRE).toInt()
        val pricePerSeatOere = maxOf(totalTripCostOere / seatsTotal, 100)
        return PriceResult(totalTripCostOere, pricePerSeatOere)
    }
}

package com.example.hop.pricing

object PricingEngine {
    private const val SKAT_RATE_OERE_PER_METRE = 0.228 // DKK 2.28/km
    private const val PLATFORM_FEE_RATE = 0.15

    data class PriceResult(
        val totalTripCostOere: Int,
        val driverNetPerSeatOere: Int,
        val passengerPaysPerSeatOere: Int,
        val platformFeeOere: Int
    )

    fun calculate(distanceMetres: Int, seatsTotal: Int): PriceResult {
        require(seatsTotal in 1..4)
        require(distanceMetres > 0)
        val totalTripCostOere = (distanceMetres * SKAT_RATE_OERE_PER_METRE).toInt()
        val driverNetPerSeatOere = totalTripCostOere / seatsTotal
        val passengerPaysPerSeatOere = (driverNetPerSeatOere / (1 - PLATFORM_FEE_RATE)).toInt()
        val platformFeeOere = passengerPaysPerSeatOere - driverNetPerSeatOere
        return PriceResult(totalTripCostOere, driverNetPerSeatOere, passengerPaysPerSeatOere, platformFeeOere)
    }
}

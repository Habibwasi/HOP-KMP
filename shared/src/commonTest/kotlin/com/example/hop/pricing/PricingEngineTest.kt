package com.example.hop.pricing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PricingEngineTest {

    // Aarhus → Copenhagen reference route from Agent.md
    private val AARHUS_CPH_METRES = 304_000
    private val SEATS = 4

    @Test
    fun aarhusCopenhagen_totalTripCost() {
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(69_312, result.totalTripCostOere)
    }

    @Test
    fun aarhusCopenhagen_driverNetPerSeat() {
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(17_328, result.driverNetPerSeatOere)
    }

    @Test
    fun aarhusCopenhagen_passengerPaysPerSeat() {
        // (17328 / 0.85).toInt() == 20385
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(20_385, result.passengerPaysPerSeatOere)
    }

    @Test
    fun aarhusCopenhagen_platformFee() {
        // 20385 - 17328 == 3057
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(3_057, result.platformFeeOere)
    }

    @Test
    fun aarhusCopenhagen_allFieldsInSingleCall() {
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(
            PricingEngine.PriceResult(
                totalTripCostOere = 69_312,
                driverNetPerSeatOere = 17_328,
                passengerPaysPerSeatOere = 20_385,
                platformFeeOere = 3_057
            ),
            result
        )
    }

    @Test
    fun seatsOutOfRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            PricingEngine.calculate(AARHUS_CPH_METRES, seatsTotal = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            PricingEngine.calculate(AARHUS_CPH_METRES, seatsTotal = 5)
        }
    }

    @Test
    fun zeroDistance_throws() {
        assertFailsWith<IllegalArgumentException> {
            PricingEngine.calculate(distanceMetres = 0, seatsTotal = 1)
        }
    }
}

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
    fun aarhusCopenhagen_pricePerSeat() {
        // floor(304000 * 0.228) = 69312, / 4 seats = 17328
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(17_328, result.pricePerSeatOere)
    }

    @Test
    fun aarhusCopenhagen_allFieldsInSingleCall() {
        val result = PricingEngine.calculate(AARHUS_CPH_METRES, SEATS)
        assertEquals(
            PricingEngine.PriceResult(
                totalTripCostOere = 69_312,
                pricePerSeatOere = 17_328,
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

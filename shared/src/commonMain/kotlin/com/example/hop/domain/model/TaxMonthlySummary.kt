package com.example.hop.domain.model

/**
 * Monthly tax summary returned by `GET /tax/summary/:year/:month`.
 * All monetary amounts are in øre (1 DKK = 100 øre).
 */
data class TaxMonthlySummary(
    val year: Int,
    val month: Int,
    /** Gross earnings from all completed trips in this month. */
    val grossOere: Int,
    /** Befordringsfradrag deduction (SKAT 2026: DKK 2.28/km). */
    val befordringsfradragOere: Int,
    /** Gross minus befordringsfradrag. */
    val taxableOere: Int,
    /** Platform-estimated income tax on taxable amount. */
    val estimatedTaxOere: Int,
)

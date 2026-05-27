package com.example.hop.ui.util

/**
 * Formats an øre amount as a DKK string using the Danish comma convention.
 *
 * Examples:
 *   24000 → "DKK 240"
 *   24099 → "DKK 240,99"
 *    5050 → "DKK 50,50"
 */
fun formatDkk(oere: Int): String {
    val kr = oere / 100
    val rem = oere % 100
    return if (rem == 0) "DKK $kr" else "DKK $kr,${rem.toString().padStart(2, '0')}"
}

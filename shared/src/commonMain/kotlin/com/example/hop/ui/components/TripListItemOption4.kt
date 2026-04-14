package com.example.hop.ui.components

import androidx.compose.foundation.alpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * ─ OPTION 4 UI PATTERN ─
 *
 * Trip list item that respects the isBroken flag from TripUiModel.
 *
 * • Broken trips render greyed out (alpha = 0.6f).
 * • All tap targets disabled when isBroken = true.
 * • Status label shows "—" instead of UNKNOWN.
 * • No filtering: broken trips stay visible for user awareness.
 *
 * Adapt colors/styling to match your design system.
 */

@Composable
fun TripListItemOption4(
    tripUi: TripUiModel,
    onTapTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnabled = !tripUi.isBroken
    val alpha = if (isEnabled) 1f else 0.6f

    Surface(
        modifier = modifier
            .alpha(alpha)
            .clickable(enabled = isEnabled) {
                onTapTrip(tripUi.id)
            }
            .semantics {
                // Mark as disabled in accessibility tree if broken
                if (!isEnabled) {
                    // Accessibility: communicate that this item is unavailable
                }
            },
        color = HopColors.surfaceElevated,
    ) {
        Column(
            modifier = Modifier.padding(HopSpacing.md),
        ) {
            // ─ Status badge ───────────────────────────────────────────────
            Text(
                text = when {
                    isEnabled -> statusDisplayName(tripUi.status)
                    else -> "—" // Generic placeholder for broken state
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isEnabled) {
                    statusColor(tripUi.status)
                } else {
                    HopColors.textSecondary
                },
            )

            // ─ Route display ──────────────────────────────────────────────
            Text(
                text = "${tripUi.originName} → ${tripUi.destName}",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.textPrimary.copy(alpha = alpha),
            )

            // ─ Time and seats ─────────────────────────────────────────────
            Text(
                text = "${tripUi.departsAt} · ${tripUi.seatsBooked}/${tripUi.seatsTotal} seats",
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.textSecondary.copy(alpha = alpha),
            )

            // ─ Price (in DKK) ─────────────────────────────────────────────
            Text(
                text = "DKK ${tripUi.priceOerePerSeat / 100}",
                style = MaterialTheme.typography.labelMedium,
                color = HopColors.success.copy(alpha = alpha),
            )
        }
    }
}

/**
 * Helper: Display-friendly status label.
 */
private fun statusDisplayName(status: com.example.hop.domain.model.TripStatus): String =
    when (status) {
        TripStatus.ACTIVE -> "Active"
        TripStatus.CONFIRMED -> "Confirmed"
        TripStatus.CANCELLED -> "Cancelled"
        TripStatus.COMPLETED -> "Completed"
        TripStatus.UNKNOWN -> "—" // Fallback (should not appear if isBroken check works)
    }

/**
 * Helper: Status → display color.
 */
private fun statusColor(status: com.example.hop.domain.model.TripStatus) =
    when (status) {
        TripStatus.ACTIVE -> HopColors.primaryLime
        TripStatus.CONFIRMED -> HopColors.success
        TripStatus.CANCELLED -> HopColors.error
        TripStatus.COMPLETED -> HopColors.textSecondary
        TripStatus.UNKNOWN -> HopColors.textSecondary // Grey for unknown
    }

/**
 * ─ List Rendering Pattern ─────────────────────────────────────────────────────
 *
 * In your screen composable:
 *
 *   LazyColumn {
 *       items(
 *           items = state.trips,
 *           key = { it.id }
 *       ) { tripUi ->
 *           TripListItemOption4(
 *               tripUi = tripUi,
 *               onTapTrip = onTapTrip,
 *               modifier = Modifier.fillMaxWidth()
 *           )
 *       }
 *   }
 *
 * Do NOT filter trips before rendering:
 *   ❌ WRONG: state.trips.filter { !it.isBroken }
 *   ✅ RIGHT: state.trips (render all, UI handles disabled state)
 *
 * Rationale:
 * • User maintains visibility into all their bookings.
 * • Pull-to-refresh can self-heal broken state.
 * • Backend analytics track observations (soft-logged in ViewModel).
 */

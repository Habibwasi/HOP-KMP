package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Live countdown banner for the soonest upcoming confirmed booking.
 *
 * Tick budget: only re-renders the seconds digit while [departureIso] is
 * within 24 h. Beyond that, the banner is hidden (returns Unit early) so
 * there is no per-second recomposition cost on the home screen.
 *
 * @param departureIso ISO-8601 UTC timestamp of departure ("2026-04-25T18:30:00Z").
 *                     Pass `null` to hide.
 * @param origin       Origin label for the route line.
 * @param destination  Destination label for the route line.
 * @param onClick      Tap → open the active-trip detail screen.
 */
@Composable
fun ActiveBookingBanner(
    departureIso: String?,
    origin: String,
    destination: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val departure = departureIso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return

    // Guard: don't render if departure is more than 24 h away or already past 6 h ago.
    val initialDelta = departure - Clock.System.now()
    val initialSeconds = initialDelta.inWholeSeconds
    if (initialSeconds > 24 * 3600 || initialSeconds < -6 * 3600) return

    var remainingSeconds by remember(departureIso) { mutableStateOf(initialSeconds) }
    LaunchedEffect(departureIso) {
        while (true) {
            delay(1000L)
            remainingSeconds = (departure - Clock.System.now()).inWholeSeconds
        }
    }

    val isPast = remainingSeconds < 0
    val absSeconds = if (isPast) -remainingSeconds else remainingSeconds
    val countdown = formatCountdown(absSeconds)

    val gradient = Brush.horizontalGradient(
        listOf(HopColors.primaryGreen, HopColors.referralEnd),
    )
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (isPast) {
                    "Trip in progress. Tap to open."
                } else {
                    "Next trip departs in $countdown. Tap to open."
                }
            }
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPast) "Trip in progress" else "Departs in $countdown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$origin → $destination",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                    ),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Format seconds into "Hh MMm SSs", "MMm SSs", or "SSs" — depending on magnitude. */
private fun formatCountdown(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return when {
        h > 0 -> "${h}h ${m.pad2()}m ${s.pad2()}s"
        m > 0 -> "${m}m ${s.pad2()}s"
        else  -> "${s}s"
    }
}

private fun Long.pad2(): String = if (this < 10) "0$this" else "$this"

package com.example.hop.ui.components.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
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

    // Pulsing dot visible only when trip is in progress
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotAlpha",
    )

    val cardShape = RoundedCornerShape(24.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = cardShape, ambientColor = Color(0x14000000))
            .clip(cardShape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (isPast) {
                    "Trip in progress. Tap to open."
                } else {
                    "Next trip departs in $countdown. Tap to open."
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lime→green accent stripe on the left edge
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(72.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(HopColors.primaryLime, HopColors.primaryGreen)
                    )
                ),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HopColors.primaryLime.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = HopColors.primaryGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                if (isPast) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HopColors.primaryGreen.copy(alpha = dotAlpha)),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trip in progress",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HopColors.authTextPrimary,
                            ),
                        )
                    }
                } else {
                    Text(
                        text = "Departs in $countdown",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HopColors.authTextPrimary,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$origin → $destination",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = HopColors.authTextSecondary,
                    ),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = HopColors.authTextSecondary.copy(alpha = 0.6f),
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

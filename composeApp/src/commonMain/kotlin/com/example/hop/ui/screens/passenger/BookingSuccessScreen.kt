package com.example.hop.ui.screens.passenger

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
import com.example.hop.presentation.bookingsuccess.BookingSuccessEvent
import com.example.hop.presentation.bookingsuccess.BookingSuccessUiState
import com.example.hop.presentation.bookingsuccess.BookingSuccessViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-06 — Booking Success Route.
 *
 * Obtains [BookingSuccessViewModel] from Koin, triggers the initial load keyed on
 * [bookingId], collects state, and delegates rendering to the stateless
 * [BookingSuccessScreen].
 */
@Composable
fun BookingSuccessRoute(
    bookingId: String,
    onNavigateToMyTrips: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingSuccessViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(bookingId) {
        viewModel.onEvent(BookingSuccessEvent.Load(bookingId))
    }

    BookingSuccessScreen(
        state = state,
        onViewMyTrips = onNavigateToMyTrips,
        onBackToHome = onNavigateToHome,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-06 — Booking Success Screen.
 *
 * Stateless renderer. Renders:
 *  - Animated checkmark: circle scales 0 → 1.1 → 1.0 while the check stroke
 *    draws in from 0 to 100 % over 500 ms.
 *  - Model A: "Booking Confirmed!" headline in [HopColors.success] green.
 *  - Model B: "Booking Pending" headline in [HopColors.warning] amber, plus a
 *    descriptive subtitle.
 *  - Trip summary card: route (origin → destination), departure date, driver name.
 *  - "View My Trips" primary button.
 *  - "Back to Home" ghost button.
 */
@Composable
fun BookingSuccessScreen(
    state: BookingSuccessUiState,
    onViewMyTrips: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xxl))

            // ── Animated checkmark ────────────────────────────────────────

            if (state.isLoading) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = HopColors.success,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp,
                    )
                }
            } else {
                val accentColor = if (state.tripModel == TripModel.B) HopColors.warning else HopColors.success
                AnimatedCheckmark(
                    accentColor = accentColor,
                    modifier = Modifier
                        .size(96.dp)
                        .semantics { contentDescription = "Booking status icon" },
                )
            }

            Spacer(modifier = Modifier.height(HopSpacing.lg))

            // ── Headline ──────────────────────────────────────────────────

            if (!state.isLoading) {
                val headlineColor = if (state.tripModel == TripModel.B) HopColors.warning else HopColors.success
                val headlineText = if (state.tripModel == TripModel.B) "Booking Pending" else "Booking Confirmed!"

                Text(
                    text = headlineText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = headlineColor,
                    textAlign = TextAlign.Center,
                )

                if (state.tripModel == TripModel.B) {
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    Text(
                        text = "Your seat is reserved. Payment will be charged when the trip is confirmed.",
                        fontSize = 15.sp,
                        color = HopColors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // ── Trip summary card ─────────────────────────────────────────

            if (!state.isLoading && state.originName.isNotEmpty()) {
                TripSummaryCard(
                    originName = state.originName,
                    destName = state.destName,
                    departsAt = state.departsAt,
                    driverName = state.driverName,
                )
            }

            if (!state.isLoading && !state.error.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(HopSpacing.md))
                Text(
                    text = "Could not load trip details.",
                    fontSize = 14.sp,
                    color = HopColors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(HopSpacing.xl))
        }

        // ── Action buttons ────────────────────────────────────────────────

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md)
                .padding(bottom = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            HopButton(
                text = "View My Trips",
                onClick = onViewMyTrips,
                modifier = Modifier.fillMaxWidth(),
            )
            HopButton(
                text = "Back to Home",
                onClick = onBackToHome,
                variant = HopButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Animated Checkmark ────────────────────────────────────────────────────────

/**
 * Draws a circle that scales 0 → 1.1 → 1.0 while simultaneously drawing the
 * check stroke from 0 → 100 %. Total animation budget: ~500 ms.
 */
@Composable
private fun AnimatedCheckmark(
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(0f) }
    val strokeProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 150),
            )
        }
        launch {
            strokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(
        modifier = modifier.scale(scale.value),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCheckmarkScene(
                progress = strokeProgress.value,
                accentColor = accentColor,
            )
        }
    }
}

private fun DrawScope.drawCheckmarkScene(progress: Float, accentColor: Color) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Background circle (slightly dimmed version of the accent colour)
    drawCircle(
        color = accentColor.copy(alpha = 0.15f),
        radius = radius,
        center = center,
    )

    // Outline ring
    drawCircle(
        color = accentColor,
        radius = radius - 3.dp.toPx(),
        center = center,
        style = Stroke(width = 3.dp.toPx()),
    )

    if (progress > 0f) {
        // Checkmark path: start at (left, mid-high), through (mid, high), to (right, low)
        val strokeWidth = 4.dp.toPx()
        val cx = center.x
        val cy = center.y
        val r = radius * 0.45f

        val p1 = Offset(cx - r * 0.85f, cy)
        val p2 = Offset(cx - r * 0.1f, cy + r * 0.65f)
        val p3 = Offset(cx + r * 0.85f, cy - r * 0.55f)

        // Split into two segments: p1→p2 (first half) and p2→p3 (second half)
        val firstFraction = 0.5f

        val path = Path()

        when {
            progress <= firstFraction -> {
                // Drawing first segment p1→p2
                val t = progress / firstFraction
                val midX = p1.x + (p2.x - p1.x) * t
                val midY = p1.y + (p2.y - p1.y) * t
                path.moveTo(p1.x, p1.y)
                path.lineTo(midX, midY)
            }
            else -> {
                // First segment complete, drawing second segment p2→p3
                val t = (progress - firstFraction) / firstFraction
                val endX = p2.x + (p3.x - p2.x) * t
                val endY = p2.y + (p3.y - p2.y) * t
                path.moveTo(p1.x, p1.y)
                path.lineTo(p2.x, p2.y)
                path.lineTo(endX, endY)
            }
        }

        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

// ── Trip Summary Card ─────────────────────────────────────────────────────────

@Composable
private fun TripSummaryCard(
    originName: String,
    destName: String,
    departsAt: String,
    driverName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.surfaceElevated)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
    ) {
        // Route
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SummaryLabel(label = "From")
                SummaryValue(value = originName)
            }
            Text(
                text = "→",
                color = HopColors.textSecondary,
                fontSize = 18.sp,
            )
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                SummaryLabel(label = "To")
                SummaryValue(value = destName)
            }
        }

        HorizontalDivider(color = HopColors.surface, thickness = 1.dp)

        // Date
        SummaryRow(label = "Departs", value = departsAt)

        HorizontalDivider(color = HopColors.surface, thickness = 1.dp)

        // Driver
        SummaryRow(label = "Driver", value = driverName)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryLabel(label = label)
        SummaryValue(value = value)
    }
}

@Composable
private fun SummaryLabel(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        color = HopColors.textSecondary,
    )
}

@Composable
private fun SummaryValue(value: String) {
    Text(
        text = value,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = HopColors.textPrimary,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Model A — Confirmed")
@Composable
private fun PreviewBookingSuccessModelA() {
    HopTheme {
        BookingSuccessScreen(
            state = BookingSuccessUiState(
                isLoading = false,
                tripModel = TripModel.A,
                originName = "Aarhus",
                destName = "Copenhagen",
                departsAt = "Mon 22 Apr · 08:30",
                driverName = "Lars Eriksen",
            ),
            onViewMyTrips = {},
            onBackToHome = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Model B — Pending")
@Composable
private fun PreviewBookingSuccessModelB() {
    HopTheme {
        BookingSuccessScreen(
            state = BookingSuccessUiState(
                isLoading = false,
                tripModel = TripModel.B,
                originName = "Aarhus",
                destName = "Copenhagen",
                departsAt = "Sat 27 Apr · 14:00",
                driverName = "Mette Hansen",
            ),
            onViewMyTrips = {},
            onBackToHome = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Loading")
@Composable
private fun PreviewBookingSuccessLoading() {
    HopTheme {
        BookingSuccessScreen(
            state = BookingSuccessUiState(isLoading = true),
            onViewMyTrips = {},
            onBackToHome = {},
        )
    }
}

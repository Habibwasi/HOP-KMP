package com.example.hop.ui.screens.passenger

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.hop.presentation.cancellationconfirmation.CancellationConfirmationEvent
import com.example.hop.presentation.cancellationconfirmation.CancellationConfirmationUiState
import com.example.hop.presentation.cancellationconfirmation.CancellationConfirmationViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-10 — Cancellation Confirmation Route.
 *
 * Obtains [CancellationConfirmationViewModel] from Koin, triggers the load on
 * entry keyed to [bookingId], and delegates rendering to the stateless
 * [CancellationConfirmationScreen].
 */
@Composable
fun CancellationConfirmationRoute(
    bookingId: String,
    onNavigateToMyTrips: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CancellationConfirmationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(bookingId) {
        viewModel.onEvent(CancellationConfirmationEvent.Load(bookingId))
    }

    CancellationConfirmationScreen(
        state = state,
        onBackToMyTrips = onNavigateToMyTrips,
        modifier = modifier,
    )
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * PA-10 — Cancellation Confirmation Screen.
 *
 * Stateless renderer. Shows:
 *  - Canvas-drawn animated red X icon
 *  - "Booking Cancelled" headline
 *  - "Your refund is on its way" with DKK amount
 *  - Model B note: "Payment hold will be released within 1-2 business days"
 *  - "Back to My Trips" primary button (clears PA-08 + PA-10 from back stack)
 */
@Composable
fun CancellationConfirmationScreen(
    state: CancellationConfirmationUiState,
    onBackToMyTrips: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xxl))

            // Animated cancellation icon
            CancellationIcon()

            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // Headline
            Text(
                text = "Booking Cancelled",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
                color = HopColors.error,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(HopSpacing.md))

            // Refund message
            val refundText = state.refundAmountOere?.let { amountOere ->
                val dkk = amountOere / 100
                val ore = amountOere % 100
                val amountStr = if (ore == 0) "DKK $dkk" else "DKK $dkk.${ore.toString().padStart(2, '0')}"
                "Your refund of $amountStr is on its way."
            } ?: "Your refund is on its way."
            Text(
                text = refundText,
                style = MaterialTheme.typography.bodyLarge,
                color = HopColors.textPrimary,
                textAlign = TextAlign.Center,
            )

            // Model B specific note
            if (state.isModelB) {
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HopColors.surfaceElevated)
                        .padding(HopSpacing.md),
                ) {
                    Text(
                        text = "Payment hold will be released within 1-2 business days.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HopColors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(HopSpacing.xxl))
        }

        // Primary action button
        HopButton(
            text = "Back to My Trips",
            onClick = onBackToMyTrips,
            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        )
    }
}

// ── Canvas icon ───────────────────────────────────────────────────────────────

/**
 * Animated red-circle X icon drawn entirely via Canvas.
 *
 * Animation sequence:
 *  1. Circle outline scales from 0 → 1 over 350 ms.
 *  2. The two X strokes draw in from the centre outward over 250 ms (staggered 200 ms).
 */
@Composable
private fun CancellationIcon(modifier: Modifier = Modifier) {
    val circleProgress = remember { Animatable(0f) }
    val strokeProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        circleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        )
        strokeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        )
    }

    val cp by circleProgress.asState()
    val sp by strokeProgress.asState()

    Canvas(
        modifier = modifier
            .size(96.dp)
            .semantics { contentDescription = "Booking cancelled" },
    ) {
        drawCancellationIcon(circleProgress = cp, strokeProgress = sp)
    }
}

private fun DrawScope.drawCancellationIcon(
    circleProgress: Float,
    strokeProgress: Float,
    color: Color = Color(0xFFEF4444),
) {
    val radius = size.minDimension / 2f
    val centre = Offset(size.width / 2f, size.height / 2f)

    // Circle outline
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 360f * circleProgress,
        useCenter = false,
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
    )

    if (strokeProgress > 0f) {
        val inset = radius * 0.3f
        val strokeWidth = 6.dp.toPx()

        // Diagonal "/" stroke (bottom-left → top-right)
        val path1 = Path().apply {
            val startX = centre.x - (inset * strokeProgress)
            val startY = centre.y + (inset * strokeProgress)
            val endX = centre.x + (inset * strokeProgress)
            val endY = centre.y - (inset * strokeProgress)
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        // Diagonal "\" stroke (top-left → bottom-right)
        val path2 = Path().apply {
            val startX = centre.x - (inset * strokeProgress)
            val startY = centre.y - (inset * strokeProgress)
            val endX = centre.x + (inset * strokeProgress)
            val endY = centre.y + (inset * strokeProgress)
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        drawPath(path = path1, color = color, style = strokeStyle)
        drawPath(path = path2, color = color, style = strokeStyle)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CancellationConfirmationScreenModelAPreview() {
    HopTheme {
        CancellationConfirmationScreen(
            state = CancellationConfirmationUiState(
                refundAmountOere = 20386,
                isModelB = false,
            ),
            onBackToMyTrips = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CancellationConfirmationScreenModelBPreview() {
    HopTheme {
        CancellationConfirmationScreen(
            state = CancellationConfirmationUiState(
                refundAmountOere = 40772,
                isModelB = true,
            ),
            onBackToMyTrips = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CancellationConfirmationScreenLoadingPreview() {
    HopTheme {
        CancellationConfirmationScreen(
            state = CancellationConfirmationUiState(isLoading = true),
            onBackToMyTrips = {},
        )
    }
}

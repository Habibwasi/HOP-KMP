package com.example.hop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Empty-state placeholder — shown instead of a blank list.
 *
 * Layout:
 *   [Illustration]       (120 dp — default geometric placeholder; swap via [illustration])
 *   [headline]           (titleMedium, centred)
 *   [subtext]            (bodySmall, secondary, centred — optional)
 *   [HopButton Primary]  (CTA)
 *
 * Usage:
 *   EmptyState(
 *       headline  = "No trips yet",
 *       ctaLabel  = "Search rides",
 *       onCtaClick = { /* navigate */ },
 *   )
 */
@Composable
fun EmptyState(
    headline: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    illustration: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.xl, vertical = HopSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Illustration — custom slot or built-in placeholder
        if (illustration != null) {
            illustration()
        } else {
            DefaultIllustration(
                modifier = Modifier.size(120.dp),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // Headline
        Text(
            text = headline,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.textPrimary,
            textAlign = TextAlign.Center,
        )

        // Optional subtext
        if (subtext != null) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // CTA button — constrained width so it doesn't stretch full screen
        HopButton(
            text = ctaLabel,
            onClick = onCtaClick,
            modifier = Modifier.fillMaxWidth(fraction = 0.75f),
            variant = HopButtonVariant.Primary,
        )
    }
}

/**
 * Default geometric illustration drawn with Canvas — no asset dependencies.
 *
 * Renders a simple composition:
 *   - Large muted circle (background ring)
 *   - Smaller Lime-accent circle
 *   - Two rounded arcs suggesting a route / path
 */
@Composable
private fun DefaultIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Background ring
        drawCircle(
            color = Color(0xFF2A2A2A),
            radius = w * 0.46f,
            center = Offset(cx, cy),
        )

        // Lime accent filled circle (top-left offset)
        drawCircle(
            color = HopColors.primaryLime.copy(alpha = 0.85f),
            radius = w * 0.12f,
            center = Offset(cx - w * 0.20f, cy - h * 0.18f),
        )

        // Dark secondary circle (bottom-right)
        drawCircle(
            color = HopColors.primaryGreen.copy(alpha = 0.60f),
            radius = w * 0.09f,
            center = Offset(cx + w * 0.22f, cy + h * 0.20f),
        )

        // Arc 1 — outer path-line
        drawArc(
            color = HopColors.primaryLime.copy(alpha = 0.30f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.35f, cy - h * 0.35f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        // Arc 2 — inner accent
        drawArc(
            color = HopColors.textSecondary.copy(alpha = 0.25f),
            startAngle = 30f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.25f, cy - h * 0.25f),
            size = Size(w * 0.50f, h * 0.50f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Centre dot
        drawCircle(
            color = Color(0xFF3A3A3A),
            radius = w * 0.07f,
            center = Offset(cx, cy),
        )
    }
}

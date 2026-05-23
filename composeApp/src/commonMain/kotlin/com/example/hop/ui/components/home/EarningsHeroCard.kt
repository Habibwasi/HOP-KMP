package com.example.hop.ui.components.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.components.AnimatedCounter
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing

/**
 * Driver earnings hero card. Shows the running monthly DKK total as an
 * animated counter, the estimated tax line, and a 7-day sparkline of daily
 * earnings (gradient-filled). Pure presentation — caller passes whatever
 * series data is available; an empty series renders a flat baseline.
 *
 * @param monthlyEarningsOere current-month gross earnings in øre.
 * @param estimatedTaxOere estimated current-month tax in øre.
 * @param sparkSeriesOere ordered (oldest → newest) daily totals in øre. Up to ~14 points.
 */
@Composable
fun EarningsHeroCard(
    monthlyEarningsOere: Int,
    estimatedTaxOere: Int,
    sparkSeriesOere: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.primaryLime)
            .clickable(onClick = onClick)
            .padding(HopSpacing.md),
    ) {
        Column {
            Text(
                text = "Earnings this month",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HopColors.authTextPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DKK ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = HopMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = HopColors.authTextPrimary,
                    ),
                )
                AnimatedCounter(
                    targetValue = monthlyEarningsOere,
                    format = { oere -> "${oere / 100},${(oere % 100).toString().padStart(2, '0')}" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = HopMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = HopColors.authTextPrimary,
                        letterSpacing = 0.sp,
                    ),
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
//            Spacer(modifier = Modifier.height(2.dp))
//            Text(
//                text = "Est. tax: DKK ${estimatedTaxOere / 100},${(estimatedTaxOere % 100).toString().padStart(2, '0')}",
//                style = MaterialTheme.typography.bodySmall.copy(
//                    color = HopColors.authTextPrimary.copy(alpha = 0.65f),
//                    fontWeight = FontWeight.Medium,
//                ),
//            )
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            Sparkline(
                series = sparkSeriesOere,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last 7 days",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HopColors.authTextPrimary.copy(alpha = 0.55f),
                ),
            )
        }
    }
}

/** Lightweight gradient-filled line chart used inside [EarningsHeroCard]. */
@Composable
private fun Sparkline(
    series: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = HopColors.authTextPrimary,
    fillColor: Color = HopColors.authTextPrimary.copy(alpha = 0.18f),
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        // Always render a baseline for empty / single-point series so the
        // card never looks "broken".
        val data = when {
            series.isEmpty() -> listOf(0, 0)
            series.size == 1 -> listOf(series.first(), series.first())
            else -> series
        }
        val maxVal = (data.maxOrNull() ?: 0).coerceAtLeast(1)
        val minVal = data.minOrNull() ?: 0
        val range = (maxVal - minVal).coerceAtLeast(1)

        val stepX = width / (data.size - 1).coerceAtLeast(1).toFloat()
        val points = data.mapIndexed { index, value ->
            val x = stepX * index
            val normalised = (value - minVal).toFloat() / range.toFloat()
            val y = height - (normalised * (height - 6f)) - 3f
            Offset(x, y)
        }

        // Filled area under the curve.
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = height,
            ),
        )

        // Line itself.
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round),
        )

        // Subtle dashed baseline at zero.
        if (minVal < 0 || maxVal > 0) {
            drawLine(
                color = lineColor.copy(alpha = 0.12f),
                start = Offset(0f, height - 2f),
                end = Offset(width, height - 2f),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
            )
        }
    }
}

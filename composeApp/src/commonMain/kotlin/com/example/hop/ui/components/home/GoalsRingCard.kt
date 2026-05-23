package com.example.hop.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Driver goals & streak card. Shows a circular progress ring for the
 * "trips toward next bonus" goal alongside a streak indicator.
 *
 * Pure presentation: caller computes [tripsCompleted] / [tripsGoal] from the
 * driver insights endpoint (or zeros while loading).
 */
@Composable
fun GoalsRingCard(
    tripsCompleted: Int,
    tripsGoal: Int,
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    val safeGoal = tripsGoal.coerceAtLeast(1)
    val rawProgress = (tripsCompleted.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 900),
        label = "goalRingProgress",
    )
    val tripsToGo = (safeGoal - tripsCompleted).coerceAtLeast(0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.cardSurface)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(
            progress = animatedProgress,
            label = "$tripsCompleted/$safeGoal",
        )
        Spacer(modifier = Modifier.width(HopSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (tripsToGo == 0) "Bonus unlocked! 🎉" else "$tripsToGo trips to bonus",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Complete $safeGoal trips this week to earn extra.",
                style = MaterialTheme.typography.bodySmall.copy(color = HopColors.authTextSecondary),
            )
            if (streakDays > 0) {
                Spacer(modifier = Modifier.height(HopSpacing.xs))
                StreakChip(days = streakDays)
            }
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 8f
            val inset = strokeWidth / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            // Track
            drawArc(
                color = HopColors.cardBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // Progress
            drawArc(
                color = HopColors.primaryGreen,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun StreakChip(days: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3CD))
            .padding(horizontal = HopSpacing.sm, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🔥 $days-day streak",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF8A5A00),
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

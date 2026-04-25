package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Compact greeting banner shown above the search card on the passenger home.
 * Uses a time-of-day gradient and shows a personalised salutation.
 *
 * Pure presentation — accepts an optional [firstName] and [subtitle];
 * the gradient is selected automatically from the device clock.
 */
@Composable
fun GreetingBanner(
    firstName: String?,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = now.hour
    val (greeting, gradient, contentColor) = when (hour) {
        in 5..11 -> Triple(
            "Good morning",
            Brush.verticalGradient(listOf(HopColors.gradientDayStart, HopColors.gradientDayEnd)),
            HopColors.authTextPrimary,
        )
        in 12..16 -> Triple(
            "Good afternoon",
            Brush.verticalGradient(listOf(HopColors.gradientDayStart, HopColors.gradientDayEnd)),
            HopColors.authTextPrimary,
        )
        in 17..21 -> Triple(
            "Good evening",
            Brush.verticalGradient(listOf(HopColors.gradientDuskStart, HopColors.gradientDuskEnd)),
            HopColors.authTextPrimary,
        )
        else -> Triple(
            "Travelling late?",
            Brush.verticalGradient(listOf(HopColors.gradientNightStart, HopColors.gradientNightEnd)),
            Color.White,
        )
    }
    val name = firstName?.takeIf { it.isNotBlank() }
    val resolvedSubtitle = subtitle ?: defaultSubtitle(hour)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(gradient)
            .padding(
                PaddingValues(
                    start = HopSpacing.md,
                    end = HopSpacing.md,
                    top = HopSpacing.lg,
                    bottom = HopSpacing.lg,
                )
            ),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (name?.firstOrNull()?.uppercase() ?: "H"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = contentColor,
                    )
                }
                Spacer(modifier = Modifier.padding(start = HopSpacing.sm))
                Column {
                    Text(
                        text = if (name != null) "$greeting, $name 👋" else "$greeting 👋",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                    )
                    Text(
                        text = resolvedSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.78f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(HopSpacing.sm))
        }
    }
}

private fun defaultSubtitle(hour: Int): String = when (hour) {
    in 5..9 -> "Where are you headed today?"
    in 10..15 -> "Find a ride for the rest of your day."
    in 16..20 -> "Heading home? See who's going your way."
    else -> "Late-night ride? We'll find you one."
}

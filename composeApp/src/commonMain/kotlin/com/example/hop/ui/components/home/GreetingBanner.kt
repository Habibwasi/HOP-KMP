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
 * Transparent — the parent [PassengerHomeScreen] supplies the lime→background
 * gradient behind it, so no own background is applied here.
 *
 * Pure presentation — accepts an optional [firstName] and [subtitle];
 * the greeting is selected automatically from the device clock.
 */
@Composable
fun GreetingBanner(
    firstName: String?,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Travelling late?"
    }
    val name = firstName?.takeIf { it.isNotBlank() }
    val resolvedSubtitle = subtitle ?: defaultSubtitle(hour)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = HopSpacing.md,
                end = HopSpacing.md,
                top = HopSpacing.lg,
                bottom = HopSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (name?.firstOrNull()?.uppercase() ?: "H"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HopColors.authTextPrimary,
            )
        }
        Spacer(modifier = Modifier.padding(start = HopSpacing.sm))
        Column {
            Text(
                text = if (name != null) "$greeting, $name 👋" else "$greeting 👋",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
            )
            Text(
                text = resolvedSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextPrimary.copy(alpha = 0.6f),
            )
        }
    }
}

private fun defaultSubtitle(hour: Int): String = when (hour) {
    in 5..9 -> "Where are you headed today?"
    in 10..15 -> "Find a ride for the rest of your day."
    in 16..20 -> "Heading home? See who's going your way."
    else -> "Late-night ride? We'll find you one."
}

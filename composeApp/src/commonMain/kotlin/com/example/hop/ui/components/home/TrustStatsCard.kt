package com.example.hop.ui.components.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.components.AnimatedCounter
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Three-stat trust card: rating, completed trips, CO₂ saved.
 * Pure presentation; safe to render with zeroes while real data loads.
 */
@Composable
fun TrustStatsCard(
    ratingTimes10: Int,        // e.g. 48 = 4.8★ — kept as Int for stable animation
    completedTrips: Int,
    co2SavedKg: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.cardSurface)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = HopSpacing.md, horizontal = HopSpacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatCell(
            icon = Icons.Filled.Star,
            iconTint = HopColors.warning,
            value = ratingTimes10,
            label = "Rating",
            format = { v -> if (v <= 0) "—" else "${v / 10}.${v % 10}" },
        )
        StatDivider()
        StatCell(
            icon = Icons.Filled.DirectionsCar,
            iconTint = HopColors.primaryGreen,
            value = completedTrips,
            label = "Trips",
        )
        StatDivider()
        StatCell(
            icon = Icons.Filled.Eco,
            iconTint = HopColors.co2Accent,
            value = co2SavedKg,
            label = "kg CO₂ saved",
        )
    }
}

@Composable
private fun StatCell(
    icon: ImageVector,
    iconTint: Color,
    value: Int,
    label: String,
    format: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedCounter(
            targetValue = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Bold,
            ),
            format = format,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = HopColors.authTextSecondary),
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(HopColors.cardBorder),
    )
}

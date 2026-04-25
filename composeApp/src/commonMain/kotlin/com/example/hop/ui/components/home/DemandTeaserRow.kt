package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
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

enum class DemandLevel { LOW, MEDIUM, HIGH }

data class DemandHotspot(
    val areaName: String,
    val tagline: String,
    val level: DemandLevel,
)

/**
 * Driver "demand near you" teaser. Tapping a hotspot lets the caller prefill
 * the post-trip flow with the suggested origin.
 */
@Composable
fun DemandTeaserRow(
    hotspots: List<DemandHotspot>,
    onHotspotClick: (DemandHotspot) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(hotspots, key = { it.areaName }) { hotspot ->
            HotspotCard(hotspot = hotspot, onClick = { onHotspotClick(hotspot) })
        }
    }
}

@Composable
private fun HotspotCard(
    hotspot: DemandHotspot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (accent, label) = when (hotspot.level) {
        DemandLevel.HIGH -> Color(0xFFEF4444) to "High demand"
        DemandLevel.MEDIUM -> Color(0xFFF59E0B) to "Medium demand"
        DemandLevel.LOW -> Color(0xFF6B7280) to "Low demand"
    }
    val gradient = Brush.linearGradient(
        listOf(accent.copy(alpha = 0.12f), Color.White)
    )
    Box(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(HopSpacing.md),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            Text(
                text = hotspot.areaName,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hotspot.tagline,
                style = MaterialTheme.typography.bodySmall.copy(color = HopColors.authTextSecondary),
            )
        }
    }
}

/** Curated default until the demand API ships. */
val DefaultDemandHotspots: List<DemandHotspot> = listOf(
    DemandHotspot("Aarhus C", "12 searches in the last hour", DemandLevel.HIGH),
    DemandHotspot("Copenhagen Airport", "Friday surge expected", DemandLevel.HIGH),
    DemandHotspot("Aalborg University", "Weekday commute lane", DemandLevel.MEDIUM),
    DemandHotspot("Odense", "Steady weekend interest", DemandLevel.LOW),
)

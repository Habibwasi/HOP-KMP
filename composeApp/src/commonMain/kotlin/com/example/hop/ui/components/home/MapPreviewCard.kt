package com.example.hop.ui.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Decorative card that visually evokes a map preview without depending on a
 * real maps SDK. The Canvas draws abstract "road" curves and a few pickup
 * markers; the bottom row shows an "Open map" affordance.
 *
 * TODO map-integration: replace with an actual map widget once Google Maps
 * (Android) and MapKit (iOS) are wired through expect/actual. The current
 * placeholder keeps the home screen visually rich until that ships.
 */
@Composable
fun MapPreviewCard(
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        listOf(HopColors.gradientDayStart, HopColors.gradientDayEnd),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenMap),
    ) {
        // Decorative road curves + pickup markers.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Main road
            val road1 = Path().apply {
                moveTo(0f, h * 0.7f)
                quadraticTo(w * 0.3f, h * 0.4f, w * 0.55f, h * 0.55f)
                quadraticTo(w * 0.8f, h * 0.7f, w, h * 0.35f)
            }
            drawPath(
                path = road1,
                color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = 6f),
            )

            // Secondary road
            val road2 = Path().apply {
                moveTo(w * 0.1f, 0f)
                quadraticTo(w * 0.4f, h * 0.3f, w * 0.6f, h * 0.25f)
                quadraticTo(w * 0.85f, h * 0.2f, w * 0.95f, h * 0.1f)
            }
            drawPath(
                path = road2,
                color = Color.White.copy(alpha = 0.45f),
                style = Stroke(width = 4f),
            )

            // Pickup markers
            drawCircle(
                color = HopColors.primaryGreen,
                radius = 8f,
                center = Offset(w * 0.25f, h * 0.55f),
            )
            drawCircle(
                color = HopColors.error,
                radius = 8f,
                center = Offset(w * 0.75f, h * 0.45f),
            )
            drawCircle(
                color = Color(0xFF1A1A1A),
                radius = 5f,
                center = Offset(w * 0.55f, h * 0.55f),
            )
        }

        // Bottom CTA row
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(HopSpacing.sm))
                Text(
                    text = "Open map",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF1A1A1A),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

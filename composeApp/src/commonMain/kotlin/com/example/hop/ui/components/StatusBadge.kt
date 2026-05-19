package com.example.hop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors

/**
 * Visual status badge — height = 22 dp, cornerRadius = 6 dp, uppercase label.
 *
 * Pass a [TripStatusBadge] to render booking/trip lifecycle states, or a
 * [TripModelBadge] to render the trip model identifier.
 *
 *  Confirmed  → green  (#1DB954 bg, dark text)
 *  Pending    → amber  (#FBBF24 bg, dark text)
 *  Cancelled  → red    (#EF4444 bg, white text)
 *  Completed  → grey   (#B3B3B3 bg, dark text)
 *  Model A    → lime   (#C8F135 bg, dark text)
 *  Model B    → surface elevated bg, light text
 */
sealed class BadgeType {
    data object Confirmed  : BadgeType()
    data object Pending    : BadgeType()
    data object Cancelled  : BadgeType()
    data object Completed  : BadgeType()
    data object ModelA     : BadgeType()
    data object ModelB     : BadgeType()
    /** Arbitrary custom badge with explicit colors */
    data class Custom(val label: String, val background: Color, val contentColor: Color) : BadgeType()
}

private data class BadgeColors(val background: Color, val contentColor: Color, val label: String)

private fun BadgeType.resolve(): BadgeColors = when (this) {
    BadgeType.Confirmed -> BadgeColors(HopColors.primaryGreen,       Color(0xFF1A1A1A), "Confirmed")
    BadgeType.Pending   -> BadgeColors(HopColors.warning,            Color(0xFF1A1A1A), "Pending")
    BadgeType.Cancelled -> BadgeColors(HopColors.error,              Color.White,       "Cancelled")
    BadgeType.Completed -> BadgeColors(HopColors.textSecondary,      Color(0xFF1A1A1A), "Completed")
    BadgeType.ModelA    -> BadgeColors(HopColors.primaryLime,        Color(0xFF1A1A1A), "Model A")
    BadgeType.ModelB    -> BadgeColors(HopColors.surfaceElevated,    HopColors.textPrimary, "Model B")
    is BadgeType.Custom -> BadgeColors(this.background, this.contentColor, this.label)
}

@Composable
fun StatusBadge(
    type: BadgeType,
    modifier: Modifier = Modifier,
) {
    val colors = type.resolve()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(22.dp)
            .wrapContentWidth()
            .background(
                color = colors.background,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = colors.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
            ),
            color = colors.contentColor,
        )
    }
}

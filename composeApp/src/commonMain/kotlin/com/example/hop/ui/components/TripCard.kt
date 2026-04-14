package com.example.hop.ui.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Trip list card.
 *
 * Layout (top → bottom):
 *  ┌─────────────────────────────────────────────┐
 *  │  [Avatar]  Driver name      ★ 4.8  [ModelA] │
 *  │  ─────────────────────────────────────────  │
 *  │  ● Origin name                              │
 *  │  │                                          │
 *  │  ● Destination name                         │
 *  │  ─────────────────────────────────────────  │
 *  │  Departs 08:30                 DKK 204      │
 *  └─────────────────────────────────────────────┘
 *
 * White card, cornerRadius = 12 dp, elevation shadow.
 * Price is supplied in øre and converted to DKK here (÷ 100).
 */
@Composable
fun TripCard(
    driverName: String,
    driverInitials: String,
    driverRating: Float,
    originName: String,
    destinationName: String,
    departureTime: String,
    tripModel: BadgeType,
    pricePerSeatOere: Int,
    modifier: Modifier = Modifier,
    driverAvatar: Painter? = null,
    isVerified: Boolean = false,
    onClick: () -> Unit = {},
) {
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = cardShape, ambientColor = Color(0x1A000000))
            .clip(cardShape)
            .background(Color.White)        // White card per design spec
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            // ── Row 1: Avatar + Driver name + Rating + Model badge ────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HopAvatar(
                    initials = driverInitials,
                    painter = driverAvatar,
                    size = AvatarSize.Sm,
                    isVerified = isVerified,
                )

                Spacer(modifier = Modifier.width(HopSpacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRating(
                            rating = driverRating,
                            starSize = 12.dp,
                            gap = 1.dp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(driverRating),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF666666),
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(HopSpacing.sm))

                StatusBadge(type = tripModel)
            }

            Spacer(modifier = Modifier.height(HopSpacing.sm))
            CardDivider()
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Row 2: Route ─────────────────────────────────────────────────
            RouteColumn(origin = originName, destination = destinationName)

            Spacer(modifier = Modifier.height(HopSpacing.sm))
            CardDivider()
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Row 3: Departure time + Price ─────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Departs $departureTime",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF666666),
                    ),
                )

                Text(
                    text = "DKK ${pricePerSeatOere / 100}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        fontSize = 16.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color(0xFFF0F0F0),
    )
}

@Composable
private fun RouteColumn(origin: String, destination: String) {
    val dotColor = HopColors.primaryLime
    val lineColor = Color(0xFFD0D0D0)

    Column {
        // Origin row
        Row(verticalAlignment = Alignment.CenterVertically) {
            RouteDot(color = HopColors.primaryLime)
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = origin,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1A1A1A)),
                maxLines = 1,
            )
        }

        // Connector line
        Box(
            modifier = Modifier
                .padding(start = 6.dp)     // centre-align with dot
                .size(width = 2.dp, height = 12.dp)
                .background(lineColor),
        )

        // Destination row
        Row(verticalAlignment = Alignment.CenterVertically) {
            RouteDot(color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = destination,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RouteDot(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
    )
}

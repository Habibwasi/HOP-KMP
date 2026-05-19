package com.example.hop.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    isBooked: Boolean = false,
    onClick: () -> Unit = {},
) {
    val cardShape = RoundedCornerShape(12.dp)

    // Entrance pop: scale from 0.93 → 1.0 with a springy overshoot
    val scale = remember { Animatable(if (isBooked) 0.93f else 1f) }
    LaunchedEffect(isBooked) {
        if (isBooked) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
            )
        }
    }

    val elevation = if (isBooked) 14.dp else 4.dp
    val shadowColor = if (isBooked) HopColors.primaryLime.copy(alpha = 0.55f) else Color(0x1A000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(elevation = elevation, shape = cardShape, ambientColor = shadowColor, spotColor = shadowColor)
            .then(
                if (isBooked) Modifier.border(width = 2.dp, color = HopColors.primaryLime, shape = cardShape)
                else Modifier
            )
            .clip(cardShape)
            .background(if (isBooked) HopColors.primaryLime.copy(alpha = 0.04f) else Color.White)
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

                // ── Booked ✓ chip ──────────────────────────────────────────
                if (isBooked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(HopColors.primaryLime)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "Booked",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }
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

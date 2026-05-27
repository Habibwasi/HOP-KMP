package com.example.hop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.example.hop.ui.theme.HopColors

/**
 * Standardised avatar sizes.
 *
 *  Xs  = 32 dp — comment threads, suggestion chips
 *  Sm  = 40 dp — list rows
 *  Md  = 56 dp — cards + shows verified badge
 *  Lg  = 80 dp — profile headers + shows verified badge
 */
enum class AvatarSize(val dp: Dp, val initialsSize: TextUnit) {
    Xs(32.dp, 12.sp),
    Sm(40.dp, 14.sp),
    Md(56.dp, 18.sp),
    Lg(80.dp, 24.sp),
}

/** Circle avatar with fallback initials. Verified tick shown for Md and Lg. */
@Composable
fun HopAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    painter: Painter? = null,
    size: AvatarSize = AvatarSize.Sm,
    isVerified: Boolean = false,
) {
    val showVerified = isVerified && (size == AvatarSize.Md || size == AvatarSize.Lg)
    val badgeSizeDp = if (size == AvatarSize.Lg) 20.dp else 16.dp

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(HopColors.surfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size.dp)
                        .clip(CircleShape),
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(size.dp)
                                .background(HopColors.surfaceElevated),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((size.dp.value / 3).dp),
                                color = HopColors.primaryLime,
                                strokeWidth = 1.5.dp,
                            )
                        }
                    },
                    error = {
                        Text(
                            text = initials.take(2).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = size.initialsSize,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = HopColors.primaryLime,
                        )
                    },
                )
            } else if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size.dp)
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = initials.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = size.initialsSize,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = HopColors.primaryLime,
                )
            }
        }

        // Verified badge — bottom-right corner
        if (showVerified) {
            VerifiedBadge(
                size = badgeSizeDp,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

/** Small green circle with a white check drawn via Canvas (dependency-free). */
@Composable
private fun VerifiedBadge(size: Dp, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(size),
        onDraw = {
            val radius = this.size.minDimension / 2f

            // Green circle background
            drawCircle(
                color = HopColors.primaryGreen,
                radius = radius,
                center = center,
            )

            // White border ring (separates badge from avatar)
            drawCircle(
                color = Color(0xFF1A1A1A),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
            )

            // White checkmark ✓ drawn as two line segments
            val strokeWidth = (size.toPx() * 0.12f).coerceAtLeast(1.5f)
            val startX = center.x - radius * 0.40f
            val midY  = center.y + radius * 0.08f
            val midX  = center.x - radius * 0.05f
            val endX  = center.x + radius * 0.42f
            val startY = center.y + radius * 0.08f
            val midYLeft = midY + radius * 0.22f
            val endY  = center.y - radius * 0.32f

            // Short left stroke of checkmark
            drawLine(
                color = Color.White,
                start = Offset(startX, startY),
                end   = Offset(midX, midYLeft),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            // Long right stroke of checkmark
            drawLine(
                color = Color.White,
                start = Offset(midX, midYLeft),
                end   = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        },
    )
}

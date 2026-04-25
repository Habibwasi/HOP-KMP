package com.example.hop.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors

/**
 * Animated shimmer overlay used for loading skeletons.
 * Apply to any composable that already has a background; the shimmer is
 * painted on top with [BlendMode.SrcAtop] so it respects the underlying shape.
 */
fun Modifier.shimmerLoading(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )
    drawWithContent {
        drawContent()
        val width = size.width
        val brush = Brush.linearGradient(
            colors = listOf(
                HopColors.shimmerBase.copy(alpha = 0f),
                HopColors.shimmerHighlight.copy(alpha = 0.85f),
                HopColors.shimmerBase.copy(alpha = 0f),
            ),
            start = Offset(x = offset * width, y = 0f),
            end = Offset(x = (offset + 0.6f) * width, y = size.height),
        )
        drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
    }
}

/**
 * Convenience block-level skeleton — pre-styled with the shimmer base colour
 * and rounded corners so callers only specify width/height.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(HopColors.shimmerBase)
            .shimmerLoading(),
    )
}

@Composable
fun SkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(HopColors.shimmerBase)
            .shimmerLoading(),
    )
}

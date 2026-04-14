package com.example.hop.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ── Star colours ────────────────────────────────────────────────────────────
private val StarFilled  = Color(0xFFFBBF24)   // Amber-400
private val StarEmpty   = Color(0xFF3A3A3A)   // neutral dark surface

// ── Star path helper ─────────────────────────────────────────────────────────

/** Returns a 5-pointed star Path centred at (0,0) fitting inside [size]. */
private fun starPath(size: Float): Path {
    val outerR = size / 2f
    val innerR = outerR * 0.4f
    val path = Path()
    val totalPoints = 10
    for (i in 0 until totalPoints) {
        val radius = if (i % 2 == 0) outerR else innerR
        // Start at top (-π/2) and step by π/5 (36°) per point
        val angle = (Math.PI * (-0.5 + i.toDouble() / 5)).toFloat()
        val x = (radius * cos(angle))
        val y = (radius * sin(angle))
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// ── Read-only display ─────────────────────────────────────────────────────────

/**
 * Read-only 5-star rating display with half-star support.
 *
 * [rating] is a Float in 0..5. Values like 3.5 render a half-filled star.
 * Each star is [starSize] dp; total row width is (5 × [starSize] + 4 × [gap]) dp.
 */
@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    gap: Dp = 2.dp,
    contentDescription: String = "$rating out of 5 stars",
) {
    Row(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
    ) {
        for (index in 1..5) {
            val fill = (rating - (index - 1)).coerceIn(0f, 1f)
            StarCanvas(
                fillFraction = fill,
                size = starSize,
                modifier = if (index < 5) Modifier.size(starSize) else Modifier.size(starSize),
            )
            if (index < 5 && gap > 0.dp) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(gap))
            }
        }
    }
}

// ── Interactive input ─────────────────────────────────────────────────────────

/**
 * Interactive 5-star rating input.
 *
 * Tap targets are 44×44 dp per WCAG / HIG minimum.
 * Stars animate from empty → filled with a short spring.
 *
 * @param rating     Current selected star count (0 = no selection).
 * @param onRatingChange Called with the new star count (1–5) on tap.
 */
@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 28.dp,
) {
    var hoveredStar by remember { mutableIntStateOf(0) }

    Row(modifier = modifier) {
        for (index in 1..5) {
            val displayRating = if (hoveredStar > 0) hoveredStar else rating
            val targetFill = if (index <= displayRating) 1f else 0f
            val animatedFill by animateFloatAsState(
                targetValue = targetFill,
                animationSpec = tween(durationMillis = 150),
                label = "starFill_$index",
            )

            StarCanvas(
                fillFraction = animatedFill,
                size = starSize,
                modifier = Modifier
                    .size(44.dp)   // 44 dp tap target
                    .semantics {
                        role = Role.Button
                        contentDescription = "$index star${if (index == 1) "" else "s"}"
                    }
                    .pointerInput(index) {
                        detectTapGestures(
                            onPress = {
                                hoveredStar = index
                                val released = tryAwaitRelease()
                                hoveredStar = 0
                                if (released) onRatingChange(index)
                            },
                        )
                    },
            )
        }
    }
}

// ── Single star canvas ────────────────────────────────────────────────────────

/**
 * Draws one star.
 * [fillFraction] 0.0 = empty, 0.5 = half-filled, 1.0 = full.
 * The half-star effect is achieved by clipping the filled layer to the left half.
 */
@Composable
private fun StarCanvas(
    fillFraction: Float,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val path = starPath(canvasSize)

        // Translate path from (0,0) centre to canvas centre
        val translateX = this.size.width / 2f
        val translateY = this.size.height / 2f

        scale(scaleX = 1f, scaleY = 1f, pivot = center) {
            // Empty (background) star
            drawPath(
                path = translatePath(path, translateX, translateY),
                color = StarEmpty,
            )

            // Filled (foreground) star — clipped to fillFraction width
            if (fillFraction > 0f) {
                val clipWidth = this.size.width * fillFraction.coerceIn(0f, 1f)
                clipRect(
                    left   = 0f,
                    top    = 0f,
                    right  = clipWidth,
                    bottom = this.size.height,
                ) {
                    drawPath(
                        path = translatePath(path, translateX, translateY),
                        color = StarFilled,
                    )
                }
            }
        }
    }
}

/** Translates a Path by (dx, dy) by creating a new Path. */
private fun translatePath(source: Path, dx: Float, dy: Float): Path {
    val result = Path()
    result.addPath(source, offset = Offset(dx, dy))
    return result
}

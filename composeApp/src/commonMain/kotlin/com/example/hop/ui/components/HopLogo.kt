package com.example.hop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopTheme

// ── HopLogo ───────────────────────────────────────────────────────────────────

/**
 * Renders the Hop brand logo as a Canvas composable.
 *
 * The logo is drawn from the SVG viewBox (690 × 150) and scaled uniformly to
 * fit [width] × [height], preserving aspect ratio and centering within the
 * allocated bounds.
 *
 * Usage:
 * ```
 * HopLogo(modifier = Modifier.width(120.dp))
 * HopLogo(width = 200.dp, height = 43.dp)
 * ```
 */
@Composable
fun HopLogo(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 26.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        drawHopLogo(this)
    }
}

// ── Internal drawing ──────────────────────────────────────────────────────────

private fun drawHopLogo(scope: DrawScope) {
    // SVG viewBox: 690 × 150
    val viewW = 690f
    val viewH = 150f

    val scale = minOf(scope.size.width / viewW, scope.size.height / viewH)
    val offsetX = (scope.size.width - viewW * scale) / 2f
    val offsetY = (scope.size.height - viewH * scale) / 2f

    scope.withTransform({
        translate(left = offsetX, top = offsetY)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        val white    = Color(0xFFFFFFFF)
        val darkBg   = Color(0xFF1B4332)
        val lime     = Color(0xFF52B788)

        // ── Background ────────────────────────────────────────────────────────
        drawRect(
            color = darkBg,
            topLeft = Offset(0f, 0f),
            size = Size(680f, 140f),
        )

        // ── h — left stem ─────────────────────────────────────────────────────
        drawRoundRect(
            color = white,
            topLeft = Offset(220f, 28f),
            size = Size(15f, 84f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── h — right stem ────────────────────────────────────────────────────
        drawRoundRect(
            color = white,
            topLeft = Offset(283f, 58f),
            size = Size(15f, 54f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── h — crossbar arc: M235 82 Q249 48 283 66 ─────────────────────────
        val hCrossbar = Path().apply {
            moveTo(235f, 82f)
            quadraticTo(249f, 48f, 283f, 66f)
        }
        drawPath(
            path = hCrossbar,
            color = white,
            style = Stroke(width = 15f, cap = StrokeCap.Round),
        )

        // ── o — white ring ────────────────────────────────────────────────────
        drawCircle(
            color = white,
            radius = 33f,
            center = Offset(358f, 90f),
            style = Stroke(width = 15f),
        )

        // ── o — road strip (mask with bg color) ───────────────────────────────
        drawLine(
            color = darkBg,
            start = Offset(326f, 90f),
            end = Offset(391f, 90f),
            strokeWidth = 5f,
        )

        // ── o — center-line dashes ────────────────────────────────────────────
        drawLine(color = lime, start = Offset(336f, 90f), end = Offset(346f, 90f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = lime, start = Offset(352f, 90f), end = Offset(362f, 90f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = lime, start = Offset(368f, 90f), end = Offset(378f, 90f), strokeWidth = 2.5f, cap = StrokeCap.Round)

        // ── p — stem ──────────────────────────────────────────────────────────
        drawRoundRect(
            color = white,
            topLeft = Offset(412f, 56f),
            size = Size(15f, 76f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── p — bowl: M427 74 Q427 43 458 56 Q485 68 474 90 Q463 112 427 106 ─
        val pBowl = Path().apply {
            moveTo(427f, 74f)
            quadraticTo(427f, 43f, 458f, 56f)
            quadraticTo(485f, 68f, 474f, 90f)
            quadraticTo(463f, 112f, 427f, 106f)
        }
        drawPath(
            path = pBowl,
            color = white,
            style = Stroke(width = 15f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "HopLogo — default", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun HopLogoPreview() {
    HopTheme {
        HopLogo()
    }
}

@Preview(name = "HopLogo — large", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun HopLogoLargePreview() {
    HopTheme {
        HopLogo(width = 240.dp, height = 52.dp)
    }
}

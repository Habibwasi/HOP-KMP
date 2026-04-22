package com.example.hop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
    width: Dp = 88.dp,
    height: Dp = 38.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        drawHopLogo(this)
    }
}

// ── Internal drawing ──────────────────────────────────────────────────────────

private fun drawHopLogo(scope: DrawScope) {
    // Cropped viewBox: original SVG coords offset by (-195, -8) so content
    // fills the canvas exactly. Original content spanned x:195–510, y:8–142.
    val viewW = 315f
    val viewH = 134f

    val scale = minOf(scope.size.width / viewW, scope.size.height / viewH)
    val offsetX = (scope.size.width - viewW * scale) / 2f
    val offsetY = (scope.size.height - viewH * scale) / 2f

    scope.withTransform({
        translate(left = offsetX, top = offsetY)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        val lime     = Color(0xFFC8F135)
        val ink      = Color(0xFF1A1A1A)
        val bgMask   = lime

        // ── Pill background ───────────────────────────────────────────────────
        drawRoundRect(
            color = lime,
            topLeft = Offset(0f, 0f),
            size = Size(315f, 134f),
            cornerRadius = CornerRadius(36f, 36f),
        )

        // ── h — left stem ─────────────────────────────────────────────────────
        drawRoundRect(
            color = ink,
            topLeft = Offset(25f, 20f),
            size = Size(15f, 84f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── h — right stem ────────────────────────────────────────────────────
        drawRoundRect(
            color = ink,
            topLeft = Offset(88f, 50f),
            size = Size(15f, 54f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── h — crossbar arc ─────────────────────────────────────────────────
        val hCrossbar = Path().apply {
            moveTo(40f, 74f)
            quadraticTo(54f, 40f, 88f, 58f)
        }
        drawPath(
            path = hCrossbar,
            color = ink,
            style = Stroke(width = 15f, cap = StrokeCap.Round),
        )

        // ── o — ink ring ──────────────────────────────────────────────────────
        drawCircle(
            color = ink,
            radius = 33f,
            center = Offset(163f, 82f),
            style = Stroke(width = 15f),
        )

        // ── o — road strip (mask with pill color) ─────────────────────────────
        drawLine(
            color = bgMask,
            start = Offset(131f, 82f),
            end = Offset(196f, 82f),
            strokeWidth = 5f,
        )

        // ── o — center-line dashes ────────────────────────────────────────────
        val dashColor = Color(0xFF167A30)
        drawLine(color = dashColor, start = Offset(141f, 82f), end = Offset(151f, 82f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = dashColor, start = Offset(157f, 82f), end = Offset(167f, 82f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = dashColor, start = Offset(173f, 82f), end = Offset(183f, 82f), strokeWidth = 2.5f, cap = StrokeCap.Round)

        // ── p — stem ──────────────────────────────────────────────────────────
        drawRoundRect(
            color = ink,
            topLeft = Offset(217f, 48f),
            size = Size(15f, 76f),
            cornerRadius = CornerRadius(7f, 7f),
        )

        // ── p — bowl ─────────────────────────────────────────────────────────
        val pBowl = Path().apply {
            moveTo(232f, 66f)
            quadraticTo(232f, 35f, 263f, 48f)
            quadraticTo(290f, 60f, 279f, 82f)
            quadraticTo(268f, 104f, 232f, 98f)
        }
        drawPath(
            path = pBowl,
            color = ink,
            style = Stroke(width = 15f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "HopLogo — default", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HopLogoPreview() {
    HopTheme {
        HopLogo()
    }
}

@Preview(name = "HopLogo — large", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HopLogoLargePreview() {
    HopTheme {
        HopLogo(width = 240.dp, height = 52.dp)
    }
}

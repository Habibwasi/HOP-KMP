package com.example.hop.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Full-size Canvas that paints the Hop logo as a very-low-opacity watermark.
 * Draws on top of whatever background the parent provides; intended to be the
 * first (bottom-most) child inside a [androidx.compose.foundation.layout.Box].
 *
 * SVG source viewBox: 690 × 260.
 */
@Composable
fun HopLogoBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val svgW  = 690f
        val svgH  = 260f
        val scale = size.width / svgW

        // Vertically centre the 260-unit-tall logo in the available height.
        val startY = (size.height - svgH * scale) / 2f

        // Coordinate helpers
        fun px(v: Float) = v * scale             // x position
        fun py(v: Float) = startY + v * scale    // y position
        fun sx(v: Float) = v * scale             // size / stroke / radius

        val alpha     = 0.08f
        val darkGreen  = Color(0xFF2E7D32).copy(alpha = alpha)
        val ringGreen  = Color(0xFF558B2F).copy(alpha = alpha)
        val medGreen   = Color(0xFF4CAF50).copy(alpha = alpha)
        val ltGreen    = Color(0xFFA5D6A7).copy(alpha = alpha)
        val limeGreen  = Color(0xFFAED581).copy(alpha = alpha)
        val xlGreen    = Color(0xFF81C784).copy(alpha = alpha)
        val dkGreen    = Color(0xFF388E3C).copy(alpha = alpha)

        val strokeW14 = sx(14f)

        // ── Letter "h" ────────────────────────────────────────────────────────
        drawLine(darkGreen,
            Offset(px(182f), py(75f)), Offset(px(182f), py(172f)),
            strokeWidth = strokeW14, cap = StrokeCap.Round)

        val hArch = Path().apply {
            moveTo(px(182f), py(124f))
            quadraticBezierTo(px(214f), py(92f), px(246f), py(124f))
        }
        drawPath(hArch, darkGreen, style = Stroke(width = strokeW14, cap = StrokeCap.Round))

        drawLine(darkGreen,
            Offset(px(246f), py(124f)), Offset(px(246f), py(172f)),
            strokeWidth = strokeW14, cap = StrokeCap.Round)

        // ── Letter "o" ring ───────────────────────────────────────────────────
        drawCircle(ringGreen, radius = sx(38f), center = Offset(px(340f), py(136f)),
            style = Stroke(width = strokeW14))

        // ── Bunny inside the "o" ──────────────────────────────────────────────
        // Body
        drawOval(medGreen,
            topLeft = Offset(px(326f), py(136f)), size = Size(sx(28f), sx(24f)))
        // Head
        drawCircle(medGreen, radius = sx(11f), center = Offset(px(340f), py(131f)))
        // Ears
        drawOval(medGreen,
            topLeft = Offset(px(330f), py(102f)), size = Size(sx(8f), sx(20f)))
        drawOval(medGreen,
            topLeft = Offset(px(342f), py(102f)), size = Size(sx(8f), sx(20f)))
        // Inner ears
        drawOval(ltGreen,
            topLeft = Offset(px(332f), py(105f)), size = Size(sx(4f), sx(14f)))
        drawOval(ltGreen,
            topLeft = Offset(px(344f), py(105f)), size = Size(sx(4f), sx(14f)))
        // Eyes (bright dots → slightly higher alpha so they're visible)
        drawCircle(Color.White.copy(alpha = alpha * 2.5f),
            radius = sx(1.6f), center = Offset(px(336f), py(130f)))
        drawCircle(Color.White.copy(alpha = alpha * 2.5f),
            radius = sx(1.6f), center = Offset(px(344f), py(130f)))
        // Nose
        drawOval(Color(0xFFC8E6C9).copy(alpha = alpha),
            topLeft = Offset(px(338.5f), py(133f)), size = Size(sx(3f), sx(2f)))
        // Tail
        drawCircle(ltGreen, radius = sx(4f), center = Offset(px(354f), py(150f)))
        // Feet
        drawOval(dkGreen,
            topLeft = Offset(px(327f), py(156f)), size = Size(sx(10f), sx(6f)))
        drawOval(dkGreen,
            topLeft = Offset(px(343f), py(156f)), size = Size(sx(10f), sx(6f)))

        // ── Letter "p" ────────────────────────────────────────────────────────
        drawLine(darkGreen,
            Offset(px(426f), py(122f)), Offset(px(426f), py(186f)),
            strokeWidth = strokeW14, cap = StrokeCap.Round)
        drawCircle(darkGreen, radius = sx(36f), center = Offset(px(456f), py(136f)),
            style = Stroke(width = strokeW14))
        // Dot inside "p"
        drawCircle(limeGreen, radius = sx(10f), center = Offset(px(456f), py(136f)))

        // ── Leaf above the "o" ────────────────────────────────────────────────
        drawOval(medGreen,
            topLeft = Offset(px(324f), py(68f)), size = Size(sx(32f), sx(20f)))
        drawOval(xlGreen.copy(alpha = alpha * 0.6f),
            topLeft = Offset(px(326.5f), py(76.5f)), size = Size(sx(13f), sx(7f)))
        drawLine(darkGreen,
            Offset(px(340f), py(68f)), Offset(px(340f), py(88f)),
            strokeWidth = sx(2f), cap = StrokeCap.Round)
        drawLine(darkGreen,
            Offset(px(333f), py(74f)), Offset(px(347f), py(82f)),
            strokeWidth = sx(1.4f), cap = StrokeCap.Round)
        drawLine(darkGreen,
            Offset(px(333f), py(82f)), Offset(px(347f), py(74f)),
            strokeWidth = sx(1.4f), cap = StrokeCap.Round)

        // Trailing mini leaves (rotated)
        withTransform({
            rotate(degrees = -22f, pivot = Offset(px(316f), py(90f)))
        }) {
            drawOval(ltGreen.copy(alpha = alpha * 0.5f),
                topLeft = Offset(px(308f), py(85f)), size = Size(sx(16f), sx(10f)))
        }
        withTransform({
            rotate(degrees = -12f, pivot = Offset(px(327f), py(82f)))
        }) {
            drawOval(xlGreen.copy(alpha = alpha * 0.4f),
                topLeft = Offset(px(317f), py(76f)), size = Size(sx(20f), sx(12f)))
        }
    }
}

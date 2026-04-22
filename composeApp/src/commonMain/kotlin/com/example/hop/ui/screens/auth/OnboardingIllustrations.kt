package com.example.hop.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin

/** Pick the right illustration for the given slide [index] (0-based). */
@Composable
fun OnboardingIllustration(index: Int, modifier: Modifier = Modifier) {
    when (index) {
        0    -> Slide1Illustration(modifier)
        1    -> Slide2Illustration(modifier)
        else -> Slide3Illustration(modifier)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 1 — "Fewer cars. Better journeys."
// Day scene: animated sun, drifting clouds, trees, road, carpooling car + leaves
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Slide1Illustration(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "s1")
    val carY       by inf.animateFloat(0f,   -6f,  infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "carY")
    val wheelAngle by inf.animateFloat(0f,  360f,  infiniteRepeatable(tween(1200, easing = LinearEasing)),                           "wheel")
    val sunScale   by inf.animateFloat(1f,  1.07f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "sun")
    val cloud1X    by inf.animateFloat(0f,   8f,   infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "cloud1")
    val cloud2X    by inf.animateFloat(8f,   0f,   infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "cloud2")
    // leaf-a: translate+rotate; leaf-b: translate+rotate; leaf-c: translate+rotate
    val leaf1T     by inf.animateFloat(0f,   1f,   infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "l1")
    val leaf2T     by inf.animateFloat(0f,   1f,   infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(400)), "l2")
    val leaf3T     by inf.animateFloat(0f,   1f,   infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(900)), "l3")

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 280f
        val scaleY = size.height / 200f
        val scaleR = minOf(scaleX, scaleY)
        fun x(v: Float) = v * scaleX
        fun y(v: Float) = v * scaleY
        fun r(v: Float) = v * scaleR

        // Sky
        drawRect(Color(0xFFE8F5E9))

        // Sun (pulsing about its own centre)
        withTransform({ scale(sunScale, sunScale, Offset(x(240f), y(38f))) }) {
            drawCircle(Color(0xFFFFF9C4), radius = r(22f), center = Offset(x(240f), y(38f)))
            drawCircle(Color(0xFFFFF176), radius = r(15f), center = Offset(x(240f), y(38f)))
        }

        // Cloud 1 (slides right)
        withTransform({ translate(cloud1X * scaleX, 0f) }) {
            drawOval(Color.White.copy(alpha = 0.85f), topLeft = Offset(x(32f),  y(23f)), size = Size(x(56f), y(24f)))
            drawOval(Color.White.copy(alpha = 0.85f), topLeft = Offset(x(62f),  y(19f)), size = Size(x(36f), y(22f)))
            drawOval(Color.White.copy(alpha = 0.85f), topLeft = Offset(x(28f),  y(23f)), size = Size(x(28f), y(18f)))
        }
        // Cloud 2 (slides left — alternate-reverse)
        withTransform({ translate(cloud2X * scaleX, 0f) }) {
            drawOval(Color.White.copy(alpha = 0.70f), topLeft = Offset(x(138f), y(18f)), size = Size(x(44f), y(20f)))
            drawOval(Color.White.copy(alpha = 0.70f), topLeft = Offset(x(161f), y(15f)), size = Size(x(28f), y(18f)))
            drawOval(Color.White.copy(alpha = 0.70f), topLeft = Offset(x(136f), y(18f)), size = Size(x(24f), y(16f)))
        }

        // Ground + road  (SVG: ground y=158 h=42, road y=146 h=20, dashes y=154)
        drawRect(Color(0xFFA5D6A7), Offset(0f, y(158f)), Size(size.width, y(42f)))
        drawRect(Color(0xFF78909C), Offset(0f, y(146f)), Size(size.width, y(20f)))
        listOf(20f, 75f, 130f, 185f, 240f).forEach { dx ->
            drawRoundRect(Color(0xFFECEFF1).copy(alpha = 0.7f), Offset(x(dx), y(154f)), Size(x(30f), y(4f)), CornerRadius(r(2f)))
        }

        // Trees — left (SVG coords)
        drawRect(Color(0xFF6D4C41), Offset(x(18f), y(112f)), Size(x(7f), y(36f)))
        drawOval(Color(0xFF388E3C), topLeft = Offset(x(3f),  y(78f)), size = Size(x(36f), y(44f)))
        drawOval(Color(0xFF43A047), topLeft = Offset(x(8f),  y(76f)), size = Size(x(26f), y(32f)))
        drawRect(Color(0xFF6D4C41), Offset(x(45f), y(120f)), Size(x(6f), y(28f)))
        drawOval(Color(0xFF2E7D32), topLeft = Offset(x(34f), y(92f)), size = Size(x(28f), y(36f)))
        drawOval(Color(0xFF388E3C), topLeft = Offset(x(38f), y(92f)), size = Size(x(20f), y(24f)))

        // Trees — right (SVG coords)
        drawRect(Color(0xFF6D4C41), Offset(x(232f), y(114f)), Size(x(7f), y(34f)))
        drawOval(Color(0xFF388E3C), topLeft = Offset(x(217f), y(80f)), size = Size(x(36f), y(44f)))
        drawOval(Color(0xFF43A047), topLeft = Offset(x(222f), y(78f)), size = Size(x(26f), y(32f)))
        drawRect(Color(0xFF6D4C41), Offset(x(256f), y(120f)), Size(x(6f), y(28f)))
        drawOval(Color(0xFF2E7D32), topLeft = Offset(x(245f), y(92f)), size = Size(x(28f), y(36f)))

        // Car (floating — SVG .car-body: float 3s)
        withTransform({ translate(0f, carY * scaleY) }) {
            // Body + cabin (SVG: body x=62 y=112 w=130 h=34, cabin x=82 y=94 w=90 h=28)
            drawRoundRect(Color(0xFF26C6DA), Offset(x(62f), y(112f)), Size(x(130f), y(34f)), CornerRadius(r(8f)))
            drawRoundRect(Color(0xFF00ACC1), Offset(x(82f), y(94f)),  Size(x(90f),  y(28f)), CornerRadius(r(8f)))
            // Windows (SVG: x=88 y=99 w=32 h=18; x=126 y=99 w=32 h=18)
            drawRoundRect(Color(0xFFE0F7FA).copy(alpha = 0.9f), Offset(x(88f),  y(99f)), Size(x(32f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE0F7FA).copy(alpha = 0.9f), Offset(x(126f), y(99f)), Size(x(32f), y(18f)), CornerRadius(r(4f)))
            // Heads (SVG: cx=104 cy=106; cx=142 cy=106)
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(104f), y(106f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(142f), y(106f)))
            // Smiles (SVG: M101 108 Q104 111 107 108; M139 108 Q142 111 145 108)
            drawSmile(Offset(x(104f), y(108f)), r(3f), Color(0xFFE64A19))
            drawSmile(Offset(x(142f), y(108f)), r(3f), Color(0xFFE64A19))
            // Bumper + lights (SVG: bumper y=138; headlight x=184 y=120; taillight x=64 y=120)
            drawRoundRect(Color(0xFF0097A7), Offset(x(62f),  y(138f)), Size(x(130f), y(8f)),  CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(184f), y(120f)), Size(x(10f),  y(8f)),  CornerRadius(r(3f)))
            drawRoundRect(Color(0xFFEF9A9A), Offset(x(64f),  y(120f)), Size(x(8f),   y(8f)),  CornerRadius(r(3f)))
            // Wheels (SVG: cx=92 cy=146 r=14; cx=168 cy=146 r=14; 8-spoke: +×)
            drawWheel8Spoke(Offset(x(92f),  y(146f)), r(14f), r(9f),  r(4f),  wheelAngle,
                Color(0xFF37474F), Color(0xFF546E7A), Color(0xFFB0BEC5))
            drawWheel8Spoke(Offset(x(168f), y(146f)), r(14f), r(9f),  r(4f),  wheelAngle,
                Color(0xFF37474F), Color(0xFF546E7A), Color(0xFFB0BEC5))
        }

        // Floating leaves — leaf-a origin 30,60 (–10°→+10°, translate 0→4/–8)
        withTransform({
            translate(leaf1T * x(4f), leaf1T * -y(8f))
            rotate(degrees = -10f + leaf1T * 20f, pivot = Offset(x(30f), y(60f)))
        }) {
            drawOval(Color(0xFF66BB6A),
                topLeft = Offset(x(30f) - r(10f), y(60f) - r(6f)),
                size = Size(r(20f), r(12f)))
        }
        // leaf-b origin 240,70 (5°→–8°, translate 0→–5/–6)
        withTransform({
            translate(leaf2T * -x(5f), leaf2T * -y(6f))
            rotate(degrees = 5f + leaf2T * -13f, pivot = Offset(x(240f), y(70f)))
        }) {
            drawOval(Color(0xFF81C784),
                topLeft = Offset(x(240f) - r(9f), y(70f) - r(5f)),
                size = Size(r(18f), r(10f)))
        }
        // leaf-c origin 255,50 (same as leaf-a timing)
        withTransform({
            translate(leaf3T * x(4f), leaf3T * -y(8f))
            rotate(degrees = -10f + leaf3T * 20f, pivot = Offset(x(255f), y(50f)))
        }) {
            drawOval(Color(0xFFA5D6A7),
                topLeft = Offset(x(255f) - r(7f), y(50f) - r(4f)),
                size = Size(r(14f), r(8f)))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 2 — "Every seat filled matters."
// CO₂ fading cloud, pulsing leaf, sparkles, blue car with 3 passengers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Slide2Illustration(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "s2")
    val carBob     by inf.animateFloat(0f,   -5f,   infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "bob")
    val wheelAngle by inf.animateFloat(0f,  360f,   infiniteRepeatable(tween(1000, easing = LinearEasing)),                           "whl")
    val leafScale  by inf.animateFloat(1f,   1.15f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "leaf")
    val smokeOff   by inf.animateFloat(0f,   30f,   infiniteRepeatable(tween(2500, easing = LinearEasing)),                           "smoke")
    val smokeAlpha by inf.animateFloat(0.7f, 0f,    infiniteRepeatable(tween(2500, easing = LinearEasing)),                           "smkA")
    val sp1        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "sp1")
    val sp2        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(600)),  "sp2")
    val sp3        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(1200)), "sp3")

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 280f
        val scaleY = size.height / 200f
        val scaleR = minOf(scaleX, scaleY)
        fun x(v: Float) = v * scaleX
        fun y(v: Float) = v * scaleY
        fun r(v: Float) = v * scaleR

        drawRect(Color(0xFFF1F8E9))

        // Ground + road  (SVG: ground y=158 h=42, road y=146 h=18, dashes y=153)
        drawRect(Color(0xFFC8E6C9), Offset(0f, y(158f)), Size(size.width, y(42f)))
        drawRect(Color(0xFF90A4AE), Offset(0f, y(146f)), Size(size.width, y(18f)))
        listOf(10f, 60f, 115f, 170f, 225f).forEach { dx ->
            drawRoundRect(Color.White.copy(alpha = 0.6f), Offset(x(dx), y(153f)), Size(x(25f), y(3f)), CornerRadius(r(1.5f)))
        }

        // CO₂ static cloud (SVG: ellipses at cx=38 cy=62, cx=22 cy=58, cx=52 cy=55 — opacity 0.55)
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), topLeft = Offset(x(10f), y(46f)), size = Size(x(56f), y(32f)))
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), topLeft = Offset(x(6f),  y(46f)), size = Size(x(32f), y(24f)))
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), topLeft = Offset(x(38f), y(44f)), size = Size(x(28f), y(22f)))

        // Rising smoke particles (SVG: .co2 cx=38 cy=38; .co2-2 cx=44 cy=34; .co2-3 cx=32 cy=36)
        drawCircle(Color(0xFFB0BEC5).copy(alpha = smokeAlpha * 0.5f), radius = r(6f),  center = Offset(x(38f), y(38f) - smokeOff * scaleY))
        drawCircle(Color(0xFFB0BEC5).copy(alpha = smokeAlpha * 0.4f), radius = r(5f),  center = Offset(x(44f), y(34f) - smokeOff * scaleY))
        drawCircle(Color(0xFFB0BEC5).copy(alpha = smokeAlpha * 0.3f), radius = r(4f),  center = Offset(x(32f), y(36f) - smokeOff * scaleY))

        // Pulsing leaf (SVG: outer ellipse cx=140 cy=48 rx=38 ry=24; inner rx=28 ry=16; 2 vein lines)
        withTransform({ scale(leafScale, leafScale, Offset(x(140f), y(48f))) }) {
            drawOval(Color(0xFF66BB6A), topLeft = Offset(x(102f), y(24f)), size = Size(x(76f), y(48f)))
            drawOval(Color(0xFF81C784), topLeft = Offset(x(112f), y(32f)), size = Size(x(56f), y(32f)))
            drawLine(Color(0xFF388E3C), Offset(x(110f), y(48f)), Offset(x(170f), y(48f)), r(1.5f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(140f), y(30f)), Offset(x(140f), y(66f)), r(1.5f), StrokeCap.Round)
        }

        // Sparkles (SVG: sp1 cx=200 cy=35 r=4 yellow; sp2 cx=220 cy=55 r=3 green; sp3 cx=178 cy=28 r=3 cyan)
        drawSparkle(Offset(x(200f), y(35f)), r(4f), Color(0xFFFFF176), sp1)
        drawSparkle(Offset(x(220f), y(55f)), r(3f), Color(0xFFA5D6A7), sp2)
        drawSparkle(Offset(x(178f), y(28f)), r(3f), Color(0xFF80DEEA), sp3)

        // Car (bobbing — SVG: body x=52 y=110 w=148 h=36; cabin x=74 y=91 w=104 h=30)
        withTransform({ translate(0f, carBob * scaleY) }) {
            drawRoundRect(Color(0xFF42A5F5), Offset(x(52f), y(110f)), Size(x(148f), y(36f)), CornerRadius(r(9f)))
            drawRoundRect(Color(0xFF1E88E5), Offset(x(74f), y(91f)),  Size(x(104f), y(30f)), CornerRadius(r(9f)))
            // Windows (SVG: x=80 y=97; x=112 y=97; x=144 y=97 — all w=26 h=18)
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(80f),  y(97f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(112f), y(97f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(144f), y(97f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            // Heads (SVG: cx=93 cy=104; cx=125 cy=104; cx=157 cy=104)
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(93f),  y(104f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(125f), y(104f)))
            drawCircle(Color(0xFFCE93D8), radius = r(7f), center = Offset(x(157f), y(104f)))
            drawSmile(Offset(x(93f),  y(106f)), r(3f), Color(0xFFBF360C))
            drawSmile(Offset(x(125f), y(106f)), r(3f), Color(0xFFBF360C))
            drawSmile(Offset(x(157f), y(106f)), r(3f), Color(0xFF6A1B9A))
            // Bumper + headlight (SVG: bumper y=138; headlight x=194 y=118 w=8 h=7)
            drawRoundRect(Color(0xFF1565C0), Offset(x(52f),  y(138f)), Size(x(148f), y(8f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(194f), y(118f)), Size(x(8f),   y(7f)), CornerRadius(r(3f)))
            // Wheels (SVG: cx=90 cy=148 r=13; cx=166 cy=148 r=13; + cross only)
            drawWheelCross(Offset(x(90f),  y(148f)), r(13f), r(8f),   r(3.5f), wheelAngle,
                Color(0xFF37474F), Color(0xFF546E7A), Color(0xFFB0BEC5))
            drawWheelCross(Offset(x(166f), y(148f)), r(13f), r(8f),   r(3.5f), wheelAngle,
                Color(0xFF37474F), Color(0xFF546E7A), Color(0xFFB0BEC5))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 3 — "Move together. Live lighter."
// Night drive: moon, twinkling stars, silhouette hills+trees, scrolling dashes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Slide3Illustration(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "s3")
    val carFloat   by inf.animateFloat(0f,  -4f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "cf")
    val wheelAngle by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(1100, easing = LinearEasing)),                           "whl")
    val dashOffset by inf.animateFloat(0f,  60f, infiniteRepeatable(tween(1800, easing = LinearEasing)),                           "dash")
    val moonGlow   by inf.animateFloat(0.9f, 1f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "moon")
    val s1 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "s1")
    val s2 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(400)),  "s2")
    val s3 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(800)),  "s3")
    val s4 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(1200)), "s4")
    val s5 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(1600)), "s5")

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 280f
        val scaleY = size.height / 200f
        val scaleR = minOf(scaleX, scaleY)
        fun x(v: Float) = v * scaleX
        fun y(v: Float) = v * scaleY
        fun r(v: Float) = v * scaleR

        // Night sky
        drawRect(Color(0xFF1A237E))
        drawRect(Color(0xFF283593), Offset(0f, y(100f)), Size(size.width, y(100f)))

        // Horizon glow (SVG: ellipse cx=140 cy=140 rx=160 ry=40 opacity=0.35)
        drawOval(Color(0xFF00796B).copy(alpha = 0.35f), topLeft = Offset(x(-20f), y(100f)), size = Size(x(320f), y(80f)))

        // Moon (SVG: cx=230 cy=40; 3 circles r=24/18/13)
        drawCircle(Color(0xFFFFF9C4).copy(alpha = 0.15f * moonGlow), radius = r(24f), center = Offset(x(230f), y(40f)))
        drawCircle(Color(0xFFFFF9C4).copy(alpha = 0.20f * moonGlow), radius = r(18f), center = Offset(x(230f), y(40f)))
        drawCircle(Color(0xFFFFFDE7),                                 radius = r(13f), center = Offset(x(230f), y(40f)))

        // Stars (SVG groups: s1-s5 with staggered twinkle)
        data class Star(val cx: Float, val cy: Float, val rad: Float, val a: Float)
        listOf(
            Star(30f,  22f, 2f,   s1), Star(68f,  14f, 1.5f, s2),
            Star(110f, 30f, 2f,   s3), Star(155f, 18f, 1.5f, s4),
            Star(185f, 35f, 2f,   s5), Star(50f,  45f, 1.5f, s1),
            Star(200f, 20f, 1.5f, s2),
        ).forEach { drawCircle(Color.White.copy(alpha = it.a), radius = r(it.rad), center = Offset(x(it.cx), y(it.cy))) }

        // Hill silhouettes (SVG: ellipses cx=40,cy=145 rx=55,ry=35 / cx=240,148 / cx=140,152)
        drawOval(Color(0xFF1B5E20), topLeft = Offset(x(-15f), y(110f)), size = Size(x(110f), y(70f)))
        drawOval(Color(0xFF1B5E20), topLeft = Offset(x(185f), y(118f)), size = Size(x(110f), y(60f)))
        drawOval(Color(0xFF1B5E20), topLeft = Offset(x(60f),  y(124f)), size = Size(x(160f), y(56f)))

        // Silhouette trees — left (SVG: trunk x=12 y=110 w=6 h=30; triangle 15,85 3,118 27,118)
        drawRect(Color(0xFF0D3B0D), Offset(x(12f), y(110f)), Size(x(6f), y(30f)))
        drawPath(Path().apply {
            moveTo(x(15f), y(85f)); lineTo(x(3f), y(118f)); lineTo(x(27f), y(118f)); close()
        }, Color(0xFF0D3B0D))

        // Silhouette trees — right (SVG: trunk x=255 y=112 w=6 h=28; triangle 258,88 246,120 270,120)
        drawRect(Color(0xFF0D3B0D), Offset(x(255f), y(112f)), Size(x(6f), y(28f)))
        drawPath(Path().apply {
            moveTo(x(258f), y(88f)); lineTo(x(246f), y(120f)); lineTo(x(270f), y(120f)); close()
        }, Color(0xFF0D3B0D))

        // Road (SVG: y=148 h=28)
        drawRect(Color(0xFF37474F), Offset(0f, y(148f)), Size(size.width, y(28f)))

        // Animated road dashes (SVG: .road-dash translateX 0→-60px; dashes at y=159 w=40 h=4 gap=20)
        listOf(-60f, 0f, 60f, 120f, 180f, 240f, 300f).forEach { startX ->
            val ox = (startX - dashOffset) * scaleX
            if (ox + x(40f) > 0f && ox < size.width) {
                drawRoundRect(Color(0xFFECEFF1).copy(alpha = 0.5f),
                    Offset(ox, y(159f)), Size(x(40f), y(4f)), CornerRadius(r(2f)))
            }
        }

        // Car (floating — SVG: body x=68 y=118 w=130 h=34; cabin x=88 y=100 w=90 h=28)
        withTransform({ translate(0f, carFloat * scaleY) }) {
            drawRoundRect(Color(0xFF00897B), Offset(x(68f), y(118f)), Size(x(130f), y(34f)), CornerRadius(r(9f)))
            drawRoundRect(Color(0xFF00695C), Offset(x(88f), y(100f)), Size(x(90f),  y(28f)), CornerRadius(r(9f)))
            // Windows (SVG: x=94 y=105 w=30 h=17; x=130 y=105 w=30 h=17)
            drawRoundRect(Color(0xFFB2EBF2).copy(alpha = 0.85f), Offset(x(94f),  y(105f)), Size(x(30f), y(17f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFB2EBF2).copy(alpha = 0.85f), Offset(x(130f), y(105f)), Size(x(30f), y(17f)), CornerRadius(r(4f)))
            // Heads (SVG: cx=109 cy=112; cx=145 cy=112)
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(109f), y(112f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(145f), y(112f)))
            drawSmile(Offset(x(109f), y(114f)), r(3f), Color(0xFFE64A19))
            drawSmile(Offset(x(145f), y(114f)), r(3f), Color(0xFFE64A19))
            // Bumper + lights (SVG: bumper y=144; front x=192 y=124 w=10 h=8; tail x=68 y=124 w=8 h=8)
            drawRoundRect(Color(0xFF004D40), Offset(x(68f),  y(144f)), Size(x(130f), y(8f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(192f), y(124f)), Size(x(10f),  y(8f)), CornerRadius(r(3f)))
            drawRoundRect(Color(0xFFEF5350), Offset(x(68f),  y(124f)), Size(x(8f),   y(8f)), CornerRadius(r(3f)))
            // Wheels (SVG: cx=98 cy=152 r=12; cx=170 cy=152 r=12; + cross only)
            drawWheelCross(Offset(x(98f),  y(152f)), r(12f), r(7f), r(3f), wheelAngle,
                Color(0xFF263238), Color(0xFF546E7A), Color(0xFFB0BEC5))
            drawWheelCross(Offset(x(170f), y(152f)), r(12f), r(7f), r(3f), wheelAngle,
                Color(0xFF263238), Color(0xFF546E7A), Color(0xFFB0BEC5))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared drawing helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Wheel with 8 spokes (+ and × cross) matching Slide 1 SVG:
 * outer circle → 4 full-diameter lines forming + and × → middle ring → hub.
 */
private fun DrawScope.drawWheel8Spoke(
    center: Offset,
    outerRadius: Float,
    middleRadius: Float,
    hubRadius: Float,
    angleDeg: Float,
    outerColor: Color,
    middleColor: Color,
    hubColor: Color,
) {
    drawCircle(outerColor, radius = outerRadius, center = center)
    // + cross (thick, stroke-width ≈ 2 * scale)
    val sw2 = outerRadius * 0.14f
    val sw15 = outerRadius * 0.10f
    repeat(2) { i ->
        val a = Math.toRadians((angleDeg + i * 90.0))
        val dx = outerRadius * cos(a).toFloat()
        val dy = outerRadius * sin(a).toFloat()
        drawLine(outerColor, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = sw2, cap = StrokeCap.Round)
    }
    // × cross (thinner)
    repeat(2) { i ->
        val a = Math.toRadians((angleDeg + 45.0 + i * 90.0))
        val dx = outerRadius * cos(a).toFloat()
        val dy = outerRadius * sin(a).toFloat()
        drawLine(outerColor, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = sw15, cap = StrokeCap.Round)
    }
    drawCircle(middleColor, radius = middleRadius, center = center)
    drawCircle(hubColor,    radius = hubRadius,    center = center)
}

/**
 * Wheel with + cross only (Slides 2 & 3 SVG: just 2 perpendicular lines).
 */
private fun DrawScope.drawWheelCross(
    center: Offset,
    outerRadius: Float,
    middleRadius: Float,
    hubRadius: Float,
    angleDeg: Float,
    outerColor: Color,
    middleColor: Color,
    hubColor: Color,
) {
    drawCircle(outerColor, radius = outerRadius, center = center)
    val sw = outerRadius * 0.15f
    repeat(2) { i ->
        val a = Math.toRadians((angleDeg + i * 90.0))
        val dx = outerRadius * cos(a).toFloat()
        val dy = outerRadius * sin(a).toFloat()
        drawLine(outerColor, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = sw, cap = StrokeCap.Round)
    }
    drawCircle(middleColor, radius = middleRadius, center = center)
    drawCircle(hubColor,    radius = hubRadius,    center = center)
}

/** Smile arc: path M(cx-r, cy) Q(cx, cy+arc) (cx+r, cy). */
private fun DrawScope.drawSmile(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - radius, center.y)
        quadraticBezierTo(center.x, center.y + radius * 1f, center.x + radius, center.y)
    }
    drawPath(path, color, style = Stroke(width = radius * 0.4f, cap = StrokeCap.Round))
}

/** 4-pointed sparkle: two crossing lines + circle. */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color, alpha: Float) {
    val c   = color.copy(alpha = alpha)
    val arm = radius * 2.2f
    val sw  = radius * 0.55f
    drawLine(c, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), strokeWidth = sw, cap = StrokeCap.Round)
    drawCircle(c, radius = radius, center = center)
}

package com.example.hop.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
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
    val carY       by inf.animateFloat(0f,   -6f,  infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), "carY")
    val wheelAngle by inf.animateFloat(0f,  360f,  infiniteRepeatable(tween(1200, easing = LinearEasing)),                           "wheel")
    val sunScale   by inf.animateFloat(1f,  1.07f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "sun")
    val cloudX     by inf.animateFloat(0f,   8f,   infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "cloud")
    val leafAngle  by inf.animateFloat(-10f, 10f,  infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "leaf")

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 280f
        val scaleY = size.height / 200f
        val scaleR = minOf(scaleX, scaleY)
        fun x(v: Float) = v * scaleX
        fun y(v: Float) = v * scaleY
        fun r(v: Float) = v * scaleR

        // Sky
        drawRect(Color(0xFFE8F5E9))

        // Sun (pulsing)
        withTransform({ scale(sunScale, sunScale, Offset(x(240f), y(38f))) }) {
            drawCircle(Color(0xFFFFF9C4), radius = r(22f), center = Offset(x(240f), y(38f)))
            drawCircle(Color(0xFFFFF176), radius = r(15f), center = Offset(x(240f), y(38f)))
        }

        // Clouds
        withTransform({ translate(cloudX * scaleX, 0f) }) {
            drawOval(Color.White.copy(alpha = 0.85f), Offset(x(32f), y(23f)), Size(x(56f), y(24f)))
            drawOval(Color.White.copy(alpha = 0.85f), Offset(x(62f), y(19f)), Size(x(36f), y(22f)))
            drawOval(Color.White.copy(alpha = 0.85f), Offset(x(28f), y(23f)), Size(x(28f), y(18f)))
        }
        withTransform({ translate(-cloudX * scaleX * 0.5f, 0f) }) {
            drawOval(Color.White.copy(alpha = 0.7f), Offset(x(138f), y(18f)), Size(x(44f), y(20f)))
            drawOval(Color.White.copy(alpha = 0.7f), Offset(x(161f), y(15f)), Size(x(28f), y(18f)))
            drawOval(Color.White.copy(alpha = 0.7f), Offset(x(136f), y(18f)), Size(x(24f), y(16f)))
        }

        // Ground + road
        drawRect(Color(0xFFA5D6A7), Offset(0f, y(155f)), Size(size.width, y(45f)))
        drawRect(Color(0xFF78909C), Offset(0f, y(143f)), Size(size.width, y(22f)))
        listOf(20f, 75f, 130f, 185f, 240f).forEach { dx ->
            drawRoundRect(Color(0xFFECEFF1).copy(alpha = 0.7f), Offset(x(dx), y(152f)), Size(x(30f), y(4f)), CornerRadius(r(2f)))
        }

        // Trees — left side
        drawRect(Color(0xFF6D4C41), Offset(x(18f), y(110f)), Size(x(7f), y(38f)))
        drawOval(Color(0xFF388E3C), Offset(x(3f),  y(76f)),  Size(x(36f), y(44f)))
        drawOval(Color(0xFF43A047), Offset(x(8f),  y(74f)),  Size(x(26f), y(32f)))
        drawRect(Color(0xFF6D4C41), Offset(x(45f), y(120f)), Size(x(6f),  y(26f)))
        drawOval(Color(0xFF2E7D32), Offset(x(34f), y(92f)),  Size(x(28f), y(36f)))
        drawOval(Color(0xFF388E3C), Offset(x(38f), y(92f)),  Size(x(20f), y(24f)))

        // Trees — right side
        drawRect(Color(0xFF6D4C41), Offset(x(232f), y(112f)), Size(x(7f),  y(36f)))
        drawOval(Color(0xFF388E3C), Offset(x(217f), y(78f)),  Size(x(36f), y(44f)))
        drawOval(Color(0xFF43A047), Offset(x(222f), y(76f)),  Size(x(26f), y(32f)))
        drawRect(Color(0xFF6D4C41), Offset(x(256f), y(118f)), Size(x(6f),  y(30f)))
        drawOval(Color(0xFF2E7D32), Offset(x(245f), y(90f)),  Size(x(28f), y(36f)))

        // Car (floating up/down)
        withTransform({ translate(0f, carY * scaleY) }) {
            // Car body + cabin
            drawRoundRect(Color(0xFF26C6DA), Offset(x(62f), y(118f)), Size(x(130f), y(34f)), CornerRadius(r(8f)))
            drawRoundRect(Color(0xFF00ACC1), Offset(x(82f), y(100f)), Size(x(90f),  y(28f)), CornerRadius(r(8f)))
            // Windows
            drawRoundRect(Color(0xFFE0F7FA).copy(alpha = 0.9f), Offset(x(88f),  y(105f)), Size(x(32f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE0F7FA).copy(alpha = 0.9f), Offset(x(126f), y(105f)), Size(x(32f), y(18f)), CornerRadius(r(4f)))
            // Passenger heads
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(104f), y(108f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(142f), y(108f)))
            // Smiles
            drawSmile(Offset(x(104f), y(109f)), r(5f), Color(0xFFE64A19))
            drawSmile(Offset(x(142f), y(109f)), r(5f), Color(0xFFE64A19))
            // Bumper, lights
            drawRoundRect(Color(0xFF0097A7), Offset(x(62f),  y(144f)), Size(x(130f), y(8f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(184f), y(125f)), Size(x(10f),  y(8f)), CornerRadius(r(3f)))
            drawRoundRect(Color(0xFFEF9A9A), Offset(x(64f),  y(125f)), Size(x(8f),   y(8f)), CornerRadius(r(3f)))
            // Wheels (spinning)
            drawWheel(Offset(x(92f),  y(148f)), r(14f), r(7f), r(3f), wheelAngle, Color(0xFF37474F), Color(0xFF78909C), Color(0xFFB0BEC5))
            drawWheel(Offset(x(178f), y(148f)), r(14f), r(7f), r(3f), wheelAngle, Color(0xFF37474F), Color(0xFF78909C), Color(0xFFB0BEC5))
        }

        // Floating leaves
        withTransform({ rotate(leafAngle - 10f, Offset(x(30f), y(60f))) }) {
            drawOval(Color(0xFF66BB6A), Offset(x(20f), y(54f)), Size(x(20f), y(12f)))
        }
        withTransform({ rotate(-leafAngle * 0.8f + 20f, Offset(x(240f), y(70f))) }) {
            drawOval(Color(0xFF81C784), Offset(x(231f), y(65f)), Size(x(18f), y(10f)))
        }
        withTransform({ rotate(leafAngle + 5f, Offset(x(255f), y(50f))) }) {
            drawOval(Color(0xFFA5D6A7), Offset(x(248f), y(46f)), Size(x(14f), y(8f)))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 2 — "Every seat filled matters."
// CO₂ cloud fading left, pulsing leaf centre, sparkles, car with 3 passengers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Slide2Illustration(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "s2")
    val carBob     by inf.animateFloat(0f,   -5f,   infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "bob")
    val wheelAngle by inf.animateFloat(0f,  360f,   infiniteRepeatable(tween(1000, easing = LinearEasing)),                           "whl")
    val leafScale  by inf.animateFloat(1f,   1.15f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "leaf")
    val smokeOff   by inf.animateFloat(0f,  30f,    infiniteRepeatable(tween(2500, easing = LinearEasing)),                           "smoke")
    val smokeAlpha by inf.animateFloat(0.7f, 0f,    infiniteRepeatable(tween(2500, easing = LinearEasing)),                           "smkAlpha")
    val sp1        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "sp1")
    val sp2        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 600), "sp2")
    val sp3        by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 1200), "sp3")

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 280f
        val scaleY = size.height / 200f
        val scaleR = minOf(scaleX, scaleY)
        fun x(v: Float) = v * scaleX
        fun y(v: Float) = v * scaleY
        fun r(v: Float) = v * scaleR

        // Background
        drawRect(Color(0xFFF1F8E9))

        // Ground + road
        drawRect(Color(0xFFC8E6C9), Offset(0f, y(155f)), Size(size.width, y(45f)))
        drawRect(Color(0xFF90A4AE), Offset(0f, y(143f)), Size(size.width, y(18f)))
        listOf(10f, 60f, 115f, 170f, 225f).forEach { dx ->
            drawRoundRect(Color.White.copy(alpha = 0.6f), Offset(x(dx), y(150f)), Size(x(25f), y(3f)), CornerRadius(r(1.5f)))
        }

        // CO₂ cloud (fading out, left side)
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), Offset(x(10f),  y(46f)), Size(x(56f), y(32f)))
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), Offset(x(6f),   y(46f)), Size(x(32f), y(24f)))
        drawOval(Color(0xFFCFD8DC).copy(alpha = 0.55f), Offset(x(38f),  y(44f)), Size(x(28f), y(22f)))

        // Rising smoke particles
        listOf(Triple(x(38f), y(38f), 12f), Triple(x(44f), y(34f), 10f), Triple(x(32f), y(36f), 8f)).forEach { (cx, cy, rr) ->
            drawCircle(Color(0xFFB0BEC5).copy(alpha = smokeAlpha * 0.5f), radius = r(rr / 2f), center = Offset(cx, cy - smokeOff * scaleY))
        }

        // Big leaf  (pulsing)
        withTransform({ scale(leafScale, leafScale, Offset(x(140f), y(48f))) }) {
            // Outer leaf
            val leafPath = Path().apply {
                moveTo(x(102f), y(48f))
                cubicTo(x(102f), y(24f), x(178f), y(24f), x(178f), y(48f))
                cubicTo(x(178f), y(72f), x(102f), y(72f), x(102f), y(48f))
                close()
            }
            drawPath(leafPath, Color(0xFF66BB6A))
            val innerLeafPath = Path().apply {
                moveTo(x(112f), y(48f))
                cubicTo(x(112f), y(32f), x(168f), y(32f), x(168f), y(48f))
                cubicTo(x(168f), y(64f), x(112f), y(64f), x(112f), y(48f))
                close()
            }
            drawPath(innerLeafPath, Color(0xFF81C784))
            // Veins
            val veinStroke = Stroke(width = r(1.5f), cap = StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(110f), y(48f)), Offset(x(170f), y(48f)), r(1.5f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(140f), y(30f)), Offset(x(140f), y(66f)), r(1.5f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(125f), y(38f)), Offset(x(140f), y(48f)), r(1f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(155f), y(38f)), Offset(x(140f), y(48f)), r(1f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(122f), y(58f)), Offset(x(140f), y(48f)), r(1f), StrokeCap.Round)
            drawLine(Color(0xFF388E3C), Offset(x(158f), y(58f)), Offset(x(140f), y(48f)), r(1f), StrokeCap.Round)
        }

        // Sparkles
        drawSparkle(Offset(x(200f), y(35f)), r(4f), Color(0xFFFFF176), sp1)
        drawSparkle(Offset(x(220f), y(55f)), r(3f), Color(0xFFA5D6A7), sp2)
        drawSparkle(Offset(x(178f), y(28f)), r(3f), Color(0xFF80DEEA), sp3)

        // Car (bobbing)
        withTransform({ translate(0f, carBob * scaleY) }) {
            // Body + cabin
            drawRoundRect(Color(0xFF42A5F5), Offset(x(52f), y(116f)), Size(x(148f), y(36f)), CornerRadius(r(9f)))
            drawRoundRect(Color(0xFF1E88E5), Offset(x(74f), y(97f)),  Size(x(104f), y(30f)), CornerRadius(r(9f)))
            // 3 windows
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(80f),  y(103f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(112f), y(103f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFE3F2FD).copy(alpha = 0.92f), Offset(x(144f), y(103f)), Size(x(26f), y(18f)), CornerRadius(r(4f)))
            // 3 passenger heads
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(93f),  y(109f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(125f), y(109f)))
            drawCircle(Color(0xFFCE93D8), radius = r(7f), center = Offset(x(157f), y(109f)))
            drawSmile(Offset(x(93f),  y(110f)), r(5f), Color(0xFFBF360C))
            drawSmile(Offset(x(125f), y(110f)), r(5f), Color(0xFFBF360C))
            drawSmile(Offset(x(157f), y(110f)), r(5f), Color(0xFF6A1B9A))
            // Bumper + headlight
            drawRoundRect(Color(0xFF1565C0), Offset(x(52f),  y(144f)), Size(x(148f), y(8f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(194f), y(124f)), Size(x(8f),   y(7f)), CornerRadius(r(3f)))
            // Wheels
            drawWheel(Offset(x(90f),  y(150f)), r(13f), r(6f), r(3f), wheelAngle, Color(0xFF37474F), Color(0xFF78909C), Color(0xFFB0BEC5))
            drawWheel(Offset(x(166f), y(150f)), r(13f), r(6f), r(3f), wheelAngle, Color(0xFF37474F), Color(0xFF78909C), Color(0xFFB0BEC5))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 3 — "Move together. Live lighter."
// Night drive: moon, twinkling stars, silhouette hills + trees, scrolling road
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Slide3Illustration(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "s3")
    val carFloat   by inf.animateFloat(0f,  -4f,  infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "cf")
    val wheelAngle by inf.animateFloat(0f, 360f,  infiniteRepeatable(tween(1100, easing = LinearEasing)),                           "whl")
    val dashOffset by inf.animateFloat(0f,  60f,  infiniteRepeatable(tween(1800, easing = LinearEasing)),                           "dash")
    val moonGlow   by inf.animateFloat(0.9f, 1f,  infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "moon")
    val s1         by inf.animateFloat(0.2f, 1f,  infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "s1")
    val s2         by inf.animateFloat(0.2f, 1f,  infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 400),  "s2")
    val s3         by inf.animateFloat(0.2f, 1f,  infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 800),  "s3")
    val s4         by inf.animateFloat(0.2f, 1f,  infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 1200), "s4")
    val s5         by inf.animateFloat(0.2f, 1f,  infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse, delayMillis = 1600), "s5")

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

        // Horizon glow
        withTransform({ }) {
            drawOval(Color(0xFF00796B).copy(alpha = 0.35f), Offset(x(140f) - x(160f), y(140f) - y(40f)), Size(x(320f), y(80f)))
        }

        // Moon
        withTransform({ }) {
            drawCircle(Color(0xFFFFF9C4).copy(alpha = 0.15f * moonGlow), radius = r(24f), center = Offset(x(230f), y(40f)))
            drawCircle(Color(0xFFFFF9C4).copy(alpha = 0.20f * moonGlow), radius = r(18f), center = Offset(x(230f), y(40f)))
            drawCircle(Color(0xFFFFFDE7),                                 radius = r(13f), center = Offset(x(230f), y(40f)))
        }

        // Stars
        data class Star(val cx: Float, val cy: Float, val rad: Float, val anim: Float)
        listOf(
            Star(30f, 22f, 2f,   s1), Star(68f, 14f, 1.5f, s2), Star(110f, 30f, 2f, s3),
            Star(155f, 18f, 1.5f, s4), Star(185f, 35f, 2f, s5), Star(50f, 45f, 1.5f, s1),
            Star(90f, 52f, 1f, s3), Star(200f, 20f, 1.5f, s2), Star(256f, 62f, 1.5f, s4),
        ).forEach { star ->
            drawCircle(Color.White.copy(alpha = star.anim), radius = r(star.rad), center = Offset(x(star.cx), y(star.cy)))
        }

        // Hill silhouettes
        drawOval(Color(0xFF1B5E20), Offset(x(40f) - x(55f), y(145f) - y(35f)), Size(x(110f), y(70f)))
        drawOval(Color(0xFF1B5E20), Offset(x(240f) - x(55f), y(148f) - y(30f)), Size(x(110f), y(60f)))
        drawOval(Color(0xFF1B5E20), Offset(x(140f) - x(80f), y(152f) - y(28f)), Size(x(160f), y(56f)))

        // Silhouette trees — left
        drawRect(Color(0xFF0D3B0D), Offset(x(12f), y(110f)), Size(x(6f), y(30f)))
        val treeL = Path().apply {
            moveTo(x(15f), y(85f)); lineTo(x(3f), y(118f)); lineTo(x(27f), y(118f)); close()
        }
        drawPath(treeL, Color(0xFF0D3B0D))
        val treeL2 = Path().apply {
            moveTo(x(15f), y(95f)); lineTo(x(5f), y(118f)); lineTo(x(25f), y(118f)); close()
        }
        drawPath(treeL2, Color(0xFF1B5E20))

        // Silhouette trees — right
        drawRect(Color(0xFF0D3B0D), Offset(x(255f), y(112f)), Size(x(6f), y(28f)))
        val treeR = Path().apply {
            moveTo(x(258f), y(88f)); lineTo(x(246f), y(120f)); lineTo(x(270f), y(120f)); close()
        }
        drawPath(treeR, Color(0xFF0D3B0D))
        val treeR2 = Path().apply {
            moveTo(x(258f), y(98f)); lineTo(x(248f), y(120f)); lineTo(x(268f), y(120f)); close()
        }
        drawPath(treeR2, Color(0xFF1B5E20))

        // Road
        drawRect(Color(0xFF37474F), Offset(0f, y(148f)), Size(size.width, y(28f)))
        drawRect(Color(0xFF455A64).copy(alpha = 0.6f), Offset(0f, y(162f)), Size(size.width, y(14f)))

        // Animated dashes — clip so they don't bleed outside canvas
        listOf(-60f, 0f, 60f, 120f, 180f, 240f, 300f).forEach { startX ->
            val ox = (startX - dashOffset) * scaleX
            if (ox + x(40f) > 0f && ox < size.width) {
                drawRoundRect(
                    Color(0xFFECEFF1).copy(alpha = 0.5f),
                    Offset(ox, y(159f)),
                    Size(x(40f), y(4f)),
                    CornerRadius(r(2f)),
                )
            }
        }

        // Headlight glow
        drawOval(Color(0xFFFFF9C4).copy(alpha = 0.18f), Offset(x(172f), y(144f)), Size(x(76f), y(16f)))

        // Car (floating)
        withTransform({ translate(0f, carFloat * scaleY) }) {
            // Body + cabin
            drawRoundRect(Color(0xFF00897B), Offset(x(68f), y(120f)), Size(x(130f), y(34f)), CornerRadius(r(9f)))
            drawRoundRect(Color(0xFF00695C), Offset(x(88f), y(103f)), Size(x(90f),  y(28f)), CornerRadius(r(9f)))
            // Windows
            drawRoundRect(Color(0xFFB2EBF2).copy(alpha = 0.85f), Offset(x(94f),  y(108f)), Size(x(30f), y(17f)), CornerRadius(r(4f)))
            drawRoundRect(Color(0xFFB2EBF2).copy(alpha = 0.85f), Offset(x(130f), y(108f)), Size(x(30f), y(17f)), CornerRadius(r(4f)))
            // Passengers
            drawCircle(Color(0xFFFFCC80), radius = r(7f), center = Offset(x(109f), y(114f)))
            drawCircle(Color(0xFFFFAB91), radius = r(7f), center = Offset(x(145f), y(114f)))
            drawSmile(Offset(x(109f), y(115f)), r(5f), Color(0xFFE64A19))
            drawSmile(Offset(x(145f), y(115f)), r(5f), Color(0xFFE64A19))
            // Bumper
            drawRoundRect(Color(0xFF004D40), Offset(x(68f), y(146f)), Size(x(130f), y(8f)), CornerRadius(r(4f)))
            // Headlight (front)
            drawRoundRect(Color(0xFFFFF9C4), Offset(x(192f), y(126f)), Size(x(10f), y(8f)), CornerRadius(r(3f)))
            drawRoundRect(Color(0xFFFFEE58), Offset(x(194f), y(128f)), Size(x(6f),  y(4f)), CornerRadius(r(2f)))
            // Taillight (back)
            drawRoundRect(Color(0xFFEF5350), Offset(x(68f), y(126f)), Size(x(8f), y(8f)), CornerRadius(r(3f)))
            // Wheels
            drawWheel(Offset(x(98f),  y(152f)), r(12f), r(5f), r(2.5f), wheelAngle, Color(0xFF263238), Color(0xFF546E7A), Color(0xFF78909C))
            drawWheel(Offset(x(170f), y(152f)), r(12f), r(5f), r(2.5f), wheelAngle, Color(0xFF263238), Color(0xFF546E7A), Color(0xFF78909C))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared drawing helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Wheel with visible spokes so rotation is perceptible. */
private fun DrawScope.drawWheel(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    hubRadius: Float,
    angleDeg: Float,
    outerColor: Color,
    innerColor: Color,
    hubColor: Color,
) {
    drawCircle(outerColor, radius = outerRadius, center = center)
    // Spokes
    val spokeCount = 4
    val strokeW = (outerRadius - innerRadius) * 0.35f
    repeat(spokeCount) { i ->
        val angle = Math.toRadians((angleDeg + i * (360f / spokeCount)).toDouble())
        val endX = center.x + innerRadius * cos(angle).toFloat()
        val endY = center.y + innerRadius * sin(angle).toFloat()
        drawLine(innerColor, center, Offset(endX, endY), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
    drawCircle(innerColor, radius = innerRadius, center = center)
    drawCircle(hubColor,   radius = hubRadius,   center = center)
}

/** Simple upward arc "smile" below a passenger head. */
private fun DrawScope.drawSmile(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - radius,       center.y)
        quadraticBezierTo(center.x, center.y + radius * 0.8f, center.x + radius, center.y)
    }
    drawPath(path, color, style = Stroke(width = radius * 0.3f, cap = StrokeCap.Round))
}

/** 4-pointed sparkle (cross of 2 lines + small inner diamond). */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color, alpha: Float) {
    val c = color.copy(alpha = alpha)
    val arm = radius * 2.2f
    drawLine(c, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), strokeWidth = radius * 0.6f, cap = StrokeCap.Round)
    drawLine(c, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), strokeWidth = radius * 0.6f, cap = StrokeCap.Round)
    drawCircle(c, radius = radius, center = center)
}

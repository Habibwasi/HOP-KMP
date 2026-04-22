package com.example.hop.ui.screens.shared

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Colour palette ────────────────────────────────────────────────────────────
private val SkyBlue     = Color(0xFFE3F2FD)
private val SunOuter    = Color(0xFFFFF9C4)
private val SunInner    = Color(0xFFFFF176)
private val BirdGrey    = Color(0xFF78909C)
private val BldBg1      = Color(0xFFB2DFDB)
private val BldBg2      = Color(0xFFA5D6A7)
private val BldMid1     = Color(0xFF80CBC4)
private val BldMid2     = Color(0xFF4DB6AC)
private val Ground1     = Color(0xFFA5D6A7)
private val GroundEll   = Color(0xFF81C784)
private val RoadColor   = Color(0xFF546E7A)
private val DashColor   = Color(0xFFECEFF1)
private val TrunkColor  = Color(0xFF5D4037)
private val Leaf1       = Color(0xFF388E3C)
private val Leaf2       = Color(0xFF43A047)
private val Leaf3       = Color(0xFF2E7D32)
private val CarBody1    = Color(0xFF26C6DA)
private val CarBody2    = Color(0xFF00ACC1)
private val CarWindow   = Color(0xFFE0F7FA)
private val CarStripe   = Color(0xFF0097A7)
private val CarLightF   = Color(0xFFFFF9C4)
private val CarLightR   = Color(0xFFEF9A9A)
private val PassL       = Color(0xFFFFCC80)
private val PassR       = Color(0xFFFFAB91)
private val SmileColor  = Color(0xFFE64A19)
private val WheelOut    = Color(0xFF37474F)
private val WheelMid    = Color(0xFF78909C)
private val WheelHub    = Color(0xFFB0BEC5)
private val WheelSpk    = Color(0xFF546E7A)
private val Grass1      = Color(0xFF66BB6A)
private val Grass2      = Color(0xFF81C784)
private val FlowerYel   = Color(0xFFFFF9C4)
private val Overlay     = Color(0xFF1B4332)
private val LogoWhite   = Color.White
private val LogoAccent  = Color(0xFF52B788)
private val Tag1Color   = Color(0xFF95D5B2)
private val Tag2Color   = Color(0xFF52B788)
private val DotColor    = Color(0xFF52B788)

// SVG reference dimensions (content area)
private const val VW = 320f
private const val VH = 703f

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * Animated brand splash screen shown for 3 seconds on cold start.
 * Calls [onComplete] after the delay, allowing the caller to navigate forward.
 */
@Composable
fun SplashRoute(onComplete: () -> Unit) {
    SplashScreen(onComplete = onComplete)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun SplashScreen(onComplete: () -> Unit = {}) {

    // ── 3-second timer ────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(3_000)
        onComplete()
    }

    // ── Infinite animations ───────────────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "splash")

    val cloudLX by inf.animateFloat(
        0f, 18f,
        infiniteRepeatable(tween(6_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cloudL",
    )
    val cloudRX by inf.animateFloat(
        0f, -14f,
        infiniteRepeatable(tween(7_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cloudR",
    )
    val carBob by inf.animateFloat(
        0f, -3f,
        infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "carBob",
    )
    val wheelAngle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "wheel",
    )
    val roadOff by inf.animateFloat(
        0f, -80f,
        infiniteRepeatable(tween(1_000, easing = LinearEasing)),
        label = "road",
    )
    val treeLAngle by inf.animateFloat(
        -2f, 2f,
        infiniteRepeatable(tween(3_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "treeL",
    )
    val treeRAngle by inf.animateFloat(
        2f, -2f,
        infiniteRepeatable(tween(3_400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "treeR",
    )
    val sunScale by inf.animateFloat(
        1f, 1.05f,
        infiniteRepeatable(tween(4_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sun",
    )
    val dotScale by inf.animateFloat(
        1f, 1.4f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dotScale",
    )
    val dotAlpha by inf.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dotAlpha",
    )
    val birdX by inf.animateFloat(
        0f, 60f,
        infiniteRepeatable(tween(5_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "birdX",
    )

    // ── Entry (one-shot) animations ───────────────────────────────────────────
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val logoAlpha by animateFloatAsState(
        if (started) 1f else 0f,
        tween(1_200, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "logoAlpha",
    )
    val logoOffY by animateFloatAsState(
        if (started) 0f else 12f,
        tween(1_200, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "logoOffY",
    )
    val tag1Alpha by animateFloatAsState(
        if (started) 1f else 0f,
        tween(1_000, delayMillis = 1_200, easing = LinearEasing),
        label = "tag1Alpha",
    )
    val tag2Alpha by animateFloatAsState(
        if (started) 1f else 0f,
        tween(1_000, delayMillis = 1_600, easing = LinearEasing),
        label = "tag2Alpha",
    )

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val sw = size.width
        val sh = size.height
        val sx = sw / VW
        val sy = sh / VH

        // Scale helpers: SVG coordinate → canvas pixel
        fun f(x: Float) = x * sx
        fun g(y: Float) = y * sy

        // ── Sky ───────────────────────────────────────────────────────────────
        drawRect(SkyBlue, size = size)

        // ── Sun ───────────────────────────────────────────────────────────────
        drawScaledSun(f(260f), g(90f), sx, sunScale)

        // ── Bird ──────────────────────────────────────────────────────────────
        translate(f(birdX), 0f) {
            drawBird(sx, sy)
        }

        // ── Cloud left ────────────────────────────────────────────────────────
        translate(f(cloudLX), 0f) {
            drawOval(Color.White.copy(alpha = 0.92f), Offset(f(32f), g(50f)),  Size(f(76f), g(36f)))
            drawOval(Color.White.copy(alpha = 0.92f), Offset(f(74f), g(44f)),  Size(f(52f), g(32f)))
            drawOval(Color.White.copy(alpha = 0.92f), Offset(f(26f), g(51f)),  Size(f(40f), g(26f)))
        }

        // ── Cloud right ───────────────────────────────────────────────────────
        translate(f(cloudRX), 0f) {
            drawOval(Color.White.copy(alpha = 0.75f), Offset(f(168f), g(37f)), Size(f(64f), g(30f)))
            drawOval(Color.White.copy(alpha = 0.75f), Offset(f(200f), g(31f)), Size(f(44f), g(26f)))
            drawOval(Color.White.copy(alpha = 0.75f), Offset(f(162f), g(40f)), Size(f(32f), g(20f)))
        }

        // ── Background buildings ──────────────────────────────────────────────
        drawRoundRect(BldBg1.copy(alpha = 0.55f), Offset(f(10f),  g(210f)), Size(f(28f), g(120f)), CornerRadius(f(3f)))
        drawRoundRect(BldBg2.copy(alpha = 0.50f), Offset(f(42f),  g(195f)), Size(f(22f), g(135f)), CornerRadius(f(3f)))
        drawRoundRect(BldBg1.copy(alpha = 0.50f), Offset(f(68f),  g(220f)), Size(f(18f), g(110f)), CornerRadius(f(3f)))
        drawRoundRect(BldBg2.copy(alpha = 0.50f), Offset(f(240f), g(200f)), Size(f(24f), g(130f)), CornerRadius(f(3f)))
        drawRoundRect(BldBg1.copy(alpha = 0.50f), Offset(f(268f), g(215f)), Size(f(30f), g(115f)), CornerRadius(f(3f)))
        drawRoundRect(BldBg2.copy(alpha = 0.50f), Offset(f(292f), g(205f)), Size(f(22f), g(125f)), CornerRadius(f(3f)))
        // windows
        for ((wx, wy) in listOf(16f to 222f, 26f to 222f, 16f to 234f, 246f to 212f, 256f to 212f)) {
            drawRoundRect(Color.White.copy(alpha = 0.45f), Offset(f(wx), g(wy)), Size(f(6f), g(5f)), CornerRadius(f(1f)))
        }

        // ── Mid buildings ─────────────────────────────────────────────────────
        drawRoundRect(BldMid1, Offset(f(0f),   g(258f)), Size(f(40f), g(112f)), CornerRadius(f(4f)))
        drawRoundRect(BldMid2, Offset(f(44f),  g(246f)), Size(f(34f), g(124f)), CornerRadius(f(4f)))
        drawRoundRect(BldMid1, Offset(f(82f),  g(266f)), Size(f(28f), g(104f)), CornerRadius(f(4f)))
        drawRoundRect(BldMid2, Offset(f(210f), g(253f)), Size(f(36f), g(117f)), CornerRadius(f(4f)))
        drawRoundRect(BldMid1, Offset(f(250f), g(263f)), Size(f(30f), g(107f)), CornerRadius(f(4f)))
        drawRoundRect(BldMid2, Offset(f(284f), g(248f)), Size(f(36f), g(122f)), CornerRadius(f(4f)))
        // windows
        for ((wx, wy) in listOf(6f to 270f, 18f to 270f, 6f to 284f, 18f to 284f, 50f to 258f, 64f to 258f, 216f to 264f, 230f to 264f)) {
            drawRoundRect(Color.White.copy(alpha = 0.55f), Offset(f(wx), g(wy)), Size(f(8f), g(7f)), CornerRadius(f(1f)))
        }

        // ── Ground ────────────────────────────────────────────────────────────
        drawRect(Ground1, Offset(0f, g(358f)), Size(sw, sh - g(358f)))
        drawOval(GroundEll, Offset(f(-60f), g(322f)), Size(f(440f), g(76f)))

        // ── Road ──────────────────────────────────────────────────────────────
        drawRect(RoadColor, Offset(0f, g(390f)), Size(sw, g(36f)))

        // scrolling centre dashes (clipped to road band)
        clipRect(0f, g(390f), sw, g(426f)) {
            for (i in -1..6) {
                val dx = f(i * 80f) + f(roadOff)
                drawRoundRect(
                    DashColor.copy(alpha = 0.65f),
                    Offset(dx, g(405f)),
                    Size(f(44f), g(5f)),
                    CornerRadius(g(2.5f)),
                )
            }
        }

        // ── Trees left ────────────────────────────────────────────────────────
        rotate(treeLAngle, Offset(f(18f), g(370f))) {
            drawRect(TrunkColor, Offset(f(14f),  g(338f)), Size(f(9f),  g(54f)))
            drawOval(Leaf1,      Offset(f(-6f),  g(292f)), Size(f(48f), g(56f)))
            drawOval(Leaf2,      Offset(f(1f),   g(288f)), Size(f(34f), g(40f)))
            drawRect(TrunkColor, Offset(f(50f),  g(350f)), Size(f(8f),  g(42f)))
            drawOval(Leaf3,      Offset(f(35f),  g(311f)), Size(f(38f), g(46f)))
            drawOval(Leaf1,      Offset(f(40f),  g(307f)), Size(f(28f), g(34f)))
        }

        // ── Trees right ───────────────────────────────────────────────────────
        rotate(treeRAngle, Offset(f(294f), g(370f))) {
            drawRect(TrunkColor, Offset(f(290f), g(340f)), Size(f(9f),  g(52f)))
            drawOval(Leaf1,      Offset(f(270f), g(294f)), Size(f(48f), g(56f)))
            drawOval(Leaf2,      Offset(f(277f), g(290f)), Size(f(34f), g(40f)))
            drawRect(TrunkColor, Offset(f(257f), g(348f)), Size(f(8f),  g(44f)))
            drawOval(Leaf3,      Offset(f(242f), g(309f)), Size(f(38f), g(46f)))
            drawOval(Leaf1,      Offset(f(247f), g(305f)), Size(f(28f), g(34f)))
        }

        // ── Car (with bob) ────────────────────────────────────────────────────
        translate(0f, g(carBob)) {
            drawCar(sx, sy, wheelAngle)
        }

        // ── Foreground grass ──────────────────────────────────────────────────
        drawRect(Grass1, Offset(0f, g(422f)), Size(sw, sh - g(422f)))
        drawOval(Grass2, Offset(f(-60f), g(400f)), Size(f(440f), g(44f)))

        // ── Small flowers ─────────────────────────────────────────────────────
        drawCircle(FlowerYel,               radius = f(4f), center = Offset(f(40f),  g(436f)))
        drawCircle(FlowerYel,               radius = f(3f), center = Offset(f(280f), g(440f)))
        drawCircle(Color.White.copy(alpha = 0.7f), radius = f(3f), center = Offset(f(72f),  g(446f)))
        drawCircle(Color.White.copy(alpha = 0.7f), radius = f(4f), center = Offset(f(248f), g(432f)))

        // ── Dark overlay (logo card) ──────────────────────────────────────────
        drawRect(Overlay.copy(alpha = 0.94f), Offset(0f, g(472f)), Size(sw, sh - g(472f)))

        // ── Logo (fade + slide in) ────────────────────────────────────────────
        withTransform({ translate(0f, g(logoOffY)) }) {
            drawLogo(f = ::f, g = ::g, alpha = logoAlpha)
        }

        // ── Tagline 1 ─────────────────────────────────────────────────────────
        val tag1Style = TextStyle(
            color = Tag1Color.copy(alpha = tag1Alpha),
            fontSize = 11.sp,
            fontWeight = FontWeight.W300,
            letterSpacing = 2.sp,
        )
        val tag1 = textMeasurer.measure("SHARE THE RIDE", tag1Style)
        drawText(tag1, topLeft = Offset(f(160f) - tag1.size.width / 2f, g(578f)))

        // ── Tagline 2 ─────────────────────────────────────────────────────────
        val tag2Style = TextStyle(
            color = Tag2Color.copy(alpha = tag2Alpha),
            fontSize = 9.sp,
            fontWeight = FontWeight.W300,
            letterSpacing = 1.sp,
        )
        val tag2 = textMeasurer.measure("less carbon · more journey", tag2Style)
        drawText(tag2, topLeft = Offset(f(160f) - tag2.size.width / 2f, g(600f)))

        // ── Pulsing loading dot ───────────────────────────────────────────────
        drawCircle(DotColor.copy(alpha = dotAlpha), radius = f(4f) * dotScale, center = Offset(f(160f), g(648f)))
    }
}

// ── Private drawing helpers ───────────────────────────────────────────────────

private fun DrawScope.drawScaledSun(cx: Float, cy: Float, sx: Float, scale: Float) {
    val r1 = 38f * sx * scale
    val r2 = 26f * sx * scale
    val center = Offset(cx, cy)
    drawCircle(SunOuter, radius = r1, center = center)
    drawCircle(SunInner, radius = r2, center = center)
}

private fun DrawScope.drawBird(sx: Float, sy: Float) {
    fun f(x: Float) = x * sx
    fun g(y: Float) = y * sy

    val arc1 = Path().apply {
        moveTo(f(50f), g(120f))
        quadraticBezierTo(f(54f), g(116f), f(58f), g(120f))
    }
    val arc2 = Path().apply {
        moveTo(f(62f), g(118f))
        quadraticBezierTo(f(66f), g(114f), f(70f), g(118f))
    }
    val stroke = Stroke(f(1.5f), cap = StrokeCap.Round)
    drawPath(arc1, BirdGrey.copy(alpha = 0.6f), style = stroke)
    drawPath(arc2, BirdGrey.copy(alpha = 0.6f), style = stroke)
}

private fun DrawScope.drawCar(sx: Float, sy: Float, wheelAngle: Float) {
    fun f(x: Float) = x * sx
    fun g(y: Float) = y * sy

    // Body + roof
    drawRoundRect(CarBody1, Offset(f(88f),  g(356f)), Size(f(144f), g(38f)), CornerRadius(f(10f)))
    drawRoundRect(CarBody2, Offset(f(106f), g(333f)), Size(f(100f), g(34f)), CornerRadius(f(10f)))

    // Windows
    drawRoundRect(CarWindow.copy(alpha = 0.9f), Offset(f(114f), g(339f)), Size(f(36f), g(22f)), CornerRadius(f(5f)))
    drawRoundRect(CarWindow.copy(alpha = 0.9f), Offset(f(156f), g(339f)), Size(f(36f), g(22f)), CornerRadius(f(5f)))

    // Passengers
    drawCircle(PassL, radius = f(9f), center = Offset(f(132f), g(348f)))
    drawCircle(PassR, radius = f(9f), center = Offset(f(174f), g(348f)))

    // Smiles
    val smileStroke = Stroke(f(1.3f), cap = StrokeCap.Round)
    val smile1 = Path().apply {
        moveTo(f(129f), g(350f))
        quadraticBezierTo(f(132f), g(354f), f(135f), g(350f))
    }
    val smile2 = Path().apply {
        moveTo(f(171f), g(350f))
        quadraticBezierTo(f(174f), g(354f), f(177f), g(350f))
    }
    drawPath(smile1, SmileColor, style = smileStroke)
    drawPath(smile2, SmileColor, style = smileStroke)

    // Under-body stripe + lights
    drawRoundRect(CarStripe, Offset(f(88f),  g(386f)), Size(f(144f), g(8f)), CornerRadius(f(4f)))
    drawRoundRect(CarLightF, Offset(f(224f), g(364f)), Size(f(10f),  g(9f)), CornerRadius(f(3f)))
    drawRoundRect(CarLightR, Offset(f(90f),  g(364f)), Size(f(8f),   g(9f)), CornerRadius(f(3f)))

    // Left wheel
    rotate(wheelAngle, Offset(f(116f), g(394f))) {
        drawWheel(f(116f), g(394f), sx)
    }
    // Right wheel
    rotate(wheelAngle, Offset(f(204f), g(394f))) {
        drawWheel(f(204f), g(394f), sx)
    }
}

private fun DrawScope.drawWheel(cx: Float, cy: Float, sx: Float) {
    val center = Offset(cx, cy)
    drawCircle(WheelOut, radius = sx * 16f, center = center)
    drawCircle(WheelMid, radius = sx * 9f,  center = center)
    drawCircle(WheelHub, radius = sx * 4f,  center = center)
    val spokeStroke = Stroke(sx * 2.5f)
    drawLine(WheelSpk, Offset(cx, cy - sx * 11f), Offset(cx, cy + sx * 11f), strokeWidth = sx * 2.5f)
    drawLine(WheelSpk, Offset(cx - sx * 11f, cy), Offset(cx + sx * 11f, cy), strokeWidth = sx * 2.5f)
}

private fun DrawScope.drawLogo(f: (Float) -> Float, g: (Float) -> Float, alpha: Float) {
    val logoStroke = Stroke(f(9f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val white = LogoWhite.copy(alpha = alpha)
    val accent = LogoAccent.copy(alpha = alpha)

        // ── h ─────────────────────────────────────────────────────────────────────
        drawRoundRect(white, Offset(f(86f),  g(508f)), Size(f(9f), g(48f)), CornerRadius(f(4.5f)))
        drawRoundRect(white, Offset(f(122f), g(525f)), Size(f(9f), g(31f)), CornerRadius(f(4.5f)))
        val hArch = Path().apply {
            // Start ~64% down the left stem (matches HopLogo crossbar proportion).
            // End 2 units into the right leg so the stroke cap overlaps the rect
            // seamlessly — no gap.
            // Control at ~29% x between stems, pulled above both endpoints.
            moveTo(f(95f),  g(539f))
            quadraticBezierTo(f(103f), g(514f), f(122f), g(529f))
        }
        drawPath(hArch, white, style = logoStroke)

    // ── o ─────────────────────────────────────────────────────────────────────
    drawCircle(white, radius = f(20f), center = Offset(f(160f), g(538f)), style = Stroke(f(9f)))
    // Erase the horizontal through the ring, then draw teal dashes
    drawLine(Overlay, Offset(f(141f), g(538f)), Offset(f(180f), g(538f)), strokeWidth = f(3.5f))
    drawLine(accent, Offset(f(146f), g(538f)), Offset(f(154f), g(538f)), strokeWidth = f(2f), cap = StrokeCap.Round)
    drawLine(accent, Offset(f(158f), g(538f)), Offset(f(166f), g(538f)), strokeWidth = f(2f), cap = StrokeCap.Round)
    drawLine(accent, Offset(f(170f), g(538f)), Offset(f(177f), g(538f)), strokeWidth = f(2f), cap = StrokeCap.Round)

    // ── p ─────────────────────────────────────────────────────────────────────
    drawRoundRect(white, Offset(f(192f), g(526f)), Size(f(9f), g(42f)), CornerRadius(f(4.5f)))
    val pBump = Path().apply {
        moveTo(f(201f), g(534f))
        quadraticBezierTo(f(201f), g(514f), f(220f), g(524f))
        quadraticBezierTo(f(235f), g(532f), f(228f), g(546f))
        quadraticBezierTo(f(221f), g(560f), f(201f), g(552f))
    }
    drawPath(pBump, white, style = logoStroke)
}

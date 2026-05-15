package com.example.hop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopTheme
import org.jetbrains.compose.resources.Font
import ridly.composeapp.generated.resources.Res
import ridly.composeapp.generated.resources.nunito_black

// ── HopLogo ───────────────────────────────────────────────────────────────────

/**
 * Renders the Ridly brand lockup: chevron-in-lime-pill icon mark + "ridly" wordmark.
 *
 * [markBg] is the pill background (lime by default), [markStroke] is the chevron
 * colour (dark by default). [textColor] is the wordmark colour.
 *
 * Common variants:
 * - Dark bg  : `HopLogo(textColor = Color.White)` — lime pill, dark chevron, white text
 * - Light bg : `HopLogo()` — lime pill, dark chevron, dark text
 * - Lime bg  : `HopLogo(markBg = Color(0xFF0B0B0B), markStroke = Color(0xFFC5FF45), textColor = Color(0xFF0B0B0B))`
 */
@Composable
fun HopLogo(
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    markBg: Color = Color(0xFFC5FF45),
    markStroke: Color = Color(0xFF0B0B0B),
    textColor: Color = Color(0xFF0B0B0B),
) {
    val syneFamily = FontFamily(Font(Res.font.nunito_black, FontWeight.Black))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((height.value * 0.35f).dp),
        modifier = modifier,
    ) {
        Canvas(modifier = Modifier.size(height)) {
            drawChevronMark(bg = markBg, stroke = markStroke)
        }
        Text(
            text = "ridly",
            color = textColor,
            fontFamily = syneFamily,
            fontWeight = FontWeight.Black,
            fontSize = (height.value * 0.9f).sp,
            letterSpacing = TextUnit(-0.03f, TextUnitType.Em),
            maxLines = 1,
        )
    }
}

// ── R mark drawing ────────────────────────────────────────────────────────────

/**
 * Draws the Ridly icon mark: a lime rounded-square pill containing a dark chevron.
 * The chevron is based on a 44×44 SVG viewBox: M14 8 L30 22 L14 36.
 */
internal fun DrawScope.drawChevronMark(bg: Color, stroke: Color) {
    // Rounded-square pill background (cornerRadius ≈ 25% of size)
    val cr = CornerRadius(size.width * 0.25f, size.width * 0.25f)
    drawRoundRect(bg, Offset.Zero, size, cr)
    // Chevron scaled from 44×44 viewBox
    val scale = size.width / 44f
    withTransform({ scale(scale, scale, Offset.Zero) }) {
        val chevron = Path().apply {
            moveTo(14f, 8f)
            lineTo(30f, 22f)
            lineTo(14f, 36f)
        }
        drawPath(chevron, stroke, style = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "HopLogo — dark bg", showBackground = true, backgroundColor = 0xFF0B0B0B)
@Composable
private fun HopLogoPreviewDark() {
    HopTheme {
        HopLogo(textColor = Color.White)
    }
}

@Preview(name = "HopLogo — light bg", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HopLogoPreviewLight() {
    HopTheme {
        HopLogo()
    }
}

@Preview(name = "HopLogo — large dark", showBackground = true, backgroundColor = 0xFF0B0B0B)
@Composable
private fun HopLogoLargePreview() {
    HopTheme {
        HopLogo(height = 52.dp, textColor = Color.White)
    }
}

@Preview(name = "HopLogo — lime bg", showBackground = true, backgroundColor = 0xFFC5FF45)
@Composable
private fun HopLogoLimeBgPreview() {
    HopTheme {
        HopLogo(markBg = Color(0xFF0B0B0B), markStroke = Color(0xFFC5FF45))
    }
}

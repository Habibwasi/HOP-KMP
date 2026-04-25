package com.example.hop.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Animates an integer between values, useful for trip counts, earnings, CO₂.
 * Pure presentation — caller controls formatting via [format].
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 700,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    format: (Int) -> String = { it.toString() },
) {
    val animated by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = durationMillis),
        label = "animatedCounter",
    )
    Text(
        text = format(animated),
        modifier = modifier,
        style = style,
        color = color,
    )
}

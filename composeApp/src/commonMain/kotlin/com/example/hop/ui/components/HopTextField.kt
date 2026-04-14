package com.example.hop.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Hop design-system text input.
 *
 * Layout:  label (above) → input field → error message (below, when present)
 * Field:   height = 52 dp, 1 dp border at rest, 2 dp Lime border on focus,
 *          cornerRadius = 12 dp, dark surface background.
 *
 * Disabled: 38 % alpha on everything.
 * Error:    red border (#EF4444) + error text below the field.
 */
@Composable
fun HopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val hasError = errorMessage != null

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled   -> Color(0xFF3A3A3A)
            hasError   -> HopColors.error
            isFocused  -> HopColors.primaryLime
            else       -> Color(0xFF3A3A3A)   // subtle resting border
        },
        animationSpec = tween(durationMillis = 150),
        label = "borderColor",
    )

    val borderWidth = if (isFocused && !hasError) 2.dp else 1.dp
    val contentAlpha = if (enabled) 1f else 0.38f
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier) {
        // Label above field
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) HopColors.textSecondary else HopColors.textSecondary.copy(alpha = 0.38f),
            modifier = Modifier.padding(bottom = HopSpacing.xs),
        )

        // Field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(shape)
                .background(HopColors.surfaceElevated.copy(alpha = contentAlpha))
                .border(borderWidth, borderColor, shape),
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(HopColors.primaryLime),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = HopColors.textPrimary.copy(alpha = contentAlpha),
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = HopSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Placeholder + actual text content, expanding to fill available width
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = HopColors.textSecondary.copy(alpha = if (enabled) 0.6f else 0.38f),
                            )
                        }
                        innerTextField()
                    }

                    // Trailing icon
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            },
        )

        // Error message below field
        if (hasError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = HopColors.error,
                modifier = Modifier.padding(top = HopSpacing.xs, start = HopSpacing.xs),
            )
        }
    }
}

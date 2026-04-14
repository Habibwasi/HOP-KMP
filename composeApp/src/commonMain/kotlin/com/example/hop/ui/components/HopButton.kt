package com.example.hop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors

enum class HopButtonVariant { Primary, Ghost, Destructive }

/**
 * Hop design-system button.
 *
 * Variants:
 *  - Primary     — Lime fill  (#C8F135), dark label
 *  - Ghost       — Transparent with Lime 1 dp border
 *  - Destructive — Red fill (#EF4444), white label
 *
 * States: enabled (default), loading (spinner replaces label), disabled (38 % alpha).
 * All variants are height = 52 dp, cornerRadius = 12 dp, full-width by default.
 */
@Composable
fun HopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HopButtonVariant = HopButtonVariant.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val isInteractive = enabled && !isLoading

    when (variant) {
        HopButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = shape,
            enabled = isInteractive,
            colors = ButtonDefaults.buttonColors(
                containerColor = HopColors.primaryLime,
                contentColor = Color(0xFF1A1A1A),
                disabledContainerColor = HopColors.primaryLime.copy(alpha = 0.38f),
                disabledContentColor = Color(0xFF1A1A1A).copy(alpha = 0.38f),
            ),
        ) {
            ButtonContent(
                text = text,
                isLoading = isLoading,
                contentColor = Color(0xFF1A1A1A),
                leadingIcon = leadingIcon,
            )
        }

        HopButtonVariant.Ghost -> OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = shape,
            enabled = isInteractive,
            border = BorderStroke(
                width = 1.dp,
                color = if (isInteractive) HopColors.primaryLime else HopColors.primaryLime.copy(alpha = 0.38f),
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = HopColors.primaryLime,
                disabledContentColor = HopColors.primaryLime.copy(alpha = 0.38f),
            ),
        ) {
            ButtonContent(
                text = text,
                isLoading = isLoading,
                contentColor = if (isInteractive) HopColors.primaryLime else HopColors.primaryLime.copy(alpha = 0.38f),
                leadingIcon = leadingIcon,
            )
        }

        HopButtonVariant.Destructive -> Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = shape,
            enabled = isInteractive,
            colors = ButtonDefaults.buttonColors(
                containerColor = HopColors.error,
                contentColor = Color.White,
                disabledContainerColor = HopColors.error.copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.38f),
            ),
        ) {
            ButtonContent(
                text = text,
                isLoading = isLoading,
                contentColor = Color.White,
                leadingIcon = leadingIcon,
            )
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    contentColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp,
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

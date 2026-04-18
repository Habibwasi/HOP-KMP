package com.example.hop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopTheme

/**
 * Full-screen error state shown when [com.example.hop.network.ApiResponse.Error] is received.
 *
 * Displays an error icon, a descriptive [message], and a "Retry" call-to-action that invokes
 * [onRetry]. Follows the Hop dark-themed design — [HopColors.surface] background, error icon in
 * [HopColors.error], and a primary [HopButton] for the retry action.
 *
 * Usage:
 * ```
 * when {
 *     state.error != null -> ErrorScreen(
 *         message = state.error,
 *         onRetry  = { onEvent(MyEvent.Retry) },
 *     )
 *     ...
 * }
 * ```
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = HopColors.error,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Something went wrong",
            color = HopColors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            color = HopColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(32.dp))

        HopButton(
            text = "Retry",
            onClick = onRetry,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ErrorScreenPreview() {
    HopTheme {
        ErrorScreen(
            message = "Unable to load data. Please check your connection and try again.",
            onRetry = {},
        )
    }
}

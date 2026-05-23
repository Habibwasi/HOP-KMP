package com.example.hop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.delay

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * ON-02b — Email Verified success screen.
 *
 * Shown after the user taps their confirmation link and the deep-link callback
 * succeeds. Auto-advances to Home after 2 seconds; no user interaction needed.
 */
@Composable
fun EmailVerifiedScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(2_000L)
        onNavigateToHome()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = HopColors.authAccent,
            modifier = Modifier.size(88.dp),
        )

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        Text(
            text = "Email verified!",
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.authTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.sm))

        Text(
            text = "Your account is ready. Taking you home…",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun EmailVerifiedPreview() {
    HopTheme {
        EmailVerifiedScreen(onNavigateToHome = {})
    }
}

package com.example.hop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthUiState
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun VerifyEmailRoute(
    onNavigateToEmailVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToEmailVerified -> onNavigateToEmailVerified()
                is AuthEffect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        VerifyEmailScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = {
                viewModel.onEvent(AuthEvent.ClearEmailVerification)
                onNavigateBack()
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun VerifyEmailScreen(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val email = state.pendingVerificationEmail ?: ""
    val cooldown = state.resendCooldownSeconds
    val canResend = cooldown == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Back button ───────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Envelope icon ─────────────────────────────────────────────────────
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = HopColors.authAccent,
            modifier = Modifier.size(72.dp),
        )

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Heading ───────────────────────────────────────────────────────────
        Text(
            text = "Check your email",
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.authTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.sm))

        // ── Body ──────────────────────────────────────────────────────────────
        Text(
            text = "We sent a confirmation link to",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.authTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xs))

        Text(
            text = "Tap the link in the email to verify your account and continue.",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Resend button ─────────────────────────────────────────────────────
        HopButton(
            text = if (canResend) "Resend email" else "Resend in ${cooldown}s",
            onClick = { onEvent(AuthEvent.ResendVerificationEmail) },
            variant = HopButtonVariant.Ghost,
            enabled = canResend,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

        Text(
            text = "Didn't receive it? Check your spam folder.",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun VerifyEmailCooldownPreview() {
    HopTheme {
        VerifyEmailScreen(
            state = AuthUiState(
                isEmailVerificationPending = true,
                pendingVerificationEmail = "jane@example.com",
                resendCooldownSeconds = 42,
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview
@Composable
private fun VerifyEmailReadyPreview() {
    HopTheme {
        VerifyEmailScreen(
            state = AuthUiState(
                isEmailVerificationPending = true,
                pendingVerificationEmail = "jane@example.com",
                resendCooldownSeconds = 0,
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

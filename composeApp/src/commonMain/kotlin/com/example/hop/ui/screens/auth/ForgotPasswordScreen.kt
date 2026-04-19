package com.example.hop.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.components.HopTextField
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * ON-03b — Forgot Password Route.
 *
 * MVP stub: no ViewModel. Simulates a brief loading delay then shows
 * the "Check your email" success state.
 * Post-MVP: wire to AuthEvent.RequestPasswordReset + AuthRepository.
 */
@Composable
fun ForgotPasswordRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgotPasswordScreen(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * ON-03b — Forgot Password Screen.
 *
 * Two states driven by local [submitted] flag:
 *  - **Form** — email input + "Send reset link" CTA
 *  - **Success** — envelope icon + "Check your email" confirmation
 *
 * No ViewModel in MVP. Network call is stubbed with a [delay].
 *
 * @param onNavigateBack  Called when the user taps the back arrow or "Back to Log In".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email     by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()

    val emailValid = email.contains("@") && email.split("@").lastOrNull()?.contains(".") == true
    val canSubmit  = emailValid && !isLoading

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = HopColors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor      = HopColors.surface,
                    scrolledContainerColor = HopColors.surface,
                ),
            )
        },
    ) { innerPadding ->

        AnimatedContent(
            targetState = submitted,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "forgot-password-content",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { isSubmitted ->
            if (isSubmitted) {
                SuccessContent(
                    email = email,
                    onNavigateBack = onNavigateBack,
                    onTryAgain = { submitted = false },
                )
            } else {
                FormContent(
                    email = email,
                    onEmailChange = { email = it },
                    isLoading = isLoading,
                    canSubmit = canSubmit,
                    onSubmit = {
                        scope.launch {
                            isLoading = true
                            delay(800L) // MVP stub — replace with real API call
                            isLoading = false
                            submitted = true
                        }
                    },
                    onNavigateBack = onNavigateBack,
                )
            }
        }
    }
}

// ── Form content ──────────────────────────────────────────────────────────────

@Composable
private fun FormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.lg),
    ) {
        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Icon ──────────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = HopColors.surfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LockReset,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Reset password",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HopColors.textPrimary,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xs))

        Text(
            text = "Enter the email address linked to your account and we'll send you a reset link.",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.textSecondary,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Email ─────────────────────────────────────────────────────────────
        HopTextField(
            label = "Email address",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "jane@example.com",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── CTA ───────────────────────────────────────────────────────────────
        HopButton(
            text = if (isLoading) "Sending…" else "Send reset link",
            onClick = onSubmit,
            variant = HopButtonVariant.Primary,
            isLoading = isLoading,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Back to log in ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Back to Log In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HopColors.textSecondary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))
    }
}

// ── Success content ───────────────────────────────────────────────────────────

@Composable
private fun SuccessContent(
    email: String,
    onNavigateBack: () -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // ── Illustration ──────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = HopColors.primaryLime.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(48.dp),
                ),
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        Text(
            text = "Check your email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HopColors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xs))

        Text(
            text = "We've sent a reset link to $email.\nCheck your inbox and follow the instructions.",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Back to log in ────────────────────────────────────────────────────
        HopButton(
            text = "Back to Log In",
            onClick = onNavigateBack,
            variant = HopButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Try again ─────────────────────────────────────────────────────────
        TextButton(onClick = onTryAgain) {
            Text(
                text = "Didn't receive it? Try again",
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.textSecondary,
                textDecoration = TextDecoration.Underline,
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ForgotPasswordScreenEmptyPreview() {
    HopTheme {
        ForgotPasswordScreen(onNavigateBack = {})
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenLoadingPreview() {
    HopTheme {
        // Show the form with a valid email so the enabled state is visible.
        ForgotPasswordScreen(onNavigateBack = {})
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenSuccessPreview() {
    HopTheme {
        // Drive the success content directly.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HopColors.surface),
        ) {
            SuccessContent(
                email = "jane@example.com",
                onNavigateBack = {},
                onTryAgain = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

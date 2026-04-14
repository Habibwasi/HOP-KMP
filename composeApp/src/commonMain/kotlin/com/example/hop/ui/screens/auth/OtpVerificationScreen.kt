package com.example.hop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val OTP_LENGTH = 6
private const val RESEND_COUNTDOWN_SECONDS = 45

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * ON-04 — OTP Verification Route.
 *
 * Obtains [AuthViewModel] from Koin, collects state and one-shot effects,
 * and delegates rendering to the stateless [OtpVerificationScreen].
 *
 * [onNavigateToHome] must clear the back stack (caller's responsibility).
 */
@Composable
fun OtpVerificationRoute(
    phone: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> onNavigateToHome()

                is AuthEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                    viewModel.onEvent(AuthEvent.ClearError)
                }

                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        OtpVerificationScreen(
            phone = phone,
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * ON-04 — OTP Verification Screen.
 *
 * Stateless renderer. Digit values, countdown timer, and focus management are
 * held as local Compose state — all are ephemeral UI concerns.
 *
 * @param phone  The phone number the code was sent to (display-only).
 * @param state  Current auth loading/error state from the ViewModel.
 * @param onEvent Dispatches [AuthEvent] to the ViewModel.
 */
@Composable
fun OtpVerificationScreen(
    phone: String,
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local state ───────────────────────────────────────────────────────────
    val digits = remember { Array(OTP_LENGTH) { mutableStateOf("") } }
    val focusRequesters = remember { Array(OTP_LENGTH) { FocusRequester() } }
    var countdown by remember { mutableIntStateOf(RESEND_COUNTDOWN_SECONDS) }

    val code = digits.joinToString("") { it.value }
    val isComplete = code.length == OTP_LENGTH
    val canSubmit = isComplete && !state.isLoading

    // ── Countdown timer ───────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1_000L)
            countdown--
        }
    }

    // ── Auto-submit on 6th digit ──────────────────────────────────────────────
    LaunchedEffect(code) {
        if (code.length == OTP_LENGTH) {
            onEvent(AuthEvent.VerifyOtp(phone = phone, code = code))
        }
    }
    // ── Clear error when digit changes ─────────────────────────────────────
    LaunchedEffect(code) {
        if (state.error != null && code.isNotEmpty()) {
            onEvent(AuthEvent.ClearError)
        }
    }
    // ── Request focus on first digit on entry ─────────────────────────────────
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Verify your number",
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.textPrimary,
        )
        Spacer(modifier = Modifier.height(HopSpacing.xs))
        Text(
            text = "We sent a 6-digit code to $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.textSecondary,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── OTP digit boxes ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            repeat(OTP_LENGTH) { index ->
                OtpDigitBox(
                    value = digits[index].value,
                    onValueChange = { incoming ->
                        val cleaned = incoming.filter { it.isDigit() }
                        when {
                            cleaned.isEmpty() -> {
                                digits[index].value = ""
                            }
                            cleaned.length == 1 -> {
                                digits[index].value = cleaned
                                if (index < OTP_LENGTH - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                            // Handle paste: distribute digits across boxes
                            else -> {
                                cleaned.take(OTP_LENGTH - index).forEachIndexed { offset, ch ->
                                    digits[index + offset].value = ch.toString()
                                }
                                val nextFocus = minOf(index + cleaned.length, OTP_LENGTH - 1)
                                focusRequesters[nextFocus].requestFocus()
                            }
                        }
                    },
                    onBackspace = {
                        if (digits[index].value.isEmpty() && index > 0) {
                            digits[index - 1].value = ""
                            focusRequesters[index - 1].requestFocus()
                        } else {
                            digits[index].value = ""
                        }
                    },
                    isFocused = false,
                    isError = state.error != null,
                    enabled = !state.isLoading,
                    focusRequester = focusRequesters[index],
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Inline error ──────────────────────────────────────────────────────
        if (state.error != null) {
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.error,
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Resend timer / link ───────────────────────────────────────────────
        if (countdown > 0) {
            Text(
                text = "Resend code in 0:${countdown.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.textSecondary,
            )
        } else {
            TextButton(
                onClick = {
                    // Reset digits and restart timer
                    digits.forEach { it.value = "" }
                    countdown = RESEND_COUNTDOWN_SECONDS
                    focusRequesters[0].requestFocus()
                    onEvent(AuthEvent.ClearError)
                    onEvent(AuthEvent.SendOtp(phone = phone))
                },
                enabled = !state.isLoading,
            ) {
                Text(
                    text = "Resend code",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = HopColors.primaryLime,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Verify button ─────────────────────────────────────────────────────
        HopButton(
            text = "Verify",
            onClick = {
                onEvent(AuthEvent.VerifyOtp(phone = phone, code = code))
            },
            variant = HopButtonVariant.Primary,
            isLoading = state.isLoading,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Digit box ─────────────────────────────────────────────────────────────────

/**
 * A single OTP digit input box.
 *
 * Uses [BasicTextField] to render a single digit with custom styling.
 * Backspace on an empty box is forwarded via [onBackspace] so the parent
 * can shift focus to the previous box.
 */
@Composable
private fun OtpDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    isFocused: Boolean,
    isError: Boolean,
    enabled: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        isError && value.isNotEmpty() -> HopColors.error
        isError                       -> HopColors.error.copy(alpha = 0.5f)
        value.isNotEmpty()            -> HopColors.primaryLime
        else                          -> HopColors.textSecondary.copy(alpha = 0.3f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 44.dp, height = 56.dp)
            .background(
                color = HopColors.surfaceElevated,
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                color = HopColors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(HopColors.primaryLime),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Backspace && value.isEmpty()) {
                        onBackspace()
                        true
                    } else {
                        false
                    }
                },
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OtpVerificationScreenEmptyPreview() {
    HopTheme {
        OtpVerificationScreen(
            phone = "+45 20 12 34 56",
            state = AuthUiState(),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun OtpVerificationScreenLoadingPreview() {
    HopTheme {
        OtpVerificationScreen(
            phone = "+45 20 12 34 56",
            state = AuthUiState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun OtpVerificationScreenErrorPreview() {
    HopTheme {
        OtpVerificationScreen(
            phone = "+45 20 12 34 56",
            state = AuthUiState(error = "Incorrect code. 2 attempts remaining."),
            onEvent = {},
        )
    }
}

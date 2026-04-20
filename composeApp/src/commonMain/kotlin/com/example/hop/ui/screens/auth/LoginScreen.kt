package com.example.hop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthUiState
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.components.HopTextField
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * ON-03 — Log In Route.
 *
 * Obtains [AuthViewModel] from Koin, collects state and one-shot effects,
 * and delegates rendering to the stateless [LoginScreen].
 */
@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome ->
                    onNavigateToHome()

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
        containerColor = HopColors.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        LoginScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToForgotPassword = onNavigateToForgotPassword,
            onNavigateToSignUp = onNavigateToSignUp,
            onMitIdClick = {
                scope.launch { snackbarHostState.showSnackbar("Coming soon") }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * ON-03 — Log In Screen.
 *
 * Stateless: email/password and visibility toggle are held locally.
 * Loading and error states flow in from [AuthUiState].
 *
 * @param state                    Current auth loading/error state from the ViewModel.
 * @param onEvent                  Dispatches [AuthEvent] to the ViewModel.
 * @param onNavigateToForgotPassword Navigate to the password reset stub screen.
 * @param onNavigateToSignUp       Navigate to ON-02 (Sign Up).
 * @param onMitIdClick             Fired when the disabled MitID button is tapped.
 */
@Composable
fun LoginScreen(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onMitIdClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local form state ──────────────────────────────────────────────────────
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Derived validation ────────────────────────────────────────────────────
    val emailValid = email.contains("@") && email.substringAfter("@").contains(".")
    val passwordValid = password.isNotEmpty()
    val canSubmit = emailValid && passwordValid && !state.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HopSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.authTextPrimary,
        )
        Spacer(modifier = Modifier.height(HopSpacing.xs))
        Text(
            text = "Log in to your Hop account",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Email ─────────────────────────────────────────────────────────────
        HopTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email address",
            placeholder = "jane@example.com",
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            lightSurface = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Password ──────────────────────────────────────────────────────────
        HopTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            enabled = !state.isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            lightSurface = true,
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.padding(end = HopSpacing.xs),
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.labelMedium,
                        color = HopColors.authAccent,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // ── "Forgot password?" link ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onNavigateToForgotPassword,
                enabled = !state.isLoading,
            ) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.labelMedium,
                    color = HopColors.authAccent,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Log In (Primary) ──────────────────────────────────────────────────
        HopButton(
            text = "Log In",
            onClick = {
                onEvent(
                    AuthEvent.Login(
                        email = email.trim(),
                        password = password,
                    )
                )
            },
            variant = HopButtonVariant.Primary,
            isLoading = state.isLoading,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Continue with MitID (Ghost — visible but disabled) ────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Coming soon",
                    onClick = onMitIdClick,
                ),
        ) {
            HopButton(
                text = "Continue with MitID",
                onClick = { /* intercepted by Box wrapper */ },
                variant = HopButtonVariant.Ghost,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Footer link ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
            )
            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = HopColors.authAccent,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun LoginScreenEmptyPreview() {
    HopTheme {
        LoginScreen(
            state = AuthUiState(),
            onEvent = {},
            onNavigateToForgotPassword = {},
            onNavigateToSignUp = {},
            onMitIdClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    HopTheme {
        LoginScreen(
            state = AuthUiState(isLoading = true),
            onEvent = {},
            onNavigateToForgotPassword = {},
            onNavigateToSignUp = {},
            onMitIdClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    HopTheme {
        LoginScreen(
            state = AuthUiState(error = "Invalid email or password"),
            onEvent = {},
            onNavigateToForgotPassword = {},
            onNavigateToSignUp = {},
            onMitIdClick = {},
        )
    }
}

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * ON-02 — Sign Up Route.
 *
 * Obtains [AuthViewModel] from Koin, collects state and one-shot effects,
 * and delegates rendering to the stateless [SignUpScreen].
 */
@Composable
fun SignUpRoute(
    onNavigateToOtpVerification: (phone: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToOtpVerification ->
                    onNavigateToOtpVerification(effect.phone)

                is AuthEffect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }

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
        SignUpScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToLogin = onNavigateToLogin,
            onMitIdClick = {
                scope.launch { snackbarHostState.showSnackbar("Coming soon") }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * ON-02 — Sign Up Screen.
 *
 * Stateless: all visual state (text fields, password visibility, checkbox) is
 * held locally. Only the submit state (loading / error) flows in from outside
 * via [AuthUiState].
 *
 * @param state        Current auth loading/error state from the ViewModel.
 * @param onEvent      Dispatches [AuthEvent] to the ViewModel.
 * @param onNavigateToLogin    Navigate to ON-03 (Log In).
 * @param onMitIdClick Callback fired when the "Continue with MitID" button is tapped.
 */
@Composable
fun SignUpScreen(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onMitIdClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local form state ──────────────────────────────────────────────────────
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // ── Derived validation ────────────────────────────────────────────────────
    val fullNameValid = fullName.trim().isNotEmpty()
    val emailValid = email.contains("@") && email.substringAfter("@").contains(".")
    // Minimal phone check: +45 followed by 8 digits is the Danish mobile format
    val phoneValid = phone.trim().length >= 8
    val passwordValid = password.length >= 8
    val formValid = fullNameValid && emailValid && phoneValid && passwordValid && termsAccepted
    val canSubmit = formValid && !state.isLoading

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
            text = "Create your account",
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.authTextPrimary,
        )
        Spacer(modifier = Modifier.height(HopSpacing.xs))
        Text(
            text = "Start your Hop journey",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Full name ─────────────────────────────────────────────────────────
        HopTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Full name",
            placeholder = "Jane Doe",
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            lightSurface = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))

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

        // ── Phone number ──────────────────────────────────────────────────────
        HopTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone number",
            placeholder = "+45 20 12 34 56",
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
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

        // Helper text — always visible
        Text(
            text = "At least 8 characters",
            style = MaterialTheme.typography.labelSmall,
            color = if (password.isNotEmpty() && !passwordValid)
                        HopColors.error
                    else
                        HopColors.authTextSecondary,
            modifier = Modifier.padding(top = HopSpacing.xs, start = HopSpacing.xs),
        )

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Terms & Privacy checkbox ───────────────────────────────────────────
        TermsCheckboxRow(
            checked = termsAccepted,
            onCheckedChange = { termsAccepted = it },
            enabled = !state.isLoading,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Continue (Primary) ─────────────────────────────────────────────────
        HopButton(
            text = "Continue",
            onClick = {
                onEvent(
                    AuthEvent.Register(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
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

        // ── Continue with MitID (Ghost — always disabled, shows toast on tap) ─
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
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Log in",
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

// ── Terms checkbox row ─────────────────────────────────────────────────────────

@Composable
private fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val termsText = buildAnnotatedString {
        append("I agree to the ")
        withStyle(
            SpanStyle(
                color = HopColors.authAccent,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
            )
        ) {
            append("Terms of Service")
        }
        append(" and ")
        withStyle(
            SpanStyle(
                color = HopColors.authAccent,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
            )
        ) {
            append("Privacy Policy")
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = HopColors.authAccent,
                uncheckedColor = HopColors.authTextSecondary,
                checkmarkColor = HopColors.background,
                disabledCheckedColor = HopColors.authAccent.copy(alpha = 0.38f),
                disabledUncheckedColor = HopColors.authTextSecondary.copy(alpha = 0.38f),
            ),
        )
        Text(
            text = termsText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) HopColors.authTextSecondary else HopColors.authTextSecondary.copy(alpha = 0.38f),
            modifier = Modifier
                .padding(start = HopSpacing.xs)
                .clickable(enabled = enabled, role = Role.Checkbox) {
                    onCheckedChange(!checked)
                },
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SignUpScreenEmptyPreview() {
    HopTheme {
        SignUpScreen(
            state = AuthUiState(),
            onEvent = {},
            onNavigateToLogin = {},
            onMitIdClick = {},
        )
    }
}

@Preview
@Composable
private fun SignUpScreenFilledPreview() {
    HopTheme {
        SignUpScreen(
            state = AuthUiState(),
            onEvent = {},
            onNavigateToLogin = {},
            onMitIdClick = {},
        )
    }
}

@Preview
@Composable
private fun SignUpScreenLoadingPreview() {
    HopTheme {
        SignUpScreen(
            state = AuthUiState(isLoading = true),
            onEvent = {},
            onNavigateToLogin = {},
            onMitIdClick = {},
        )
    }
}

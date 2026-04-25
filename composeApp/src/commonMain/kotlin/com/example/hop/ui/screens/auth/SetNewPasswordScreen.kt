package com.example.hop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.components.HopTextField
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * ON-03c — Set New Password Route.
 *
 * Reached only via the recovery deep-link (hop://auth/callback?type=recovery).
 * The Supabase session is already imported by [AuthViewModel] before navigating here.
 *
 * @param onPasswordUpdated  Called when the password has been saved successfully (navigate to Home).
 */
@Composable
fun SetNewPasswordRoute(
    onPasswordUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.PasswordUpdated -> onPasswordUpdated()
                is AuthEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                    viewModel.onEvent(AuthEvent.ClearError)
                }
                else -> Unit
            }
        }
    }

    SetNewPasswordScreen(
        isLoading = state.isLoading,
        onSubmit = { newPassword ->
            viewModel.onEvent(AuthEvent.UpdatePassword(newPassword))
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordScreen(
    isLoading: Boolean,
    onSubmit: (newPassword: String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val passwordValid = password.length >= 8
    val passwordsMatch = password == confirm
    val canSubmit = passwordValid && passwordsMatch && !isLoading

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = HopColors.background,
                    scrolledContainerColor = HopColors.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HopColors.background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = HopSpacing.lg),
        ) {
            Spacer(Modifier.height(HopSpacing.lg))

            // ── Icon ──────────────────────────────────────────────────────
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = HopColors.authInputSurface,
                        shape = RoundedCornerShape(16.dp),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = HopColors.authAccent,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(HopSpacing.lg))

            Text(
                text = "Set new password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
            )

            Spacer(Modifier.height(HopSpacing.xs))

            Text(
                text = "Choose a strong password of at least 8 characters.",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
            )

            Spacer(Modifier.height(HopSpacing.xl))

            // ── New password ──────────────────────────────────────────────
            HopTextField(
                label = "New password",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Text(
                            text = if (showPassword) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall,
                            color = HopColors.authAccent,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                enabled = !isLoading,
                lightSurface = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(HopSpacing.md))

            // ── Confirm password ──────────────────────────────────────────
            HopTextField(
                label = "Confirm password",
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = "••••••••",
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Text(
                            text = if (showConfirm) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall,
                            color = HopColors.authAccent,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                enabled = !isLoading,
                lightSurface = true,
                errorMessage = if (confirm.isNotEmpty() && !passwordsMatch) "Passwords don't match" else null,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(HopSpacing.lg))

            HopButton(
                text = if (isLoading) "Saving…" else "Save password",
                onClick = { onSubmit(password) },
                variant = HopButtonVariant.Primary,
                isLoading = isLoading,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(HopSpacing.xl))
        }
    }
}

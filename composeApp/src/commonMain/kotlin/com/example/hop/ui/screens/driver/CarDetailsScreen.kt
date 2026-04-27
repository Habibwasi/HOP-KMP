package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.CarDetails
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopTextField
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-02 — Car Details Route.
 *
 * Obtains [DriverViewModel] from Koin, collects effects, and delegates
 * rendering to the stateless [CarDetailsScreen].
 */
@Composable
fun CarDetailsRoute(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
    authViewModel: AuthViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToHome -> {
                    // Refresh the cached user so hasDriverRole flips to true immediately.
                    authViewModel.onEvent(AuthEvent.RefreshProfile)
                    onNavigateToHome()
                }
                is DriverEffect.ShowSnackbar ->
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
        CarDetailsScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-02 — Car Details Screen.
 *
 * Step 1 of 1 in the driver onboarding flow. Collects car information and
 * dispatches [DriverEvent.SaveCarDetails] on Continue.
 */
@Composable
fun CarDetailsScreen(
    state: DriverUiState,
    onEvent: (DriverEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local form state ──────────────────────────────────────────────────────
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var colour by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf(4) }

    // ── Derived validation ────────────────────────────────────────────────────
    val yearInt = year.trim().toIntOrNull()
    val yearValid = yearInt != null && yearInt in 1980..2100
    val formValid = make.trim().isNotEmpty()
        && model.trim().isNotEmpty()
        && yearValid
        && colour.trim().isNotEmpty()
        && plate.trim().isNotEmpty()
    val canContinue = formValid && !state.isSubmittingOnboarding

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(HopSpacing.sm))
            Text(
                text = "Become a Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.authTextPrimary,
            )
        }

        HorizontalDivider(color = HopColors.authInputBorder)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Text(
                text = "Your Car Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
            )
            Text(
                text = "We need a few details about your car to set up your driver profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
            )

            Spacer(Modifier.height(HopSpacing.sm))

            HopTextField(
                value = make,
                onValueChange = { make = it },
                label = "Make",
                placeholder = "e.g. Toyota",
                lightSurface = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            HopTextField(
                value = model,
                onValueChange = { model = it },
                label = "Model",
                placeholder = "e.g. Corolla",
                lightSurface = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            HopTextField(
                value = year,
                onValueChange = { year = it },
                label = "Year",
                placeholder = "e.g. 2020",
                errorMessage = if (year.isNotEmpty() && !yearValid) "Enter a valid year" else null,
                lightSurface = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )
            HopTextField(
                value = colour,
                onValueChange = { colour = it },
                label = "Colour",
                placeholder = "e.g. White",
                lightSurface = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            HopTextField(
                value = plate,
                onValueChange = { plate = it.uppercase() },
                label = "Plate Number",
                placeholder = "e.g. AB 12 345",
                lightSurface = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
            )

            // ── Seats stepper ─────────────────────────────────────────────
            Column {
                Text(
                    text = "Available Seats",
                    style = MaterialTheme.typography.labelMedium,
                    color = HopColors.authTextSecondary,
                    modifier = Modifier.padding(bottom = HopSpacing.xs),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HopSpacing.md),
                ) {
                    IconButton(
                        onClick = { if (seats > 1) seats-- },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HopColors.authInputSurface),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Decrease seats",
                            tint = HopColors.authTextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "$seats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = HopColors.authTextPrimary,
                    )
                    IconButton(
                        onClick = { if (seats < 8) seats++ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HopColors.authInputSurface),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Increase seats",
                            tint = HopColors.authTextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // ── Continue button ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.background)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            HopButton(
                text = "Continue",
                enabled = canContinue,
                isLoading = state.isSubmittingOnboarding,
                onClick = {
                    onEvent(
                        DriverEvent.SaveCarDetails(
                            CarDetails(
                                make = make.trim(),
                                model = model.trim(),
                                year = yearInt ?: 0,
                                colour = colour.trim(),
                                licensePlate = plate.trim(),
                                seatsAvailable = seats,
                            ),
                        ),
                    )
                },
            )
        }
    }
}

// ── Shared step indicator ──────────────────────────────────────────────────────

/**
 * Compact step indicator used across the driver onboarding flow.
 *
 * - Completed steps: filled lime circle with step number.
 * - Current step:    filled lime circle with step number (same as completed).
 * - Future steps:    outlined circle with muted number.
 * - Steps connected by a horizontal divider that turns lime when passed.
 */
@Composable
internal fun OnboardingStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            // Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted || isCurrent -> HopColors.primaryLime
                            else                     -> HopColors.authInputBorder
                        },
                    ),
            ) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCompleted || isCurrent -> HopColors.authTextPrimary
                        else                     -> HopColors.authTextSecondary
                    },
                )
            }

            if (index < totalSteps - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = HopSpacing.xs),
                    color = if (isCompleted) HopColors.primaryLime else HopColors.authInputBorder,
                    thickness = 2.dp,
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CarDetailsScreenPreview() {
    HopTheme {
        CarDetailsScreen(
            state = DriverUiState(),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

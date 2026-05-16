package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.profile.OwnProfileEffect
import com.example.hop.presentation.profile.OwnProfileEvent
import com.example.hop.presentation.profile.OwnProfileUiState
import com.example.hop.presentation.profile.OwnProfileViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-03 — Enable Driver: MobilePay Number.
 *
 * Second onboarding step after car details. Asks the new driver for their
 * MobilePay number so passengers can pay them after rides. Saving navigates
 * home; "Skip for now" also goes home.
 */
@Composable
fun EnableDriverMobilepayRoute(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onEvent(OwnProfileEvent.StartEditMobilepay)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OwnProfileEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                    // Navigate home after a successful save (snackbar message contains "updated").
                    if (effect.message.contains("updated", ignoreCase = true)) {
                        onNavigateToHome()
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        EnableDriverMobilepayScreen(
            state = state,
            onDraftChanged = { viewModel.onEvent(OwnProfileEvent.MobilepayDraftChanged(it)) },
            onSave = { viewModel.onEvent(OwnProfileEvent.SaveMobilepay) },
            onSkip = onNavigateToHome,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun EnableDriverMobilepayScreen(
    state: OwnProfileUiState,
    onDraftChanged: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HopSpacing.md),
    ) {
        // Top bar
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                )
            }
            Text(
                text = "Step 2 of 2",
                style = MaterialTheme.typography.labelMedium,
                color = HopColors.authTextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        Text(
            text = "Your MobilePay number",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = HopColors.authTextPrimary,
        )
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        Text(
            text = "Passengers send payment directly to you via MobilePay after each ride. You can update this at any time from your profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        OutlinedTextField(
            value = state.mobilepayDraft,
            onValueChange = { if (it.length <= 8) onDraftChanged(it.filter { c -> c.isDigit() }) },
            label = { Text("MobilePay number") },
            placeholder = { Text("8-digit number, e.g. 20123456") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSavingMobilepay,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HopColors.primaryLime,
                unfocusedBorderColor = HopColors.authTextSecondary,
                focusedLabelColor = HopColors.primaryLime,
                unfocusedLabelColor = HopColors.authTextSecondary,
                focusedTextColor = HopColors.authTextPrimary,
                unfocusedTextColor = HopColors.authTextPrimary,
                cursorColor = HopColors.primaryLime,
                focusedContainerColor = HopColors.authInputSurface,
                unfocusedContainerColor = HopColors.authInputSurface,
            ),
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(HopSpacing.sm)) {
            HopButton(
                text = "Save & Finish",
                onClick = onSave,
                isLoading = state.isSavingMobilepay,
                enabled = state.mobilepayDraft.length == 8,
                modifier = Modifier.fillMaxWidth(),
            )
            HopButton(
                text = "Skip for now",
                onClick = onSkip,
                enabled = !state.isSavingMobilepay,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EnableDriverMobilepayPreview() {
    HopTheme {
        EnableDriverMobilepayScreen(
            state = OwnProfileUiState(
                isEditingMobilepay = true,
                mobilepayDraft = "2012",
            ),
            onDraftChanged = {},
            onSave = {},
            onSkip = {},
            onNavigateBack = {},
        )
    }
}

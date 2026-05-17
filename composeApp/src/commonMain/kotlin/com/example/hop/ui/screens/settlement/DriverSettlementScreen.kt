package com.example.hop.ui.screens.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.example.hop.domain.model.RideSettlement
import com.example.hop.presentation.settlement.SettlementEffect
import com.example.hop.presentation.settlement.SettlementEvent
import com.example.hop.presentation.settlement.SettlementUiState
import com.example.hop.presentation.settlement.SettlementViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DriverSettlementRoute(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onSettlementComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettlementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookingId) {
        viewModel.onEvent(SettlementEvent.Load(bookingId))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(SettlementEvent.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettlementEffect.ConfirmReceivedSuccess -> onSettlementComplete()
                is SettlementEffect.DisputeSubmittedSuccess -> {
                    snackbarHostState.showSnackbar("Dispute submitted")
                    onSettlementComplete()
                }
                is SettlementEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        DriverSettlementScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun DriverSettlementScreen(
    state: SettlementUiState,
    onEvent: (SettlementEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDisputeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HopColors.authTextPrimary)
            }
            Text(
                "Payment Confirmation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
            )
        }

        if (state.isLoading) {
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = HopColors.primaryLime)
            Spacer(Modifier.weight(1f))
        } else {
            val settlement = state.settlement
            if (settlement != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(HopSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
                ) {
                    // Amount card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HopColors.authInputSurface)
                            .padding(HopSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                    ) {
                        Text("Expected payment", style = MaterialTheme.typography.labelMedium, color = HopColors.authTextSecondary)
                        Text(
                            "DKK ${settlement.suggestedAmountOere / 100}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = HopMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = HopColors.primaryLime,
                        )
                    }

                    if (settlement.passengerPaidAt != null) {
                        Text(
                            "Passenger has marked this as paid",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = HopColors.primaryLime,
                        )
                    } else {
                        Text(
                            "Waiting for passenger to send payment via MobilePay...",
                            style = MaterialTheme.typography.bodySmall,
                            color = HopColors.authTextSecondary,
                        )
                    }

                    if (showDisputeSheet) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(HopColors.authInputSurface)
                                .padding(HopSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                        ) {
                            Text("Dispute reason", style = MaterialTheme.typography.labelMedium, color = HopColors.authTextSecondary)
                            OutlinedTextField(
                                value = state.disputeReason,
                                onValueChange = { onEvent(SettlementEvent.DisputeReasonChanged(it)) },
                                placeholder = { Text("Describe the issue (min 10 characters)...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                            )
                            HopButton(
                                text = "Submit Dispute",
                                onClick = { onEvent(SettlementEvent.SubmitDispute) },
                                isLoading = state.isDisputing,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HopColors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                ) {
                    if (settlement.passengerPaidAt != null && settlement.driverConfirmedAt == null) {
                        HopButton(
                            text = "Confirm Received",
                            onClick = { onEvent(SettlementEvent.ConfirmReceived) },
                            isLoading = state.isConfirming,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HopButton(
                            text = "Dispute",
                            onClick = { showDisputeSheet = !showDisputeSheet },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

// Previews

private val previewDriverSettlement = RideSettlement(
    bookingId = "bk-preview-01",
    suggestedAmountOere = 21_600,
    mobilepayNumber = "87654321",
    passengerPaidAt = null,
    driverConfirmedAt = null,
    disputedAt = null,
    disputeReason = null,
    createdAt = "2026-05-17T08:00:00Z",
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-SE-02 - Waiting for payment")
@Composable
private fun PreviewDriverSettlementWaiting() {
    HopTheme {
        DriverSettlementScreen(
            state = SettlementUiState(settlement = previewDriverSettlement),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-SE-02 - Passenger paid, confirm pending")
@Composable
private fun PreviewDriverSettlementConfirmPending() {
    HopTheme {
        DriverSettlementScreen(
            state = SettlementUiState(
                settlement = previewDriverSettlement.copy(passengerPaidAt = "2026-05-17T09:05:00Z"),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-SE-02 - Loading")
@Composable
private fun PreviewDriverSettlementLoading() {
    HopTheme {
        DriverSettlementScreen(
            state = SettlementUiState(isLoading = true),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

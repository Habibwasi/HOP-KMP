package com.example.hop.ui.screens.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.RideSettlement
import com.example.hop.presentation.settlement.SettlementEffect
import com.example.hop.presentation.settlement.SettlementEvent
import com.example.hop.presentation.settlement.SettlementUiState
import com.example.hop.presentation.settlement.SettlementViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PassengerSettlementRoute(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onSettlementComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettlementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

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
                is SettlementEffect.OpenMobilepayDeeplink -> {
                    runCatching { uriHandler.openUri(effect.uri) }
                        .onFailure { snackbarHostState.showSnackbar("MobilePay not installed") }
                }
                is SettlementEffect.PaymentMarkedSuccess -> onSettlementComplete()
                is SettlementEffect.ConfirmReceivedSuccess -> onSettlementComplete()
                is SettlementEffect.DisputeSubmittedSuccess ->
                    snackbarHostState.showSnackbar("Dispute submitted")
                is SettlementEffect.UnmarkPaidSuccess ->
                    snackbarHostState.showSnackbar("Payment unmarked — you can retry MobilePay")
                is SettlementEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PassengerSettlementScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun PassengerSettlementScreen(
    state: SettlementUiState,
    onEvent: (SettlementEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HopColors.authTextPrimary)
            }
            Text(
                "Pay Your Driver",
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
                        Text("Amount to send", style = MaterialTheme.typography.labelMedium, color = HopColors.authTextSecondary)
                        Text(
                            "DKK ${settlement.suggestedAmountOere / 100}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = HopMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = HopColors.primaryLime,
                        )
                        Text(
                            "SKAT-suggested rate - send directly to driver MobilePay",
                            style = MaterialTheme.typography.bodySmall,
                            color = HopColors.authTextSecondary,
                        )
                    }

                    // MobilePay number card with copy button
                    val clipboardManager = LocalClipboardManager.current
                    val scope = rememberCoroutineScope()
                    var copied by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HopColors.authInputSurface)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(settlement.mobilepayNumber))
                                scope.launch {
                                    copied = true
                                    delay(2000)
                                    copied = false
                                }
                            }
                            .padding(HopSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(HopSpacing.xs),
                    ) {
                        Text("Driver MobilePay", style = MaterialTheme.typography.labelMedium, color = HopColors.authTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                settlement.mobilepayNumber,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = HopMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = HopColors.authTextPrimary,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = if (copied) "Copied" else "Copy number",
                                    tint = if (copied) HopColors.primaryLime else HopColors.authTextSecondary,
                                )
                                Text(
                                    if (copied) "Copied!" else "Copy",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (copied) HopColors.primaryLime else HopColors.authTextSecondary,
                                )
                            }
                        }
                    }

                    // Instruction hint
                    Text(
                        "Open MobilePay → paste the number → send DKK ${settlement.suggestedAmountOere / 100}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HopColors.authTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (settlement.passengerPaidAt != null) {
                        Text(
                            "You marked this as paid - waiting for driver to confirm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HopColors.primaryLime,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HopColors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                ) {
                    if (settlement.passengerPaidAt == null) {
                        HopButton(
                            text = "Open MobilePay",
                            onClick = { onEvent(SettlementEvent.OpenMobilepay) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        HopButton(
                            text = "I Have Paid",
                            onClick = { onEvent(SettlementEvent.MarkPaid) },
                            variant = HopButtonVariant.Ghost,
                            isLoading = state.isMarkingPaid,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            "Waiting for driver to confirm payment receipt...",
                            style = MaterialTheme.typography.bodySmall,
                            color = HopColors.authTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Only show undo if driver hasn't confirmed yet
                        if (settlement.driverConfirmedAt == null) {
                            Spacer(Modifier.height(4.dp))
                            HopButton(
                                text = "I Haven't Paid Yet",
                                onClick = { onEvent(SettlementEvent.UnmarkPaid) },
                                variant = HopButtonVariant.Ghost,
                                isLoading = state.isUnmarkingPaid,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Previews

private val previewSettlement = RideSettlement(
    bookingId = "bk-preview-01",
    tripId = null,
    suggestedAmountOere = 17_300,
    mobilepayNumber = "12345678",
    passengerPaidAt = null,
    driverConfirmedAt = null,
    disputedAt = null,
    disputeReason = null,
    createdAt = "2026-05-17T08:00:00Z",
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "PA-SE-01 - Awaiting payment")
@Composable
private fun PreviewPassengerSettlementPending() {
    HopTheme {
        PassengerSettlementScreen(
            state = SettlementUiState(settlement = previewSettlement),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "PA-SE-01 - Marked as paid")
@Composable
private fun PreviewPassengerSettlementPaid() {
    HopTheme {
        PassengerSettlementScreen(
            state = SettlementUiState(
                settlement = previewSettlement.copy(passengerPaidAt = "2026-05-17T09:00:00Z"),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "PA-SE-01 - Loading")
@Composable
private fun PreviewPassengerSettlementLoading() {
    HopTheme {
        PassengerSettlementScreen(
            state = SettlementUiState(isLoading = true),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

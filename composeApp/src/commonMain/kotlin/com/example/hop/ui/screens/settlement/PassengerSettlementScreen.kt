package com.example.hop.ui.screens.settlement

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.settlement.SettlementEffect
import com.example.hop.presentation.settlement.SettlementEvent
import com.example.hop.presentation.settlement.SettlementViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
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

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettlementEffect.OpenMobilepayDeeplink -> {
                    runCatching { uriHandler.openUri(effect.uri) }
                        .onFailure { snackbarHostState.showSnackbar("MobilePay not installed") }
                }
                is SettlementEffect.PaymentMarkedSuccess ->
                    snackbarHostState.showSnackbar("Marked as paid — waiting for driver confirmation")
                is SettlementEffect.ConfirmReceivedSuccess -> onSettlementComplete()
                is SettlementEffect.DisputeSubmittedSuccess ->
                    snackbarHostState.showSnackbar("Dispute submitted")
                is SettlementEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HopColors.background)
                .statusBarsPadding()
                .padding(innerPadding),
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
                                "SKAT-suggested rate · send directly to driver's MobilePay",
                                style = MaterialTheme.typography.bodySmall,
                                color = HopColors.authTextSecondary,
                            )
                        }

                        // MobilePay number info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(HopColors.authInputSurface)
                                .padding(HopSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(HopSpacing.xs),
                        ) {
                            Text("Driver MobilePay", style = MaterialTheme.typography.labelMedium, color = HopColors.authTextSecondary)
                            Text(
                                settlement.mobilepayNumber,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = HopMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = HopColors.authTextPrimary,
                            )
                        }

                        if (settlement.passengerPaidAt != null) {
                            Text(
                                "✓ You marked this as paid — waiting for driver to confirm",
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
                                onClick = { viewModel.onEvent(SettlementEvent.OpenMobilepay) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(4.dp))
                            HopButton(
                                text = "I Have Paid",
                                onClick = { viewModel.onEvent(SettlementEvent.MarkPaid) },
                                isLoading = state.isMarkingPaid,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(
                                "Waiting for driver to confirm payment receipt…",
                                style = MaterialTheme.typography.bodySmall,
                                color = HopColors.authTextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

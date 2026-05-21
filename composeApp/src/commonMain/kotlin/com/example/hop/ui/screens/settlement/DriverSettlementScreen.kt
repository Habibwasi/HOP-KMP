package com.example.hop.ui.screens.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripSettlementEntry
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
    tripId: String,
    onNavigateBack: () -> Unit,
    onSettlementComplete: () -> Unit,
    /** When non-null, the screen resolves tripId from this bookingId first (notification deep link). */
    bookingIdForResolution: String? = null,
    modifier: Modifier = Modifier,
    viewModel: SettlementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId, bookingIdForResolution) {
        if (bookingIdForResolution != null) {
            viewModel.onEvent(SettlementEvent.LoadForTripByBooking(bookingIdForResolution))
        } else {
            viewModel.onEvent(SettlementEvent.LoadForTrip(tripId))
        }
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                )
            }
            Text(
                "Payment Confirmation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
            )
        }

        when {
            state.isLoading -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = HopColors.primaryLime,
                )
                Spacer(Modifier.weight(1f))
            }

            state.entries.isEmpty() -> {
                Spacer(Modifier.weight(1f))
                Text(
                    "No passengers found for this trip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HopColors.authTextSecondary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(HopSpacing.md),
                )
                Spacer(Modifier.weight(1f))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(HopSpacing.md),
                ) {
                    items(state.entries, key = { it.bookingId }) { entry ->
                        PassengerSettlementCard(
                            entry = entry,
                            isConfirming = state.confirmingBookingId == entry.bookingId,
                            onConfirm = { onEvent(SettlementEvent.ConfirmReceivedForBooking(entry.bookingId)) },
                        )
                    }
                }

                // Summary footer
                val allPaid = state.entries.all { it.paymentStatus != TripSettlementEntry.PaymentStatus.WAITING }
                val waitingCount = state.entries.count { it.paymentStatus == TripSettlementEntry.PaymentStatus.WAITING }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HopColors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                ) {
                    Text(
                        text = if (waitingCount == 0) "All passengers have marked as paid"
                               else "$waitingCount passenger${if (waitingCount > 1) "s" else ""} still to pay",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (allPaid) HopColors.primaryLime else HopColors.authTextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PassengerSettlementCard(
    entry: TripSettlementEntry,
    isConfirming: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = when (entry.paymentStatus) {
        TripSettlementEntry.PaymentStatus.CONFIRMED -> HopColors.primaryLime
        TripSettlementEntry.PaymentStatus.PAID -> Color(0xFFFFA726)   // amber
        TripSettlementEntry.PaymentStatus.WAITING -> HopColors.authTextSecondary
    }
    val statusLabel = when (entry.paymentStatus) {
        TripSettlementEntry.PaymentStatus.CONFIRMED -> "Confirmed"
        TripSettlementEntry.PaymentStatus.PAID -> "Marked paid"
        TripSettlementEntry.PaymentStatus.WAITING -> "Waiting"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
            ) {
                // Initials avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HopColors.primaryLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.passengerInitials,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = HopColors.primaryLime,
                    )
                }
                Column {
                    Text(
                        text = entry.passengerName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = HopColors.authTextPrimary,
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                    )
                }
            }
            Text(
                text = formatDkk(entry.suggestedAmountOere),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = HopMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = HopColors.authTextPrimary,
            )
        }

        if (entry.paymentStatus == TripSettlementEntry.PaymentStatus.PAID) {
            HopButton(
                text = "Confirm Received",
                onClick = onConfirm,
                isLoading = isConfirming,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun formatDkk(oere: Int): String {
    val kr = oere / 100
    val rem = oere % 100
    return if (rem == 0) "DKK $kr" else "DKK $kr,${rem.toString().padStart(2, '0')}"
}

// Previews

private val previewEntries = listOf(
    TripSettlementEntry(
        bookingId = "bk-01",
        passengerFirstName = "Anna",
        passengerLastName = "Nielsen",
        suggestedAmountOere = 17_300,
        passengerPaidAt = "2026-05-17T09:00:00Z",
        driverConfirmedAt = null,
        bookingStatus = "AWAITING_PAYMENT",
    ),
    TripSettlementEntry(
        bookingId = "bk-02",
        passengerFirstName = "Lars",
        passengerLastName = "Madsen",
        suggestedAmountOere = 17_300,
        passengerPaidAt = null,
        driverConfirmedAt = null,
        bookingStatus = "AWAITING_PAYMENT",
    ),
    TripSettlementEntry(
        bookingId = "bk-03",
        passengerFirstName = "Mia",
        passengerLastName = "Hansen",
        suggestedAmountOere = 17_300,
        passengerPaidAt = "2026-05-17T08:50:00Z",
        driverConfirmedAt = "2026-05-17T09:30:00Z",
        bookingStatus = "COMPLETED",
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-SE-02 - Mixed payment status")
@Composable
private fun PreviewDriverSettlementMixed() {
    HopTheme {
        DriverSettlementScreen(
            state = SettlementUiState(entries = previewEntries),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-SE-02 - All waiting")
@Composable
private fun PreviewDriverSettlementAllWaiting() {
    HopTheme {
        DriverSettlementScreen(
            state = SettlementUiState(
                entries = previewEntries.map { it.copy(passengerPaidAt = null, driverConfirmedAt = null) },
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

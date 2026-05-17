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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripSettlementEntry
import com.example.hop.presentation.settlement.SettlementEffect
import com.example.hop.presentation.settlement.SettlementEvent
import com.example.hop.presentation.settlement.SettlementUiState
import com.example.hop.presentation.settlement.SettlementViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PastTripDetailDriverRoute(
    tripId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettlementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId) {
        viewModel.onEvent(SettlementEvent.LoadForTrip(tripId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
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
        PastTripDetailDriverScreen(
            state = state,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun PastTripDetailDriverScreen(
    state: SettlementUiState,
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
                "Trip Details",
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
                    "No passenger data for this trip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HopColors.authTextSecondary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(HopSpacing.md),
                )
                Spacer(Modifier.weight(1f))
            }

            else -> {
                // Earnings summary
                val totalOere = state.entries.sumOf { it.suggestedAmountOere }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Total earnings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HopColors.authTextSecondary,
                    )
                    Text(
                        "DKK ${totalOere / 100}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = HopMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = HopColors.primaryLime,
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(HopSpacing.md),
                ) {
                    items(state.entries, key = { it.bookingId }) { entry ->
                        PastPassengerRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun PastPassengerRow(
    entry: TripSettlementEntry,
    modifier: Modifier = Modifier,
) {
    val statusColor = when (entry.paymentStatus) {
        TripSettlementEntry.PaymentStatus.CONFIRMED -> HopColors.primaryLime
        TripSettlementEntry.PaymentStatus.PAID -> Color(0xFFFFA726)
        TripSettlementEntry.PaymentStatus.WAITING -> HopColors.authTextSecondary
    }
    val statusLabel = when (entry.paymentStatus) {
        TripSettlementEntry.PaymentStatus.CONFIRMED -> "Confirmed"
        TripSettlementEntry.PaymentStatus.PAID -> "Marked paid"
        TripSettlementEntry.PaymentStatus.WAITING -> "Waiting"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.authInputSurface)
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
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
            text = "DKK ${entry.suggestedAmountOere / 100}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = HopMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
            ),
            color = HopColors.authTextPrimary,
        )
    }
}

// Previews

private val previewEntries = listOf(
    TripSettlementEntry(
        bookingId = "bk-01",
        passengerFirstName = "Anna",
        passengerLastName = "Nielsen",
        suggestedAmountOere = 17_300,
        passengerPaidAt = "2026-05-17T09:00:00Z",
        driverConfirmedAt = "2026-05-17T09:30:00Z",
        bookingStatus = "COMPLETED",
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
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-09b - Past Trip Detail Driver")
@Composable
private fun PreviewPastTripDetailDriver() {
    HopTheme {
        PastTripDetailDriverScreen(
            state = SettlementUiState(entries = previewEntries),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DR-09b - Loading")
@Composable
private fun PreviewPastTripDetailDriverLoading() {
    HopTheme {
        PastTripDetailDriverScreen(
            state = SettlementUiState(isLoading = true),
            onNavigateBack = {},
        )
    }
}

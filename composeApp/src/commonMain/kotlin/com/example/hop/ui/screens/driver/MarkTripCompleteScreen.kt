package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-11 — Mark Trip Complete confirmation Route.
 *
 * Shown before the driver finalises trip completion. Fires
 * [DriverEvent.CompleteTrip] on confirm, navigates back on cancel.
 *
 * The [DriverEffect.NavigateToRatePassenger] effect produced by CompleteTrip
 * is handled here and forwarded to [onNavigateToRatePassenger].
 *
 * @param tripId          The trip being completed.
 * @param driverNetOere   Total payout amount in øre to display.
 */
@Composable
fun MarkTripCompleteRoute(
    tripId: String,
    driverNetOere: Int,
    onNavigateBack: () -> Unit,
    onNavigateToRatePassenger: (bookingId: String, passengerName: String, passengerInitials: String) -> Unit,
    onNavigateToDriverSettlement: (bookingId: String) -> Unit,
    onNavigateToMyTrips: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load passengers so completeTrip() can resolve firstBookingId for rating.
    LaunchedEffect(tripId) {
        viewModel.onEvent(DriverEvent.LoadActiveTripDetail(tripId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToRatePassenger -> {
                    val passenger = state.activeTripDetail.passengers
                        .firstOrNull { it.bookingId == effect.bookingId }
                    onNavigateToRatePassenger(
                        effect.bookingId,
                        passenger?.fullName ?: "",
                        passenger?.initials ?: "",
                    )
                }
                is DriverEffect.NavigateToDriverSettlement -> onNavigateToDriverSettlement(effect.bookingId)
                is DriverEffect.NavigateToMyTrips -> onNavigateToMyTrips()
                is DriverEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        MarkTripCompleteScreen(
            driverNetOere = driverNetOere,
            isLoading = state.isLoading,
            onConfirm = { viewModel.onEvent(DriverEvent.CompleteTrip(tripId)) },
            onCancel = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-11 — Stateless confirmation screen before marking a trip complete.
 */
@Composable
fun MarkTripCompleteScreen(
    driverNetOere: Int,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payoutDkk = driverNetOere / 100

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.semantics { contentDescription = "Go back" },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary,
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Are you sure?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HopColors.authTextPrimary,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(HopSpacing.md))

            Text(
                text = "Marking this trip complete will release payments to your account.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = HopColors.authTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // ── Payout amount card ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = HopColors.authInputBorder,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(HopSpacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Payout amount",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HopColors.authTextSecondary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(HopSpacing.xs))
                    Text(
                        text = "DKK $payoutDkk",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = HopMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = HopColors.primaryLime,
                            fontSize = 36.sp,
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Payout amount: $payoutDkk Danish Kroner"
                        },
                    )
                }
            }
        }

        // ── Bottom actions ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md)
                .padding(bottom = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            HopButton(
                text = "Confirm Complete",
                onClick = onConfirm,
                enabled = !isLoading,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Confirm trip completion" },
            )
            HopButton(
                text = "Cancel",
                onClick = onCancel,
                variant = HopButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MarkTripCompleteScreenPreview() {
    HopTheme {
        MarkTripCompleteScreen(
            driverNetOere = 69_312,
            isLoading = false,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Loading")
@Composable
private fun MarkTripCompleteScreenLoadingPreview() {
    HopTheme {
        MarkTripCompleteScreen(
            driverNetOere = 69_312,
            isLoading = true,
            onConfirm = {},
            onCancel = {},
        )
    }
}

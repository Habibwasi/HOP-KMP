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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.driver.ActiveTripDetailUiState
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.ui.components.AvatarSize
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.HopAvatar
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.StarRating
import com.example.hop.ui.components.StatusBadge
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import com.example.hop.domain.model.Trip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-10 — Trip Detail (Active, Driver) Route.
 *
 * Loads the trip and its passengers via [DriverEvent.LoadActiveTripDetail], then
 * delegates rendering to the stateless [TripDetailActiveDriverScreen].
 *
 * On "Mark Trip Complete" tap → navigates to DR-11 [MarkTripCompleteScreen].
 * On complete-trip effect → navigates to DR-12 [RatePassengerScreen].
 */
@Composable
fun TripDetailActiveDriverRoute(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMarkTripComplete: (tripId: String, driverNetOere: Int) -> Unit,
    onNavigateToRatePassenger: (bookingId: String, passengerName: String, passengerInitials: String) -> Unit,
    onNavigateToChat: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        TripDetailActiveDriverScreen(
            tripId = tripId,
            detailState = state.activeTripDetail,
            isCompletingTrip = state.isLoading,
            onBack = onNavigateBack,
            onMarkTripComplete = {
                val trip = state.activeTripDetail.trip ?: return@TripDetailActiveDriverScreen
                onNavigateToMarkTripComplete(tripId, trip.trip.driverNetOere)
            },
            onMessagePassenger = onNavigateToChat,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-10 — Stateless renderer for the driver's active trip detail.
 */
@Composable
fun TripDetailActiveDriverScreen(
    tripId: String,
    detailState: ActiveTripDetailUiState,
    isCompletingTrip: Boolean,
    onBack: () -> Unit,
    onMarkTripComplete: () -> Unit,
    onMessagePassenger: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "Go back" },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary,
                )
            }
            Text(
                text = "Active Trip",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        when {
            detailState.isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = HopColors.primaryLime)
                }
            }

            detailState.error != null -> {
                val errorMessage: String = detailState.error!!
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = HopColors.error,
                        ),
                        modifier = Modifier.padding(horizontal = HopSpacing.md),
                    )
                }
            }

            detailState.trip != null -> {
                val trip = detailState.trip!!
                val domainTrip = trip.trip
                val isConfirmed = domainTrip.status == TripStatus.CONFIRMED || domainTrip.status == TripStatus.ACTIVE

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    // ── Trip header ───────────────────────────────────────────
                    item {
                        TripHeaderSection(
                            originName = domainTrip.originName,
                            destName = domainTrip.destName,
                            departsAt = domainTrip.departsAt,
                            status = domainTrip.status,
                            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                        )
                        HorizontalDivider(color = HopColors.authInputBorder, thickness = 1.dp)
                    }

                    // ── Seats summary ─────────────────────────────────────────
                    item {
                        SeatsSummarySection(
                            seatsBooked = domainTrip.seatsBooked,
                            seatsTotal = domainTrip.seatsTotal,
                            minThreshold = domainTrip.minThreshold,
                            model = domainTrip.model,
                            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                        )
                        HorizontalDivider(color = HopColors.authInputBorder, thickness = 1.dp)
                    }

                    // ── Passengers header ─────────────────────────────────────
                    item {
                        Text(
                            text = "Passengers",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = HopColors.authTextPrimary,
                            ),
                            modifier = Modifier.padding(
                                horizontal = HopSpacing.md,
                                vertical = HopSpacing.sm,
                            ),
                        )
                    }

                    if (detailState.passengers.isEmpty()) {
                        item {
                            Text(
                                text = "No passengers booked yet.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = HopColors.authTextSecondary,
                                ),
                                modifier = Modifier.padding(
                                    horizontal = HopSpacing.md,
                                    vertical = HopSpacing.sm,
                                ),
                            )
                        }
                    } else {
                        items(
                            items = detailState.passengers,
                            key = { it.bookingId },
                        ) { passenger ->
                            PassengerRow(
                                passenger = passenger,
                                onMessage = { onMessagePassenger(passenger.bookingId) },
                                modifier = Modifier.padding(
                                    horizontal = HopSpacing.md,
                                    vertical = HopSpacing.sm,
                                ),
                            )
                            HorizontalDivider(
                                color = HopColors.authInputBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = HopSpacing.md),
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(HopSpacing.xl)) }
                }

                // ── Bottom action ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HopSpacing.md)
                        .padding(bottom = HopSpacing.md),
                ) {
                    HopButton(
                        text = "Mark Trip Complete",
                        onClick = onMarkTripComplete,
                        enabled = isConfirmed && !isCompletingTrip,
                        isLoading = isCompletingTrip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Mark trip as complete" },
                    )
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun TripHeaderSection(
    originName: String,
    destName: String,
    departsAt: String,
    status: TripStatus,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Text(
                text = "$originName → $destName",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HopColors.authTextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            val badgeType = when (status) {
                TripStatus.CONFIRMED -> BadgeType.Confirmed
                TripStatus.ACTIVE -> BadgeType.Custom(
                    label = "ACTIVE",
                    background = HopColors.primaryLime,
                    contentColor = HopColors.authTextPrimary,
                )
                TripStatus.COMPLETED -> BadgeType.Completed
                TripStatus.CANCELLED -> BadgeType.Cancelled
                TripStatus.THRESHOLD_NOT_MET -> BadgeType.Custom(
                    label = "Threshold not met",
                    background = HopColors.warning.copy(alpha = 0.15f),
                    contentColor = HopColors.warning,
                )
                TripStatus.UNKNOWN -> BadgeType.Pending
            }
            StatusBadge(type = badgeType)
        }
        Spacer(modifier = Modifier.height(HopSpacing.xs))
        Text(
            text = "Departs: $departsAt",
            style = MaterialTheme.typography.bodySmall.copy(
                color = HopColors.authTextSecondary,
            ),
        )
    }
}

@Composable
private fun SeatsSummarySection(
    seatsBooked: Int,
    seatsTotal: Int,
    minThreshold: Int?,
    model: TripModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$seatsBooked of $seatsTotal seats booked",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = HopColors.authTextPrimary,
            ),
        )

        // Model B threshold progress bar
        if (model == TripModel.B && minThreshold != null && minThreshold > 0) {
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            val progress = (seatsBooked.toFloat() / minThreshold.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = HopColors.primaryLime,
                trackColor = HopColors.authInputBorder,
            )
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            Text(
                text = "Threshold: $minThreshold seats needed",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HopColors.authTextSecondary,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun PassengerRow(
    passenger: PassengerSummary,
    onMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HopAvatar(
            initials = passenger.initials,
            size = AvatarSize.Sm,
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = passenger.fullName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = HopColors.authTextPrimary,
                ),
            )
            if (passenger.rating > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    StarRating(
                        rating = passenger.rating,
                        starSize = 10.dp,
                    )
                    Text(
                        text = String.format("%.1f", passenger.rating),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HopColors.authTextSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            } else {
                Text(
                    text = "No ratings yet",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HopColors.authTextSecondary,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
        IconButton(
            onClick = onMessage,
            modifier = Modifier.semantics { contentDescription = "Message ${passenger.fullName}" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TripDetailActiveDriverScreenPreview() {
    HopTheme {
        TripDetailActiveDriverScreen(
            tripId = "trip-1",
            detailState = ActiveTripDetailUiState(
                isLoading = false,
                trip = TripUiModel(
                    trip = Trip(
                        id = "trip-1",
                        driverId = "driver-1",
                        model = TripModel.B,
                        originName = "Aarhus",
                        originLat = 56.15,
                        originLng = 10.21,
                        destName = "Copenhagen",
                        destLat = 55.67,
                        destLng = 12.56,
                        distanceMetres = 304_000,
                        departsAt = "2026-04-20T08:00:00Z",
                        seatsTotal = 4,
                        seatsBooked = 2,
                        minThreshold = 3,
                        priceOerePerSeat = 20_386,
                        driverNetOere = 17_328,
                        status = TripStatus.CONFIRMED,
                        recurrenceDays = null,
                    )
                ),
                passengers = listOf(
                    PassengerSummary(
                        bookingId = "b-1",
                        passengerId = "p-1",
                        fullName = "Mette Andersen",
                        rating = 4.5f,
                        seats = 1,
                    ),
                    PassengerSummary(
                        bookingId = "b-2",
                        passengerId = "p-2",
                        fullName = "Jonas Holm",
                        rating = 0f,
                        seats = 1,
                    ),
                ),
            ),
            isCompletingTrip = false,
            onBack = {},
            onMarkTripComplete = {},
            onMessagePassenger = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Loading")
@Composable
private fun TripDetailActiveDriverLoadingPreview() {
    HopTheme {
        TripDetailActiveDriverScreen(
            tripId = "trip-1",
            detailState = ActiveTripDetailUiState(isLoading = true),
            isCompletingTrip = false,
            onBack = {},
            onMarkTripComplete = {},
            onMessagePassenger = {},
        )
    }
}

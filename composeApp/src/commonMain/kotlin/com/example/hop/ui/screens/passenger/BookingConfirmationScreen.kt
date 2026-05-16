package com.example.hop.ui.screens.passenger

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
import com.example.hop.presentation.booking.BookingEffect
import com.example.hop.presentation.booking.BookingEvent
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.tripdetail.TripDetailEvent
import com.example.hop.presentation.tripdetail.TripDetailUiState
import com.example.hop.presentation.tripdetail.TripDetailViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-04 — Booking Confirmation Route.
 *
 * Wires [TripDetailViewModel] (trip data display) and [BookingViewModel]
 * (booking creation / payment) from Koin, collects effects, and delegates
 * rendering to the stateless [BookingConfirmationScreen].
 */
@Composable
fun BookingConfirmationRoute(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    tripDetailViewModel: TripDetailViewModel = koinViewModel(),
    bookingViewModel: BookingViewModel = koinViewModel(),
) {
    val tripState by tripDetailViewModel.state.collectAsStateWithLifecycle()
    val bookingState by bookingViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId) {
        tripDetailViewModel.onEvent(TripDetailEvent.LoadTrip(tripId))
    }

    LaunchedEffect(bookingViewModel) {
        bookingViewModel.effect.collectLatest { effect ->
            when (effect) {
                is BookingEffect.NavigateToSuccess ->
                    onNavigateToSuccess(effect.bookingId)
                is BookingEffect.NavigateToCancellationConfirmation -> Unit
                is BookingEffect.NavigateToMyTripsPassenger -> Unit
                is BookingEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        BookingConfirmationScreen(
            tripState = tripState,
            isProcessing = bookingState.isLoading,
            onBack = onNavigateBack,
            onConfirm = {
                bookingViewModel.onEvent(
                    BookingEvent.CreateBooking(tripId = tripId, seats = 1),
                )
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-04 — Booking Confirmation Screen.
 *
 * Stateless renderer. Renders:
 *  - Top bar with back arrow and "Confirm Booking" title
 *  - Trip summary card: route (origin → destination), departure date/time,
 *    driver name, and seats being booked
 *  - Price summary card: total in DKK, per-seat cost, and platform fee breakdown
 *  - Model B pending notice: "This trip needs X more seats to be confirmed"
 *    with a threshold progress bar (hidden for Model A)
 *  - "Pay with MobilePay" primary button pinned to the bottom — shows a loading
 *    spinner while [isProcessing] is true
 */
@Composable
fun BookingConfirmationScreen(
    tripState: TripDetailUiState,
    isProcessing: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        ConfirmationTopBar(onBack = onBack)

        when {
            tripState.isLoading -> ConfirmationLoading(modifier = Modifier.weight(1f))
            else -> {
                // Scrollable content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = HopSpacing.md,
                    ),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(HopSpacing.md))
                        TripSummaryCard(
                            originName = tripState.originName,
                            destName = tripState.destName,
                            departsAt = tripState.departsAt,
                            driverName = tripState.driverName,
                            seats = 1,
                            modifier = Modifier.padding(horizontal = HopSpacing.md),
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(HopSpacing.md))
                        PriceSummaryCard(
                            priceOerePerSeat = tripState.priceOerePerSeat,
                            seats = 1,
                            modifier = Modifier.padding(horizontal = HopSpacing.md),
                        )
                    }

                    val minThreshold = tripState.minThreshold
                    if (tripState.model == TripModel.B &&
                        minThreshold != null &&
                        minThreshold > 0
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(HopSpacing.md))
                            ModelBNoticeCard(
                                seatsBooked = tripState.seatsBooked,
                                minThreshold = minThreshold,
                                modifier = Modifier.padding(horizontal = HopSpacing.md),
                            )
                        }
                    }
                }

                // Pinned MobilePay button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HopColors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                ) {
                    HopButton(
                        text = "Confirm Booking",
                        onClick = onConfirm,
                        isLoading = isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Confirm Booking" },
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmationTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.semantics { contentDescription = "Back" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = HopColors.authTextPrimary,
            )
        }
        Text(
            text = "Confirm Booking",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.authTextPrimary,
        )
    }
}

// ── Trip summary card ─────────────────────────────────────────────────────────

@Composable
private fun TripSummaryCard(
    originName: String,
    destName: String,
    departsAt: String,
    driverName: String,
    seats: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "Trip Summary",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Route
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.padding(top = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HopColors.primaryLime),
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(HopColors.textSecondary.copy(alpha = 0.4f)),
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HopColors.textSecondary),
                )
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = originName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = HopColors.authTextPrimary,
                )
                Text(
                    text = destName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = HopColors.authTextPrimary,
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = HopColors.authTextSecondary.copy(alpha = 0.15f),
        )

        // Meta row: departure · driver · seats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.lg),
        ) {
            SummaryMetaItem(label = "Departs", value = departsAt)
            SummaryMetaItem(label = "Driver", value = driverName)
            SummaryMetaItem(
                label = "Seats",
                value = if (seats == 1) "1 seat" else "$seats seats",
            )
        }
    }
}

@Composable
private fun SummaryMetaItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HopColors.authTextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = HopColors.authTextPrimary,
        )
    }
}

// ── Price summary card ────────────────────────────────────────────────────────

@Composable
private fun PriceSummaryCard(
    priceOerePerSeat: Int,
    seats: Int,
    modifier: Modifier = Modifier,
) {
    val totalOere = priceOerePerSeat * seats

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "Price Summary",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
            )
            Text(
                text = "DKK ${totalOere / 100}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = HopMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = HopColors.primaryLime,
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = HopColors.authTextSecondary.copy(alpha = 0.15f),
        )

        // Breakdown
        PriceBreakdownRow(
            label = "${seats}× SKAT-rate per seat",
            valueOere = priceOerePerSeat * seats,
        )
        PriceBreakdownRow(
            label = "Pay driver via MobilePay after ride",
            valueOere = 0,
        )
    }
}

@Composable
private fun PriceBreakdownRow(label: String, valueOere: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
        )
        Text(
            text = "DKK ${valueOere / 100}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = HopMonoFontFamily,
                fontWeight = FontWeight.Medium,
            ),
            color = HopColors.authTextSecondary,
        )
    }
}

// ── Model B notice card ───────────────────────────────────────────────────────

@Composable
private fun ModelBNoticeCard(
    seatsBooked: Int,
    minThreshold: Int,
    modifier: Modifier = Modifier,
) {
    // Account for the 1 seat the user is about to book.
    val seatsAfterBooking = seatsBooked + 1
    val seatsNeeded = (minThreshold - seatsAfterBooking).coerceAtLeast(0)
    val progressAfterBooking = (seatsAfterBooking.toFloat() / minThreshold.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressAfterBooking,
        animationSpec = tween(durationMillis = 600),
        label = "modelBThresholdProgress",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.warning.copy(alpha = 0.12f))
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(HopColors.warning),
            )
            Text(
                text = "Long Distance Trip",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.warning,
            )
        }

        val noticeText = when {
            seatsNeeded == 0 -> "Your booking meets the minimum — trip confirms soon"
            seatsNeeded == 1 -> "This trip needs 1 more seat to be confirmed"
            else -> "This trip needs $seatsNeeded more seats to be confirmed"
        }
        Text(
            text = noticeText,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextPrimary,
        )

        Text(
            text = "Payment is held until the trip is confirmed. You'll be refunded if the trip is cancelled.",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Threshold progress showing seats filled including the user's upcoming booking
        Text(
            text = "$seatsAfterBooking of $minThreshold seats after your booking",
            style = MaterialTheme.typography.labelSmall,
            color = HopColors.authTextSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = HopColors.warning,
            trackColor = HopColors.surface.copy(alpha = 0.5f),
            strokeCap = StrokeCap.Round,
        )
    }
}

// ── MobilePay logo badge ──────────────────────────────────────────────────────

/** Inline MobilePay brand badge shown as a leading icon on the payment button. */
@Composable
private fun MobilePayLogo(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 34.dp, height = 20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF008FE8)),
    ) {
        Text(
            text = "MP",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 0.sp,
            ),
            color = Color.White,
        )
    }
}

// ── Loading state ─────────────────────────────────────────────────────────────

@Composable
private fun ConfirmationLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = HopColors.primaryLime,
            modifier = Modifier.size(36.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewTripState(
    model: TripModel = TripModel.A,
    seatsBooked: Int = 1,
    minThreshold: Int? = null,
    isLoading: Boolean = false,
) = TripDetailUiState(
    isLoading = isLoading,
    tripId = "trip-preview",
    driverId = "driver-123",
    driverName = "Anders Nielsen",
    driverInitials = "AN",
    driverRating = 4.8f,
    isDriverVerified = true,
    originName = "Copenhagen H",
    destName = "Aarhus C",
    departsAt = "08:30",
    distanceMetres = 304_000,
    seatsTotal = 4,
    seatsBooked = seatsBooked,
    minThreshold = minThreshold,
    model = model,
    priceOerePerSeat = 17_328,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookingConfirmationModelAPreview() {
    HopTheme {
        BookingConfirmationScreen(
            tripState = previewTripState(model = TripModel.A),
            isProcessing = false,
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookingConfirmationModelBPreview() {
    HopTheme {
        BookingConfirmationScreen(
            tripState = previewTripState(
                model = TripModel.B,
                seatsBooked = 1,
                minThreshold = 4,
            ),
            isProcessing = false,
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookingConfirmationModelBThresholdMetPreview() {
    HopTheme {
        BookingConfirmationScreen(
            tripState = previewTripState(
                model = TripModel.B,
                seatsBooked = 3,
                minThreshold = 4,
            ),
            isProcessing = false,
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookingConfirmationProcessingPreview() {
    HopTheme {
        BookingConfirmationScreen(
            tripState = previewTripState(model = TripModel.A),
            isProcessing = true,
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookingConfirmationLoadingPreview() {
    HopTheme {
        BookingConfirmationScreen(
            tripState = previewTripState(isLoading = true),
            isProcessing = false,
            onBack = {},
            onConfirm = {},
        )
    }
}

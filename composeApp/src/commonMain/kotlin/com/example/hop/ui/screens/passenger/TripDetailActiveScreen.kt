package com.example.hop.ui.screens.passenger

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.hop.domain.model.BookingStatus
import com.example.hop.domain.model.TripModel
import com.example.hop.presentation.booking.BookingEffect
import com.example.hop.presentation.booking.BookingEvent
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.tripdetailactive.TripDetailActiveEffect
import com.example.hop.presentation.tripdetailactive.TripDetailActiveEvent
import com.example.hop.presentation.tripdetailactive.TripDetailActiveUiState
import com.example.hop.presentation.tripdetailactive.TripDetailActiveViewModel
import com.example.hop.ui.components.AvatarSize
import com.example.hop.ui.components.HopAvatar
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.components.StarRating
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-08 — Trip Detail Active Route.
 *
 * Wires [TripDetailActiveViewModel] (trip/booking display data) and
 * [BookingViewModel] (cancel action) from Koin. Handles one-shot navigation
 * effects and delegates rendering to the stateless [TripDetailActiveScreen].
 */
@Composable
fun TripDetailActiveRoute(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (bookingId: String) -> Unit,
    onNavigateToCancellationConfirmation: (bookingId: String) -> Unit,
    onNavigateToPassengerSettlement: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripDetailActiveViewModel = koinViewModel(),
    bookingViewModel: BookingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bookingState by bookingViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bookingId) {
        viewModel.onEvent(TripDetailActiveEvent.Load(bookingId))
    }

    // Re-fetch booking status when the screen resumes (e.g. user returns from
    // background after the driver completes the trip).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(TripDetailActiveEvent.Load(bookingId))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Auto-navigate to settlement as soon as status becomes AWAITING_PAYMENT.
    LaunchedEffect(state.bookingStatus) {
        if (state.bookingStatus == BookingStatus.AWAITING_PAYMENT) {
            onNavigateToPassengerSettlement(bookingId)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripDetailActiveEffect.NavigateToChat ->
                    onNavigateToChat(effect.bookingId)
                is TripDetailActiveEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    LaunchedEffect(bookingViewModel) {
        bookingViewModel.effect.collectLatest { effect ->
            when (effect) {
                is BookingEffect.NavigateToCancellationConfirmation ->
                    onNavigateToCancellationConfirmation(effect.bookingId)
                is BookingEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is BookingEffect.NavigateToSuccess -> Unit
                is BookingEffect.NavigateToMyTripsPassenger -> Unit     // not reachable here
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        TripDetailActiveScreen(
            state = state,
            isCancelling = bookingState.isLoading,
            onBack = onNavigateBack,
            onMessageDriver = { viewModel.onEvent(TripDetailActiveEvent.MessageDriver) },
            onPayDriver = { onNavigateToPassengerSettlement(bookingId) },
            onCancelBookingConfirmed = {
                bookingViewModel.onEvent(BookingEvent.CancelBooking(bookingId))
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * PA-08 — Trip Detail Active Screen.
 *
 * Stateless renderer. Shows live trip info for a confirmed/pending booking:
 *  - Driver avatar (80 dp) + name + phone tap-to-call + star rating
 *  - Route: origin → destination + departure time
 *  - Booking status badge (Confirmed green / Pending amber)
 *  - Model B pending: threshold progress bar + "X of Y seats confirmed"
 *  - "Message Driver" ghost button
 *  - "Cancel Booking" destructive button → confirm dialog before firing cancel
 */
@Composable
fun TripDetailActiveScreen(
    state: TripDetailActiveUiState,
    isCancelling: Boolean,
    onBack: () -> Unit,
    onMessageDriver: () -> Unit,
    onPayDriver: () -> Unit,
    onCancelBookingConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        ActiveTripTopBar(onBack = onBack)

        when {
            state.isLoading -> ActiveTripLoading(modifier = Modifier.weight(1f))
            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = HopSpacing.md),
                ) {
                    Spacer(modifier = Modifier.height(HopSpacing.md))

                    // Driver section
                    DriverInfoSection(
                        driverName = state.driverName,
                        driverInitials = state.driverInitials,
                        driverPhone = state.driverPhone,
                        driverRating = state.driverRating,
                        isVerified = state.isDriverVerified,
                    )

                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    HorizontalDivider(color = HopColors.surfaceElevated)
                    Spacer(modifier = Modifier.height(HopSpacing.lg))

                    // Route section
                    ActiveRouteSection(
                        originName = state.originName,
                        destName = state.destName,
                        departsAt = state.departsAt,
                    )

                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    HorizontalDivider(color = HopColors.surfaceElevated)
                    Spacer(modifier = Modifier.height(HopSpacing.lg))

                    // Booking status badge
                    BookingStatusBadge(status = state.bookingStatus)

                    // Model B threshold section
                    if (state.tripModel == TripModel.B &&
                        state.bookingStatus == BookingStatus.PENDING
                    ) {
                        Spacer(modifier = Modifier.height(HopSpacing.md))
                        ThresholdSection(
                            seatsBooked = state.seatsBooked,
                            minThreshold = state.minThreshold ?: 0,
                            progress = state.thresholdProgress,
                        )
                    }

                    Spacer(modifier = Modifier.height(HopSpacing.xl))
                }

                // Bottom action buttons
                ActionButtonsSection(
                    bookingStatus = state.bookingStatus,
                    isCancelling = isCancelling,
                    onMessageDriver = onMessageDriver,
                    onPayDriver = onPayDriver,
                    onCancelBooking = { showCancelDialog = true },
                    modifier = Modifier.padding(
                        horizontal = HopSpacing.md,
                        vertical = HopSpacing.md,
                    ),
                )
            }
        }
    }

    // Cancel confirm dialog
    if (showCancelDialog) {
        CancelBookingDialog(
            onConfirm = {
                showCancelDialog = false
                onCancelBookingConfirmed()
            },
            onDismiss = { showCancelDialog = false },
        )
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun ActiveTripTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = HopColors.authTextPrimary,
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = "My Trip",
            style = MaterialTheme.typography.titleMedium,
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ActiveTripLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = HopColors.primaryLime,
            modifier = Modifier.semantics { contentDescription = "Loading trip details" },
        )
    }
}

@Composable
private fun DriverInfoSection(
    driverName: String,
    driverInitials: String,
    driverPhone: String,
    driverRating: Float,
    isVerified: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        HopAvatar(
            initials = driverInitials,
            size = AvatarSize.Lg,
            isVerified = isVerified,
        )
        Text(
            text = driverName,
            style = MaterialTheme.typography.titleMedium,
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        if (driverPhone.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = null,
                    tint = HopColors.primaryLime,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = driverPhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HopColors.primaryLime,
                )
            }
        }
        StarRating(rating = driverRating)
    }
}

@Composable
private fun ActiveRouteSection(
    originName: String,
    destName: String,
    departsAt: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "Route",
            style = MaterialTheme.typography.labelMedium,
            color = HopColors.authTextSecondary,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Text(
                text = originName,
                style = MaterialTheme.typography.bodyLarge,
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "→",
                style = MaterialTheme.typography.bodyLarge,
                color = HopColors.authTextSecondary,
            )
            Text(
                text = destName,
                style = MaterialTheme.typography.bodyLarge,
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = departsAt,
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
        )
    }
}

@Composable
private fun BookingStatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (status) {
        BookingStatus.CONFIRMED -> "Confirmed" to HopColors.success
        BookingStatus.PENDING -> "Pending" to HopColors.warning
        BookingStatus.AWAITING_PAYMENT -> "Awaiting payment" to HopColors.warning
        BookingStatus.COMPLETED -> "Completed" to HopColors.success
        BookingStatus.DISPUTED -> "Disputed" to HopColors.error
        else -> return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            ),
            color = color,
        )
    }
}

@Composable
private fun ThresholdSection(
    seatsBooked: Int,
    minThreshold: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "$seatsBooked of $minThreshold seats confirmed",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.authTextSecondary,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .semantics { contentDescription = "Seat threshold progress: $seatsBooked of $minThreshold" },
            color = HopColors.warning,
            trackColor = HopColors.surfaceElevated,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ActionButtonsSection(
    bookingStatus: BookingStatus,
    isCancelling: Boolean,
    onMessageDriver: () -> Unit,
    onPayDriver: () -> Unit,
    onCancelBooking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        HopButton(
            text = "Message Driver",
            onClick = onMessageDriver,
            variant = HopButtonVariant.Ghost,
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = HopColors.primaryLime,
                )
            },
        )
        if (bookingStatus == BookingStatus.AWAITING_PAYMENT) {
            HopButton(
                text = "Pay Driver",
                onClick = onPayDriver,
            )
        } else {
            HopButton(
                text = "Cancel Booking",
                onClick = onCancelBooking,
                variant = HopButtonVariant.Destructive,
                isLoading = isCancelling,
            )
        }
    }
}

@Composable
private fun CancelBookingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HopColors.background,
        title = {
            Text(
                text = "Cancel Booking?",
                style = MaterialTheme.typography.titleMedium,
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to cancel this booking? Your refund will be processed automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Yes, Cancel",
                    color = HopColors.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Keep Booking",
                    color = HopColors.authTextSecondary,
                )
            }
        },
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TripDetailActiveScreenConfirmedPreview() {
    HopTheme {
        TripDetailActiveScreen(
            state = TripDetailActiveUiState(
                driverName = "Anders Nielsen",
                driverInitials = "AN",
                driverPhone = "+45 20 12 34 56",
                driverRating = 4.8f,
                isDriverVerified = true,
                originName = "Aarhus",
                destName = "Copenhagen",
                departsAt = "2026-04-14T07:30:00Z",
                bookingStatus = BookingStatus.CONFIRMED,
                tripModel = TripModel.A,
            ),
            isCancelling = false,
            onBack = {},
            onMessageDriver = {},
            onPayDriver = {},
            onCancelBookingConfirmed = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TripDetailActiveScreenModelBPendingPreview() {
    HopTheme {
        TripDetailActiveScreen(
            state = TripDetailActiveUiState(
                driverName = "Sofie Larsen",
                driverInitials = "SL",
                driverPhone = "+45 31 98 76 54",
                driverRating = 4.5f,
                isDriverVerified = false,
                originName = "Odense",
                destName = "Copenhagen",
                departsAt = "2026-04-16T06:00:00Z",
                bookingStatus = BookingStatus.PENDING,
                tripModel = TripModel.B,
                seatsBooked = 2,
                minThreshold = 3,
            ),
            isCancelling = false,
            onBack = {},
            onMessageDriver = {},
            onPayDriver = {},
            onCancelBookingConfirmed = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TripDetailActiveScreenLoadingPreview() {
    HopTheme {
        TripDetailActiveScreen(
            state = TripDetailActiveUiState(isLoading = true),
            isCancelling = false,
            onBack = {},
            onMessageDriver = {},
            onPayDriver = {},
            onCancelBookingConfirmed = {},
        )
    }
}

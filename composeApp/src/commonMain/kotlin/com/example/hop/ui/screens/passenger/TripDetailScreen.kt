package com.example.hop.ui.screens.passenger

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.hop.ui.theme.HopMonoFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
import com.example.hop.presentation.tripdetail.TripDetailEffect
import com.example.hop.presentation.tripdetail.TripDetailEvent
import com.example.hop.presentation.tripdetail.TripDetailUiState
import com.example.hop.presentation.tripdetail.TripDetailViewModel
import com.example.hop.ui.components.AvatarSize
import com.example.hop.ui.components.HopAvatar
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.StarRating
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-03 — Trip Detail Route.
 *
 * Wires [TripDetailViewModel] from Koin, collects [TripDetailEffect] for
 * one-shot navigation and snackbar events, and delegates all rendering to
 * the stateless [TripDetailScreen].
 */
@Composable
fun TripDetailRoute(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToOtherProfile: (driverId: String) -> Unit,
    onNavigateToBookingConfirmation: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId) {
        viewModel.onEvent(TripDetailEvent.LoadTrip(tripId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripDetailEffect.NavigateToBookingConfirmation ->
                    onNavigateToBookingConfirmation(effect.tripId)
                is TripDetailEffect.NavigateToOtherProfile ->
                    onNavigateToOtherProfile(effect.driverId)
                is TripDetailEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        TripDetailScreen(
            state = state,
            onBack = onNavigateBack,
            onAvatarClick = { viewModel.onEvent(TripDetailEvent.ViewDriverProfile) },
            onBookSeat = { viewModel.onEvent(TripDetailEvent.BookSeat) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-03 — Trip Detail Screen.
 *
 * Stateless renderer. Renders:
 *  - Top bar with back arrow
 *  - Driver header: 80dp avatar (tappable → OtherProfile) + name + star rating +
 *    verified badge + model badge
 *  - Route: origin → destination with departure time
 *  - Meta row: distance · duration · seats-available chip
 *  - Price breakdown card (Surface Elevated bg):
 *      cost per seat in DKK (monospace), platform-fee note,
 *      Model B threshold progress bar
 *  - "Book Seat" primary button pinned to the bottom
 */
@Composable
fun TripDetailScreen(
    state: TripDetailUiState,
    onBack: () -> Unit,
    onAvatarClick: () -> Unit,
    onBookSeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding(),
    ) {
        // Top bar
        TripDetailTopBar(onBack = onBack)

        when {
            state.isLoading -> TripDetailLoading(modifier = Modifier.weight(1f))
            else -> {
                // Scrollable content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    item {
                        DriverHeader(
                            driverName = state.driverName,
                            driverInitials = state.driverInitials,
                            driverRating = state.driverRating,
                            isVerified = state.isDriverVerified,
                            model = state.model,
                            onAvatarClick = onAvatarClick,
                        )
                    }
                    item { SectionDivider() }
                    item {
                        RouteSection(
                            originName = state.originName,
                            destName = state.destName,
                            departsAt = state.departsAt,
                        )
                    }
                    item { SectionDivider() }
                    item {
                        MetaRow(
                            distanceMetres = state.distanceMetres,
                            estimatedDurationMinutes = state.estimatedDurationMinutes,
                            seatsAvailable = state.seatsAvailable,
                        )
                    }
                    item { SectionDivider() }
                    item {
                        PriceBreakdownCard(
                            priceOerePerSeat = state.priceOerePerSeat,
                            platformFeeOere = state.platformFeeOere,
                            model = state.model,
                            seatsBooked = state.seatsBooked,
                            minThreshold = state.minThreshold,
                            thresholdProgress = state.thresholdProgress,
                            modifier = Modifier.padding(HopSpacing.md),
                        )
                    }
                    item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
                }

                // Pinned Book Seat button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HopColors.surface)
                        .navigationBarsPadding()
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
                ) {
                    HopButton(
                        text = "Book Seat",
                        onClick = onBookSeat,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TripDetailTopBar(onBack: () -> Unit) {
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
                tint = HopColors.textPrimary,
            )
        }
        Text(
            text = "Trip Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HopColors.textPrimary,
        )
    }
}

// ── Driver header ─────────────────────────────────────────────────────────────

@Composable
private fun DriverHeader(
    driverName: String,
    driverInitials: String,
    driverRating: Float,
    isVerified: Boolean,
    model: TripModel,
    onAvatarClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tappable avatar → OtherProfile
        HopAvatar(
            initials = driverInitials,
            size = AvatarSize.Lg,
            isVerified = isVerified,
            modifier = Modifier
                .clickable(onClick = onAvatarClick)
                .semantics { contentDescription = "View driver profile" },
        )

        Spacer(modifier = Modifier.width(HopSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = driverName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = HopColors.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRating(
                    rating = driverRating,
                    starSize = 14.dp,
                    gap = 2.dp,
                )
                Spacer(modifier = Modifier.width(HopSpacing.xs))
                Text(
                    text = "%.1f".format(driverRating),
                    style = MaterialTheme.typography.labelMedium,
                    color = HopColors.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.width(HopSpacing.sm))

        // Model badge
        TripModelBadge(model = model)
    }
}

// ── Model badge ───────────────────────────────────────────────────────────────

/** Model A = Commute blue, Model B = Long Distance Lime. */
@Composable
private fun TripModelBadge(model: TripModel) {
    val (background, contentColor, label) = when (model) {
        TripModel.A -> Triple(Color(0xFF2563EB), Color.White, "Commute")
        TripModel.B -> Triple(HopColors.primaryLime, Color(0xFF1A1A1A), "Long Distance")
        TripModel.UNKNOWN -> Triple(HopColors.surfaceElevated, HopColors.textSecondary, "Unknown")
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp,
            ),
            color = contentColor,
        )
    }
}

// ── Route section ─────────────────────────────────────────────────────────────

@Composable
private fun RouteSection(
    originName: String,
    destName: String,
    departsAt: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        // Origin
        Row(verticalAlignment = Alignment.CenterVertically) {
            RouteDot(color = HopColors.primaryLime)
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Column {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelSmall,
                    color = HopColors.textSecondary,
                )
                Text(
                    text = originName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = HopColors.textPrimary,
                )
            }
        }

        // Connector line (centred on the 8dp dot)
        Box(
            modifier = Modifier
                .padding(start = 3.dp)
                .size(width = 2.dp, height = 16.dp)
                .background(HopColors.textSecondary.copy(alpha = 0.4f)),
        )

        // Destination
        Row(verticalAlignment = Alignment.CenterVertically) {
            RouteDot(color = HopColors.textSecondary)
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Column {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelSmall,
                    color = HopColors.textSecondary,
                )
                Text(
                    text = destName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = HopColors.textPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.xs))

        Text(
            text = "Departs at $departsAt",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textSecondary,
        )
    }
}

@Composable
private fun RouteDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
    )
}

// ── Meta row (distance · duration · seats) ────────────────────────────────────

@Composable
private fun MetaRow(
    distanceMetres: Int,
    estimatedDurationMinutes: Int,
    seatsAvailable: Int,
) {
    val distanceText = "${distanceMetres / 1000} km"
    val durationHours = estimatedDurationMinutes / 60
    val durationMins = estimatedDurationMinutes % 60
    val durationText = when {
        durationHours > 0 -> "~${durationHours}h ${durationMins}min"
        else -> "~${durationMins}min"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        MetaChip(label = distanceText)
        MetaSeparator()
        MetaChip(label = durationText)
        MetaSeparator()
        SeatsAvailableChip(seatsAvailable = seatsAvailable)
    }
}

@Composable
private fun MetaChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = HopColors.textSecondary,
    )
}

@Composable
private fun MetaSeparator() {
    Text(
        text = "·",
        style = MaterialTheme.typography.bodySmall,
        color = HopColors.textSecondary,
    )
}

@Composable
private fun SeatsAvailableChip(seatsAvailable: Int) {
    val label = if (seatsAvailable == 1) "1 seat left" else "$seatsAvailable seats"
    val bg = if (seatsAvailable <= 1) HopColors.warning.copy(alpha = 0.15f)
             else HopColors.primaryLime.copy(alpha = 0.15f)
    val textColor = if (seatsAvailable <= 1) HopColors.warning else HopColors.primaryLime

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

// ── Price breakdown card ──────────────────────────────────────────────────────

@Composable
private fun PriceBreakdownCard(
    priceOerePerSeat: Int,
    platformFeeOere: Int,
    model: TripModel,
    seatsBooked: Int,
    minThreshold: Int?,
    thresholdProgress: Float,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(HopColors.surfaceElevated)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "Price per seat",
            style = MaterialTheme.typography.labelMedium,
            color = HopColors.textSecondary,
        )

        Text(
            text = "DKK ${priceOerePerSeat / 100}",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = HopMonoFontFamily,
                fontWeight = FontWeight.Bold,
            ),
            color = HopColors.textPrimary,
        )

        Text(
            text = "Includes DKK ${platformFeeOere / 100} platform fee",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textSecondary,
        )

        // Model B — threshold progress bar.
        // This entire block (including the animation state) is fully absent for Model A
        // or any trip with a null/zero minThreshold — no invisible bar, no dormant animator.
        if (model == TripModel.B && minThreshold != null && minThreshold > 0) {
            val animatedProgress by animateFloatAsState(
                targetValue = thresholdProgress,
                animationSpec = tween(durationMillis = 600),
                label = "thresholdProgress",
            )
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            HorizontalDivider(
                thickness = 1.dp,
                color = HopColors.textSecondary.copy(alpha = 0.15f),
            )
            Spacer(modifier = Modifier.height(HopSpacing.xs))

            val remaining = (minThreshold - seatsBooked).coerceAtLeast(0)
            val thresholdLabel = if (remaining == 0) "Threshold reached — confirming soon"
                                 else "$seatsBooked of $minThreshold seats needed to confirm"
            Text(
                text = thresholdLabel,
                style = MaterialTheme.typography.labelSmall,
                color = HopColors.textSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = HopColors.primaryLime,
                trackColor = HopColors.surface,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

// ── Shared helpers ─────────────────────────────────────────────────────────────

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = HopSpacing.md),
        thickness = 1.dp,
        color = HopColors.textSecondary.copy(alpha = 0.15f),
    )
}

@Composable
private fun TripDetailLoading(modifier: Modifier = Modifier) {
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

private fun previewState(
    model: TripModel = TripModel.A,
    seatsBooked: Int = 1,
    minThreshold: Int? = null,
) = TripDetailUiState(
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
    priceOerePerSeat = 20_386,
    platformFeeOere = 3_058,
)

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TripDetailModelAPreview() {
    HopTheme {
        TripDetailScreen(
            state = previewState(model = TripModel.A),
            onBack = {},
            onAvatarClick = {},
            onBookSeat = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TripDetailModelBPreview() {
    HopTheme {
        TripDetailScreen(
            state = previewState(
                model = TripModel.B,
                seatsBooked = 2,
                minThreshold = 4,
            ),
            onBack = {},
            onAvatarClick = {},
            onBookSeat = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TripDetailLoadingPreview() {
    HopTheme {
        TripDetailScreen(
            state = TripDetailUiState(isLoading = true),
            onBack = {},
            onAvatarClick = {},
            onBookSeat = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TripDetailOneSeatLeftPreview() {
    HopTheme {
        TripDetailScreen(
            state = previewState(model = TripModel.A, seatsBooked = 3),
            onBack = {},
            onAvatarClick = {},
            onBookSeat = {},
        )
    }
}

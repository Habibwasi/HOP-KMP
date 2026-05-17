package com.example.hop.ui.screens.driver

import kotlin.math.roundToInt
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.home.DriverAggregatesEvent
import com.example.hop.presentation.home.DriverAggregatesUiState
import com.example.hop.presentation.home.DriverAggregatesViewModel
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.SkeletonBox
import com.example.hop.ui.components.home.DefaultDemandHotspots
import com.example.hop.ui.components.home.DefaultHomeTips
import com.example.hop.ui.components.home.DemandTeaserRow
import com.example.hop.ui.components.home.EarningsHeroCard
import com.example.hop.ui.components.home.GoalsRingCard
import com.example.hop.ui.components.home.RepostTripTemplate
import com.example.hop.ui.components.home.RepostTripsRow
import com.example.hop.ui.components.home.TipsPager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.derivedStateOf
import com.example.hop.ui.components.StatusBadge
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-01 — Driver Home Content.
 *
 * Content-only composable designed to be hosted inside [HomeScreen]'s
 * [AnimatedContent] area. Manages its own [DriverViewModel] and effects but
 * contains no Scaffold, top bar, or bottom nav — those are owned by [HomeRoute].
 *
 * If [hasDriverRole] is false the user is browsing the Driver tab but hasn't
 * completed onboarding yet. Tapping "Post a Trip" redirects them to the
 * car details registration flow.
 */
@Composable
fun DriverHomeContent(
    hasDriverRole: Boolean,
    onNavigateToPostTripModelSelect: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    onNavigateToTaxDashboard: () -> Unit,
    onNavigateToDriverRegistration: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
    aggregatesViewModel: DriverAggregatesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val aggregatesState by aggregatesViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Load driver data on enter.
    LaunchedEffect(Unit) {
        if (hasDriverRole) {
            viewModel.onEvent(DriverEvent.LoadDriverHome)
            aggregatesViewModel.onEvent(DriverAggregatesEvent.Load)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToPostTrip -> onNavigateToPostTripModelSelect()
                is DriverEffect.NavigateToTripDetail -> onNavigateToTripDetail(effect.tripId)
                is DriverEffect.NavigateToTaxDashboard -> onNavigateToTaxDashboard()
                is DriverEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                // Post-trip flow effects owned by their own route VMs.
                is DriverEffect.NavigateToMyTrips -> Unit
                is DriverEffect.NavigateToRatePassenger -> Unit
                is DriverEffect.NavigateToDriverSettlement -> Unit
                is DriverEffect.NavigateToMarkTripComplete -> Unit
                // Onboarding effects handled by EnableDriverStep1 route.
                is DriverEffect.NavigateToModelAForm -> Unit
                is DriverEffect.NavigateToModelBForm -> Unit
                is DriverEffect.NavigateToPriceReview -> Unit
                is DriverEffect.NavigateToLicenceUpload -> Unit
                is DriverEffect.NavigateToReviewPending -> Unit
                is DriverEffect.NavigateToHome -> Unit
                is DriverEffect.NavigateToPastTripDetail -> Unit
            }
        }
    }

    DriverHomeScreen(
        state = state,
        aggregatesState = aggregatesState,
        hasDriverRole = hasDriverRole,
        onPostTrip = {
            if (hasDriverRole) {
                // Approved driver — proceed to the post-trip form.
                viewModel.onEvent(DriverEvent.RequestPostTrip)
            } else {
                // Not yet a driver — send to car details registration.
                onNavigateToDriverRegistration()
            }
        },
        onTripClick = { tripId -> viewModel.onEvent(DriverEvent.SelectTrip(tripId)) },
        onEarningsBannerClick = { viewModel.onEvent(DriverEvent.TapEarningsBanner) },
        onRefresh = { viewModel.onEvent(DriverEvent.RefreshDriverHome) },
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-01 — Driver Home Screen.
 *
 * Stateless content renderer. The top bar and bottom nav are owned by
 * [HomeScreen] — this composable renders only the scrollable body.
 *
 * When [hasDriverRole] is false the earnings banner is replaced with a
 * "Become a Driver" prompt so unregistered users understand what the tab
 * is for before they tap "Post a Trip".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    state: DriverUiState,
    hasDriverRole: Boolean,
    onPostTrip: () -> Unit,
    onTripClick: (tripId: String) -> Unit,
    onEarningsBannerClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
    aggregatesState: DriverAggregatesUiState = DriverAggregatesUiState(),
) {
    val listState = rememberLazyListState()
    // Hide the FAB while the user is actively scrolling down so it doesn't
    // obscure cards; show it again at rest or when scrolling up.
    val fabVisible by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val index = listState.firstVisibleItemIndex
            // Always visible near the top; otherwise hide while moving forward.
            index < 2 || !listState.isScrollInProgress || offset == 0
        }
    }

    val repostTemplates = remember(state.trips) {
        state.trips
            .filter { it.status == TripStatus.COMPLETED }
            .take(5)
            .map { trip ->
                RepostTripTemplate(
                    tripId = trip.id,
                    origin = trip.originName,
                    destination = trip.destName,
                    priceDkkPerSeat = trip.priceOerePerSeat / 100,
                )
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = HopSpacing.md,
                    end = HopSpacing.md,
                    top = HopSpacing.md,
                    // Extra bottom padding so the FAB never overlaps the last card.
                    bottom = HopSpacing.xxl + HopSpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
            ) {
                // Earnings hero — only shown to approved drivers.
                if (hasDriverRole) {
                    item {
                        EarningsHeroCard(
                            monthlyEarningsOere = state.monthlyEarningsOere,
                            estimatedTaxOere = state.estimatedTaxOere,
                            // Real 7-day series from the aggregates endpoint;
                            // fall back to the synthesised baseline before the
                            // first response so the sparkline never looks empty.
                            // .toFloat() applied at render only — money stays Int upstream.
                            sparkSeriesOere = if (aggregatesState.earningsSeries.isNotEmpty()) {
                                aggregatesState.earningsSeries.map { it.earningsOere }
                            } else {
                                synthesiseSparkSeries(state.monthlyEarningsOere)
                            },
                            onClick = onEarningsBannerClick,
                        )
                    }

//                    // Goals & streak — placeholder values until the goals API ships.
//                    item {
//                        GoalsRingCard(
//                            tripsCompleted = state.trips.count { it.status == TripStatus.COMPLETED }.coerceAtMost(5),
//                            tripsGoal = 5,
//                            streakDays = 0,
//                        )
//                    }
//
//                    // Demand near you.
//                    item {
//                        Text(
//                            text = "Demand near you",
//                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                            color = HopColors.authTextPrimary,
//                        )
//                    }
//                    item {
//                        // Real hotspots from the aggregates endpoint, mapped
//                        // into the UI model. Falls back to default fixtures so
//                        // the carousel never empties on first load.
//                        val uiHotspots = if (aggregatesState.demandHotspots.isNotEmpty()) {
//                            aggregatesState.demandHotspots.map { hot ->
//                                val level = when {
//                                    hot.demandCount >= 10 -> com.example.hop.ui.components.home.DemandLevel.HIGH
//                                    hot.demandCount >= 5 -> com.example.hop.ui.components.home.DemandLevel.MEDIUM
//                                    else -> com.example.hop.ui.components.home.DemandLevel.LOW
//                                }
//                                com.example.hop.ui.components.home.DemandHotspot(
//                                    areaName = hot.areaName,
//                                    tagline = "${hot.demandCount} bookings in last 7 days",
//                                    level = level,
//                                )
//                            }
//                        } else {
//                            DefaultDemandHotspots
//                        }
//                        DemandTeaserRow(
//                            hotspots = uiHotspots,
//                            onHotspotClick = { onPostTrip() },
//                        )
//                    }
//
                    // Repost templates — only when there's something to repost.
                    if (repostTemplates.isNotEmpty()) {
                        item {
                            Text(
                                text = "Repost a recent trip",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = HopColors.authTextPrimary,
                            )
                        }
                        item {
                            RepostTripsRow(
                                templates = repostTemplates,
                                onRepost = { onPostTrip() },
                            )
                        }
                    }
                } else {
                    item { BecomeDriverPrompt() }
                }

                // Section heading
                item {
                    Text(
                        text = "My Trips",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = HopColors.authTextPrimary,
                        modifier = Modifier.padding(top = HopSpacing.xs),
                    )
                }

                // Loading / empty / list
                if (state.isLoading) {
                    items(3) {
                        SkeletonBox(height = 120.dp, cornerRadius = 12.dp)
                    }
                } else if (state.trips.none { it.status == TripStatus.ACTIVE || it.status == TripStatus.CONFIRMED }) {
                    item {
                        EmptyState(
                            headline = "Post your first trip to start earning",
                            subtext = "Set a route, pick a time, and let passengers book seats.",
                            ctaLabel = "Post a trip",
                            onCtaClick = onPostTrip,
                        )
                    }
                } else {
                    items(
                        state.trips.filter { it.status == TripStatus.ACTIVE || it.status == TripStatus.CONFIRMED },
                        key = { it.id },
                    ) { tripUiModel ->
                        DriverTripCard(
                            tripUiModel = tripUiModel,
                            onClick = { onTripClick(tripUiModel.id) },
                            modifier = if (tripUiModel.isBroken) Modifier.alpha(0.5f) else Modifier,
                        )
                    }
                }
            }
        }

        // ── Floating action button — Post a Trip ──────────────────────────────
        // Hides when the user is actively scrolling further down the list so
        // it never obscures content; reappears at rest.
        AnimatedVisibility(
            visible = fabVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = HopSpacing.md, bottom = HopSpacing.md),
        ) {
            ExtendedFloatingActionButton(
                onClick = onPostTrip,
                containerColor = HopColors.primaryLime,
                contentColor = HopColors.authTextPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                text = {
                    Text(
                        text = "Post a Trip",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
            )
        }
    }
}

/**
 * Builds a deterministic, gently-rising 7-point series in øre derived from the
 * running monthly total. Used as a placeholder until the real
 * `/drivers/me/earnings/series` endpoint lands. Renders as a flat baseline at
 * zero when no earnings exist yet.
 */
private fun synthesiseSparkSeries(monthlyTotalOere: Int): List<Int> {
    if (monthlyTotalOere <= 0) return List(7) { 0 }
    val avg = monthlyTotalOere / 30
    // Mild ascending pattern: 0.6×, 0.7×, 1.1×, 0.9×, 1.3×, 1.5×, 1.4× of daily avg.
    val factors = floatArrayOf(0.6f, 0.7f, 1.1f, 0.9f, 1.3f, 1.5f, 1.4f)
    return factors.map { (avg * it).toInt() }
}

// ── Become-driver prompt (shown when hasDriverRole = false) ──────────────────

@Composable
private fun BecomeDriverPrompt(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HopColors.primaryLime)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.lg),
    ) {
        Column {
            Text(
                text = "Save money driving with Hop",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1A1A),
            )
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            Text(
                text = "Set your own route, time, and price. Tap \"Post a Trip\" below, we'll walk you through the setup.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A1A1A).copy(alpha = 0.7f),
            )
        }

    }
}

// ── Earnings banner ───────────────────────────────────────────────────────────

@Composable
private fun EarningsBanner(
    monthlyEarningsOere: Int,
    estimatedTaxOere: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate from 0 → actual amount on first composition
    var targetAmount by remember { mutableIntStateOf(0) }
    val animatedAmountDkk by animateIntAsState(
        targetValue = targetAmount,
        animationSpec = tween(durationMillis = 900, easing = EaseOut),
        label = "earningsTicker",
    )

    LaunchedEffect(monthlyEarningsOere) {
        targetAmount = monthlyEarningsOere / 100
    }

    val bannerShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(HopColors.primaryLime)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Monthly earnings. Tap to open tax dashboard." }
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.lg),
    ) {
        Column {
            // Animated DKK amount
            Text(
                text = "DKK $animatedAmountDkk",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = HopMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = HopColors.authTextPrimary,
                    letterSpacing = 0.sp,
                ),
            )

            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // Estimated tax subtext
            Text(
                text = "Est. tax this month: DKK ${estimatedTaxOere / 100}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = HopColors.authTextPrimary.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        // Tap hint chevron (top-right)
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = HopColors.authTextPrimary.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(20.dp),
        )
    }
}

// ── Driver trip card ──────────────────────────────────────────────────────────

@Composable
private fun DriverTripCard(
    tripUiModel: TripUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = cardShape, ambientColor = Color(0x1A000000))
            .clip(cardShape)
            .background(Color.White)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            // ── Row 1: Model badge + Status badge ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatusBadge(
                    type = when (tripUiModel.model) {
                        TripModel.A -> BadgeType.ModelA
                        TripModel.B -> BadgeType.ModelB
                        TripModel.UNKNOWN -> BadgeType.Custom(
                            label = "UNKNOWN",
                            background = HopColors.authInputSurface,
                            contentColor = HopColors.authTextSecondary,
                        )
                    },
                )
                Spacer(modifier = Modifier.width(HopSpacing.sm))
                StatusBadge(
                    type = when (tripUiModel.status) {
                        TripStatus.ACTIVE -> BadgeType.Confirmed
                        TripStatus.CONFIRMED -> BadgeType.Confirmed
                        TripStatus.CANCELLED -> BadgeType.Cancelled
                        TripStatus.COMPLETED -> BadgeType.Completed
                        TripStatus.THRESHOLD_NOT_MET -> BadgeType.Custom(
                            label = "Threshold not met",
                            background = HopColors.warning.copy(alpha = 0.15f),
                            contentColor = HopColors.warning,
                        )
                        TripStatus.UNKNOWN -> BadgeType.Custom(
                            label = "UNKNOWN",
                            background = HopColors.authInputSurface,
                            contentColor = HopColors.authTextSecondary,
                        )
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
                // "New booking" dot — shown when any booking was created in last 24 h
                if (tripUiModel.hasRecentBooking) {
                    Text(
                        text = "New booking",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HopColors.primaryGreen,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HopColors.primaryGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(HopSpacing.xs))
                }
                // Seats booked / total
                Text(
                    text = "${tripUiModel.seatsBooked}/${tripUiModel.seatsTotal} seats",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(HopSpacing.sm))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Row 2: Route ──────────────────────────────────────────────────
            DriverRouteColumn(
                origin = tripUiModel.originName,
                destination = tripUiModel.destName,
            )

            Spacer(modifier = Modifier.height(HopSpacing.sm))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Row 3: Departure time + Driver net per seat ───────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Departs ${tripUiModel.departsAt}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF666666),
                    ),
                )
                Text(
                    text = "DKK ${(tripUiModel.trip.driverNetOere / 100.0).roundToInt()}/seat",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = HopColors.authTextPrimary,
                        fontSize = 16.sp,
                    ),
                )
            }

            // ── Row 4: Recurring days (Model A only) ──────────────────────────
            val days = tripUiModel.recurrenceDays
            if (tripUiModel.model == TripModel.A && !days.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(HopSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = HopColors.primaryLime,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = days.joinToString(" · ") { it.take(2) },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HopColors.primaryLime,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
    }
}

// ── Route column (origin → destination) ──────────────────────────────────────

@Composable
private fun DriverRouteColumn(
    origin: String,
    destination: String,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(HopColors.primaryLime),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = origin,
                style = MaterialTheme.typography.bodyMedium.copy(color = HopColors.authTextPrimary),
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(width = 2.dp, height = 12.dp)
                .background(Color(0xFFD0D0D0)),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(HopColors.authTextPrimary),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = destination,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun DriverBottomNavBar(
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = HopColors.background,
        tonalElevation = 0.dp,
    ) {
        DriverNavItem(
            icon = Icons.Outlined.Home,
            label = "Home",
            selected = true,
            onClick = { /* already on home */ },
            contentDescription = "Home",
        )
        DriverNavItem(
            icon = Icons.Outlined.DirectionsCar,
            label = "My Trips",
            selected = false,
            onClick = onMyTrips,
            contentDescription = "My Trips",
        )
        DriverNavItem(
            icon = Icons.AutoMirrored.Outlined.Chat,
            label = "Chat",
            selected = false,
            onClick = onChat,
            contentDescription = "Chat",
        )
        DriverNavItem(
            icon = Icons.Outlined.Person,
            label = "Profile",
            selected = false,
            onClick = onProfile,
            contentDescription = "Profile",
        )
    }
}

@Composable
private fun RowScope.DriverNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = HopColors.primaryLime,
            selectedTextColor = HopColors.primaryLime,
            indicatorColor = Color.Transparent,
            unselectedIconColor = HopColors.authTextSecondary,
            unselectedTextColor = HopColors.authTextSecondary,
        ),
        modifier = modifier,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun DriverHomeScreenEmptyPreview() {
    HopTheme {
        DriverHomeScreen(
            state = DriverUiState(isLoading = false, trips = emptyList()),
            hasDriverRole = true,
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun DriverHomeScreenNonDriverPreview() {
    HopTheme {
        DriverHomeScreen(
            state = DriverUiState(isLoading = false, trips = emptyList()),
            hasDriverRole = false,
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun DriverHomeScreenLoadingPreview() {
    HopTheme {
        DriverHomeScreen(
            state = DriverUiState(
                isLoading = true,
                trips = emptyList(),
                monthlyEarningsOere = 69_31200,
                estimatedTaxOere = 12_50000,
            ),
            hasDriverRole = true,
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun DriverHomeScreenWithTripsPreview() {
    HopTheme {
        val sampleTrips = previewDriverTrips()
        DriverHomeScreen(
            state = DriverUiState(
                isLoading = false,
                trips = sampleTrips,
                monthlyEarningsOere = 69_31200,
                estimatedTaxOere = 12_50000,
            ),
            hasDriverRole = true,
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC8F135)
@Composable
private fun EarningsBannerPreview() {
    HopTheme {
        EarningsBanner(
            monthlyEarningsOere = 69_31200,
            estimatedTaxOere = 12_50000,
            onClick = {},
            modifier = Modifier.padding(HopSpacing.md),
        )
    }
}

// ── Preview helpers ───────────────────────────────────────────────────────────

private fun previewDriverTrips(): List<TripUiModel> {
    val trip1 = com.example.hop.domain.model.Trip(
        id = "trip-1",
        driverId = "driver-1",
        model = TripModel.A,
        originName = "Aarhus C",
        originLat = 56.1629,
        originLng = 10.2039,
        destName = "Copenhagen H",
        destLat = 55.6761,
        destLng = 12.5683,
        distanceMetres = 304_000,
        departsAt = "08:00",
        seatsTotal = 4,
        seatsBooked = 2,
        minThreshold = null,
        priceOerePerSeat = 20_386,
        driverNetOere = 17_328,
        status = TripStatus.CONFIRMED,
        recurrenceDays = listOf("MON", "WED", "FRI"),
    )
    val trip2 = com.example.hop.domain.model.Trip(
        id = "trip-2",
        driverId = "driver-1",
        model = TripModel.B,
        originName = "Odense",
        originLat = 55.4038,
        originLng = 10.4024,
        destName = "Copenhagen H",
        destLat = 55.6761,
        destLng = 12.5683,
        distanceMetres = 168_000,
        departsAt = "14:30",
        seatsTotal = 3,
        seatsBooked = 1,
        minThreshold = 2,
        priceOerePerSeat = 11_250,
        driverNetOere = 9_562,
        status = TripStatus.ACTIVE,
        recurrenceDays = null,
    )
    return listOf(trip1, trip2).map { com.example.hop.presentation.model.TripUiModel(it) }
}

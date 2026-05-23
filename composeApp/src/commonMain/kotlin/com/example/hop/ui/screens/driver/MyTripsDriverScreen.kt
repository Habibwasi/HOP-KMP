package com.example.hop.ui.screens.driver

import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.StatusBadge
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-09 — My Trips (Driver) Route.
 *
 * Wires [DriverViewModel] from Koin, loads driver trips on entry, handles
 * one-shot navigation and snackbar effects, then delegates all rendering to
 * the stateless [MyTripsDriverScreen].
 *
 * Effect mapping:
 *  • [DriverEffect.NavigateToTripDetail]  → onNavigateToTripDetailActiveDriver
 *  • [DriverEffect.NavigateToPostTrip]    → onNavigateToPostTrip
 */
@Composable
fun MyTripsDriverRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTripDetailActiveDriver: (tripId: String) -> Unit,
    onNavigateToDriverSettlement: (tripId: String) -> Unit,
    onNavigateToPastTripDetail: (tripId: String) -> Unit,
    onNavigateToPostTrip: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onEvent(DriverEvent.LoadDriverHome)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToTripDetail -> onNavigateToTripDetailActiveDriver(effect.tripId)
                is DriverEffect.NavigateToDriverSettlement -> onNavigateToDriverSettlement(effect.tripId)
                is DriverEffect.NavigateToPastTripDetail -> onNavigateToPastTripDetail(effect.tripId)
                is DriverEffect.NavigateToPostTrip -> onNavigateToPostTrip()
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        MyTripsDriverScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            onNavigateToHome = onNavigateToHome,
            onNavigateToChat = onNavigateToChat,
            onNavigateToProfile = onNavigateToProfile,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * DR-09 — My Trips (Driver) Screen.
 *
 * Stateless renderer. Tab selection (Upcoming / Past) is ephemeral visual state
 * held locally. Lists are derived from [DriverUiState.trips] by status filter.
 *
 * Driver-specific card layout:
 *  • Model badge (A / B) + Status badge + "X/Y seats" chip on the right
 *  • Origin → destination route column
 *  • Departure time footer with driver net per seat
 *  • Model B upcoming: threshold progress bar (Lime fill, dark tick at min threshold)
 */
@Composable
fun MyTripsDriverScreen(
    state: DriverUiState,
    onEvent: (DriverEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedTab: Int = 0,
) {
    var selectedTab by remember { mutableIntStateOf(initialSelectedTab) }
    val tabs = listOf("Upcoming", "Past")
    var filterDate by remember { mutableStateOf<Long?>(null) }
    var filterModel by remember { mutableStateOf<TripModel?>(null) }

    val filterDateStr = remember(filterDate) {
        filterDate?.let {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.format(java.util.Date(it))
        }
    }
    val upcomingTrips = remember(state.trips, filterDate, filterModel) {
        state.trips
            .filter { it.isUpcomingDriver() }
            .let { list -> if (filterModel != null) list.filter { it.model == filterModel } else list }
            .let { list -> if (filterDateStr != null) list.filter { it.departsAt.take(10) == filterDateStr } else list }
    }
    val pastTrips = remember(state.trips, filterDate, filterModel) {
        state.trips
            .filter { it.isPastDriver() }
            .let { list -> if (filterModel != null) list.filter { it.model == filterModel } else list }
            .let { list -> if (filterDateStr != null) list.filter { it.departsAt.take(10) == filterDateStr } else list }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        MyTripsDriverTopBar(onNavigateBack = onNavigateBack)

        // ── Tab row ───────────────────────────────────────────────────────────
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = HopColors.background,
            contentColor = HopColors.authTextPrimary,
            indicator = {
                androidx.compose.material3.TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = false),
                    height = 2.dp,
                    color = HopColors.primaryLime,
                )
            },
            divider = {},
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.semantics {
                        contentDescription = "$title trips tab"
                    },
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = if (selectedTab == index) HopColors.primaryLime else HopColors.authTextSecondary,
                        modifier = Modifier.padding(vertical = HopSpacing.sm),
                    )
                }
            }
        }
        // ── Filter bar ──────────────────────────────────────────────────────────
        TripsFilterBar(
            filterDate = filterDate,
            filterModel = filterModel,
            onFilterDateChange = { filterDate = it },
            onFilterModelChange = { filterModel = it },
        )
        // ── Content ───────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> DriverTripsLoadingIndicator()
                selectedTab == 0 -> UpcomingDriverTripsContent(
                    trips = upcomingTrips,
                    onTripClick = { tripId -> onEvent(DriverEvent.SelectTrip(tripId)) },
                    onPostTrip = { onEvent(DriverEvent.RequestPostTrip) },
                )
                else -> PastDriverTripsContent(
                    trips = pastTrips,
                    onTripClick = { tripId -> onEvent(DriverEvent.SelectTrip(tripId)) },
                    onPostTrip = { onEvent(DriverEvent.RequestPostTrip) },
                )
            }
        }

        // ── Bottom nav ────────────────────────────────────────────────────────
        MyTripsDriverBottomNavBar(
            onHome = onNavigateToHome,
            onChat = onNavigateToChat,
            onProfile = onNavigateToProfile,
        )
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun MyTripsDriverTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(HopColors.background)
            .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.xs),
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = HopColors.authTextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = "My Trips",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = HopColors.authTextPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun DriverTripsLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = HopColors.primaryLime,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun UpcomingDriverTripsContent(
    trips: List<TripUiModel>,
    onTripClick: (String) -> Unit,
    onPostTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (trips.isEmpty()) {
        EmptyState(
            headline = "No trips posted yet",
            subtext = "Trips you post will appear here once they're live.",
            ctaLabel = "Post a trip",
            onCtaClick = onPostTrip,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        DriverTripList(
            trips = trips,
            showThresholdBar = true,
            onTripClick = onTripClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PastDriverTripsContent(
    trips: List<TripUiModel>,
    onTripClick: (String) -> Unit,
    onPostTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (trips.isEmpty()) {
        EmptyState(
            headline = "No trips posted yet",
            subtext = "Your completed and cancelled trips will appear here.",
            ctaLabel = "Post a trip",
            onCtaClick = onPostTrip,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        DriverTripList(
            trips = trips,
            showThresholdBar = false,
            onTripClick = onTripClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DriverTripList(
    trips: List<TripUiModel>,
    showThresholdBar: Boolean,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = HopSpacing.md,
            vertical = HopSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
    ) {
        items(trips, key = { it.id }) { tripUiModel ->
            DriverTripCard(
                tripUiModel = tripUiModel,
                showThresholdBar = showThresholdBar && tripUiModel.model == TripModel.B,
                onClick = if (tripUiModel.isBroken) ({}) else ({ onTripClick(tripUiModel.id) }),
                modifier = if (tripUiModel.isBroken) Modifier.alpha(0.6f) else Modifier,
            )
        }

        item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
    }
}

// ── Driver trip card ───────────────────────────────────────────────────────────

@Composable
private fun DriverTripCard(
    tripUiModel: TripUiModel,
    showThresholdBar: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)
    // Suppress the "new booking" glow once the booking has moved to awaiting-payment
    // state — at that point the blue badge already draws attention.
    val isNew = tripUiModel.hasRecentBooking && tripUiModel.awaitingPaymentBookingId == null

    val scale = remember { Animatable(if (isNew) 0.93f else 1f) }
    LaunchedEffect(isNew) {
        if (isNew) scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))
    }

    val elevation = if (isNew) 14.dp else 4.dp
    val shadowColor = if (isNew) HopColors.primaryLime.copy(alpha = 0.55f) else Color(0x1A000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(elevation = elevation, shape = cardShape, ambientColor = shadowColor, spotColor = shadowColor)
            .then(if (isNew) Modifier.border(2.dp, HopColors.primaryLime, cardShape) else Modifier)
            .clip(cardShape)
            .background(if (isNew) HopColors.primaryLime.copy(alpha = 0.04f) else Color.White)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            // ── Row 1: Model badge + Status badge + Seats ─────────────────────
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
                        TripStatus.ACTIVE -> BadgeType.Custom(
                            label = "Active",
                            background = HopColors.primaryLime.copy(alpha = 0.20f),
                            contentColor = Color(0xFF1A1A1A),
                        )
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
                // "Awaiting payment" badge — a passenger has not yet paid
                if (tripUiModel.awaitingPaymentBookingId != null) {
                    Text(
                        text = "Awaiting payment",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(HopSpacing.xs))
                // "New booking" badge — shown when any booking was created in last 24 h
                } else if (tripUiModel.hasRecentBooking) {
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
            CardRouteColumn(
                origin = tripUiModel.originName,
                destination = tripUiModel.destName,
            )

            // ── Model B threshold progress bar ────────────────────────────────
            if (showThresholdBar) {
                val minThreshold = tripUiModel.trip.minThreshold
                if (minThreshold != null && tripUiModel.seatsTotal > 0) {
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    ThresholdProgressBar(
                        seatsBooked = tripUiModel.seatsBooked,
                        seatsTotal = tripUiModel.seatsTotal,
                        minThreshold = minThreshold,
                    )
                    Spacer(modifier = Modifier.height(HopSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "${tripUiModel.seatsBooked} booked",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF666666),
                            ),
                        )
                        Text(
                            text = "Min $minThreshold to confirm",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF666666),
                            ),
                        )
                    }
                }
            }

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
                    text = "Departs ${tripUiModel.formattedDepartsAt}",
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

// ── Threshold progress bar ────────────────────────────────────────────────────

/**
 * Model B threshold progress bar.
 *
 * Layout:
 *  ┌──────────────────────────────────────────────┐
 *  │  ████████████▐░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │
 *  └──────────────────────────────────────────────┘
 *                 ↑ tick mark at minThreshold/seatsTotal
 *
 * Lime fill tracks seats booked. Dark vertical tick marks the minimum threshold.
 */
@Composable
private fun ThresholdProgressBar(
    seatsBooked: Int,
    seatsTotal: Int,
    minThreshold: Int,
    modifier: Modifier = Modifier,
) {
    val progress = (seatsBooked / seatsTotal.toFloat()).coerceIn(0f, 1f)
    val thresholdFraction = (minThreshold / seatsTotal.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8E8E8)),
        )
        // Lime fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .clip(RoundedCornerShape(4.dp))
                .background(HopColors.primaryLime),
        )
        // Threshold tick marker
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tickX = thresholdFraction * size.width
            drawLine(
                color = HopColors.authTextPrimary,
                start = Offset(tickX, 0f),
                end = Offset(tickX, size.height),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Square,
            )
        }
    }
}

// ── Route column ──────────────────────────────────────────────────────────────

@Composable
private fun CardRouteColumn(
    origin: String,
    destination: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
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
                    .clip(CircleShape)
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
private fun MyTripsDriverBottomNavBar(
    onHome: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = HopColors.background,
        tonalElevation = 0.dp,
    ) {
        val chipColors = NavigationBarItemDefaults.colors(
            selectedIconColor = HopColors.primaryLime,
            selectedTextColor = HopColors.primaryLime,
            indicatorColor = HopColors.primaryLime.copy(alpha = 0.12f),
            unselectedIconColor = HopColors.authTextSecondary,
            unselectedTextColor = HopColors.authTextSecondary,
        )

        NavigationBarItem(
            selected = false,
            onClick = onHome,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text(text = "Home", style = MaterialTheme.typography.labelSmall) },
            colors = chipColors,
        )

        // My Trips — active on this screen
        NavigationBarItem(
            selected = true,
            onClick = { /* already here */ },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = "My Trips",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text(text = "My Trips", style = MaterialTheme.typography.labelSmall) },
            colors = chipColors,
        )

        NavigationBarItem(
            selected = false,
            onClick = onChat,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text(text = "Chat", style = MaterialTheme.typography.labelSmall) },
            colors = chipColors,
        )

        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text(text = "Profile", style = MaterialTheme.typography.labelSmall) },
            colors = chipColors,
        )
    }
}

// ── Filter bar composable ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripsFilterBar(
    filterDate: Long?,
    filterModel: TripModel?,
    onFilterDateChange: (Long?) -> Unit,
    onFilterModelChange: (TripModel?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = filterDate)
    val dateLabel = filterDate?.let {
        val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.format(java.util.Date(it))
    } ?: "Date"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        FilterChip(
            selected = filterDate != null,
            onClick = { showDatePicker = true },
            label = { Text(text = dateLabel, style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (filterDate == null) {
                { Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
            trailingIcon = if (filterDate != null) {
                {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Clear date filter",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onFilterDateChange(null) },
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HopColors.primaryLime.copy(alpha = 0.20f),
                selectedLabelColor = HopColors.authTextPrimary,
            ),
        )
        FilterChip(
            selected = filterModel == TripModel.A,
            onClick = { onFilterModelChange(if (filterModel == TripModel.A) null else TripModel.A) },
            label = { Text("Commute", style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HopColors.primaryLime.copy(alpha = 0.20f),
                selectedLabelColor = HopColors.authTextPrimary,
            ),
        )
        FilterChip(
            selected = filterModel == TripModel.B,
            onClick = { onFilterModelChange(if (filterModel == TripModel.B) null else TripModel.B) },
            label = { Text("Long Trip", style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HopColors.primaryLime.copy(alpha = 0.20f),
                selectedLabelColor = HopColors.authTextPrimary,
            ),
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onFilterDateChange(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK", color = HopColors.primaryLime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = HopColors.authTextSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Trips that departed more than 2 hours ago are treated as past regardless of status. */
private fun departsAtIsPast(departsAt: String): Boolean = try {
    Instant.parse(departsAt) < Clock.System.now() - 2.hours
} catch (_: Exception) { false }

private fun TripUiModel.isUpcomingDriver(): Boolean {
    if (departsAtIsPast(departsAt)) return false
    return status == TripStatus.ACTIVE ||
        status == TripStatus.CONFIRMED ||
        status == TripStatus.THRESHOLD_NOT_MET ||
        awaitingPaymentBookingId != null
}

private fun TripUiModel.isPastDriver(): Boolean {
    val statusIsPast = status == TripStatus.COMPLETED || status == TripStatus.CANCELLED
    val departedAndNotSettling = departsAtIsPast(departsAt) && awaitingPaymentBookingId == null
    return statusIsPast || departedAndNotSettling
}

// ── Preview helpers ────────────────────────────────────────────────────────────

private fun previewDriverTrip(
    id: String,
    origin: String = "Copenhagen H",
    dest: String = "Aarhus C",
    model: TripModel = TripModel.A,
    status: TripStatus = TripStatus.ACTIVE,
    seatsTotal: Int = 4,
    seatsBooked: Int = 2,
    minThreshold: Int? = null,
    driverNetOere: Int = 17_328,
    time: String = "08:30",
): TripUiModel = TripUiModel(
    trip = Trip(
        id = id,
        driverId = "preview-driver",
        model = model,
        originName = origin,
        originLat = 55.6761,
        originLng = 12.5683,
        destName = dest,
        destLat = 56.1629,
        destLng = 10.2039,
        distanceMetres = 304_000,
        departsAt = time,
        seatsTotal = seatsTotal,
        seatsBooked = seatsBooked,
        minThreshold = minThreshold,
        priceOerePerSeat = 20_400,
        driverNetOere = driverNetOere,
        status = status,
        recurrenceDays = null,
    ),
)

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "DR-09 — Upcoming trips (Model A + B)")
@Composable
private fun PreviewMyTripsDriverUpcoming() {
    HopTheme {
        MyTripsDriverScreen(
            state = DriverUiState(
                trips = listOf(
                    previewDriverTrip(id = "1", model = TripModel.A, status = TripStatus.ACTIVE),
                    previewDriverTrip(
                        id = "2",
                        origin = "Odense St",
                        dest = "Copenhagen H",
                        model = TripModel.B,
                        status = TripStatus.CONFIRMED,
                        seatsTotal = 4,
                        seatsBooked = 1,
                        minThreshold = 2,
                        driverNetOere = 14_200,
                        time = "09:15",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "DR-09 — Model B threshold met")
@Composable
private fun PreviewMyTripsDriverModelBThresholdMet() {
    HopTheme {
        MyTripsDriverScreen(
            state = DriverUiState(
                trips = listOf(
                    previewDriverTrip(
                        id = "3",
                        origin = "Aarhus C",
                        dest = "Copenhagen H",
                        model = TripModel.B,
                        status = TripStatus.ACTIVE,
                        seatsTotal = 4,
                        seatsBooked = 3,
                        minThreshold = 2,
                        driverNetOere = 17_328,
                        time = "07:00",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "DR-09 — Past trips")
@Composable
private fun PreviewMyTripsDriverPast() {
    HopTheme {
        MyTripsDriverScreen(
            state = DriverUiState(
                trips = listOf(
                    previewDriverTrip(id = "4", status = TripStatus.COMPLETED),
                    previewDriverTrip(
                        id = "5",
                        origin = "Aalborg St",
                        dest = "Odense St",
                        status = TripStatus.CANCELLED,
                        driverNetOere = 11_800,
                        time = "14:00",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
            initialSelectedTab = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "DR-09 — Empty upcoming")
@Composable
private fun PreviewMyTripsDriverEmpty() {
    HopTheme {
        MyTripsDriverScreen(
            state = DriverUiState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "DR-09 — Loading")
@Composable
private fun PreviewMyTripsDriverLoading() {
    HopTheme {
        MyTripsDriverScreen(
            state = DriverUiState(isLoading = true),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
        )
    }
}

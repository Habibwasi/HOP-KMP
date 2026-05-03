package com.example.hop.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.mytrips.MyTripsPassengerEffect
import com.example.hop.presentation.mytrips.MyTripsPassengerEvent
import com.example.hop.presentation.mytrips.MyTripsPassengerUiState
import com.example.hop.presentation.mytrips.MyTripsPassengerViewModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.TripCard
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-07 — My Trips (Passenger) Route.
 *
 * Wires [MyTripsPassengerViewModel] from Koin, loads trips on entry,
 * handles navigation effects, and delegates all rendering to the stateless
 * [MyTripsPassengerScreen].
 */
@Composable
fun MyTripsPassengerRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTripDetailActive: (tripId: String) -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFindRide: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyTripsPassengerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onEvent(MyTripsPassengerEvent.LoadTrips)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MyTripsPassengerEffect.NavigateToTripDetailActive ->
                    onNavigateToTripDetailActive(effect.bookingId)
                is MyTripsPassengerEffect.NavigateToTripDetail ->
                    onNavigateToTripDetail(effect.tripId)
                is MyTripsPassengerEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        MyTripsPassengerScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            onNavigateToHome = onNavigateToHome,
            onNavigateToChat = onNavigateToChat,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToFindRide = onNavigateToFindRide,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * PA-07 — My Trips (Passenger) Screen.
 *
 * Stateless renderer. Tab selection (Upcoming / Past) is ephemeral visual
 * state held locally. All business-facing state flows in from the ViewModel
 * via [state]. User actions are emitted via [onEvent].
 */
@Composable
fun MyTripsPassengerScreen(
    state: MyTripsPassengerUiState,
    onEvent: (MyTripsPassengerEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFindRide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        MyTripsTopBar(onNavigateBack = onNavigateBack)

        // ── Tab row ──────────────────────────────────────────────────────────
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

        // ── Content ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingIndicator()
                selectedTab == 0 -> UpcomingTripsContent(
                    trips = state.upcomingTrips,
                    onTripClick = { bookingId -> onEvent(MyTripsPassengerEvent.SelectUpcomingTrip(bookingId)) },
                    onFindRide = onNavigateToFindRide,
                )
                else -> PastTripsContent(
                    trips = state.pastTrips,
                    onTripClick = { tripId -> onEvent(MyTripsPassengerEvent.SelectPastTrip(tripId)) },
                    onFindRide = onNavigateToFindRide,
                )
            }
        }

        // ── Bottom nav ───────────────────────────────────────────────────────
        MyTripsBottomNavBar(
            onHome = onNavigateToHome,
            onChat = onNavigateToChat,
            onProfile = onNavigateToProfile,
        )
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun MyTripsTopBar(
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
private fun LoadingIndicator(modifier: Modifier = Modifier) {
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
private fun UpcomingTripsContent(
    trips: List<TripUiModel>,
    onTripClick: (String) -> Unit,
    onFindRide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (trips.isEmpty()) {
        EmptyState(
            headline = "You haven't booked a ride yet",
            subtext = "Time to hop in — find your next trip below.",
            ctaLabel = "Find your first ride",
            onCtaClick = onFindRide,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        TripList(
            trips = trips,
            isUpcoming = true,
            onTripClick = onTripClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PastTripsContent(
    trips: List<TripUiModel>,
    onTripClick: (String) -> Unit,
    onFindRide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (trips.isEmpty()) {
        EmptyState(
            headline = "You haven't booked a ride yet",
            subtext = "Your completed and cancelled trips will appear here.",
            ctaLabel = "Find your first ride",
            onCtaClick = onFindRide,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        TripList(
            trips = trips,
            isUpcoming = false,
            onTripClick = onTripClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun TripList(
    trips: List<TripUiModel>,
    isUpcoming: Boolean,
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
            // Upcoming trips navigate to PA-08 by bookingId.
            // bookingId is populated from /trips/me/passenger; falls back to trip.id if
            // the backend has not yet added the field (graceful degradation).
            val clickId = if (isUpcoming) tripUiModel.bookingId ?: tripUiModel.id else tripUiModel.id
            TripCard(
                driverName = "Driver",
                driverInitials = "D",
                driverRating = 5.0f,
                originName = tripUiModel.originName,
                destinationName = tripUiModel.destName,
                departureTime = tripUiModel.departsAt,
                tripModel = tripUiModel.status.toBadgeType(),
                pricePerSeatOere = tripUiModel.priceOerePerSeat,
                onClick = if (tripUiModel.isBroken) ({}) else ({ onTripClick(clickId) }),
                modifier = if (tripUiModel.isBroken) Modifier.alpha(0.6f) else Modifier,
            )
        }

        item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
    }
}

@Composable
private fun MyTripsBottomNavBar(
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
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text(text = "Profile", style = MaterialTheme.typography.labelSmall) },
            colors = chipColors,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun TripStatus.toBadgeType(): BadgeType = when (this) {
    TripStatus.ACTIVE     -> BadgeType.Confirmed
    TripStatus.CONFIRMED  -> BadgeType.Confirmed
    TripStatus.COMPLETED  -> BadgeType.Completed
    TripStatus.CANCELLED  -> BadgeType.Cancelled
    TripStatus.THRESHOLD_NOT_MET -> BadgeType.Custom(
        label = "Threshold not met",
        background = Color(0xFF332200),
        contentColor = Color(0xFFFFAA00),
    )
    TripStatus.UNKNOWN    -> BadgeType.Custom(
        label = "UNKNOWN",
        background = Color(0xFF242424),
        contentColor = Color(0xFFB3B3B3),
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewTrip(
    id: String,
    origin: String = "Copenhagen H",
    dest: String = "Aarhus C",
    model: TripModel = TripModel.A,
    status: TripStatus = TripStatus.ACTIVE,
    priceOere: Int = 20400,
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
        seatsTotal = 4,
        seatsBooked = 2,
        minThreshold = null,
        priceOerePerSeat = priceOere,
        driverNetOere = 17_328,
        status = status,
        recurrenceDays = null,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "PA-07 — Upcoming trips")
@Composable
private fun PreviewMyTripsUpcoming() {
    HopTheme {
        MyTripsPassengerScreen(
            state = MyTripsPassengerUiState(
                upcomingTrips = listOf(
                    previewTrip(id = "1", status = TripStatus.ACTIVE),
                    previewTrip(
                        id = "2",
                        origin = "Odense St",
                        dest = "Copenhagen H",
                        model = TripModel.B,
                        status = TripStatus.CONFIRMED,
                        priceOere = 15_200,
                        time = "09:15",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
            onNavigateToFindRide = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "PA-07 — Past trips")
@Composable
private fun PreviewMyTripsPast() {
    HopTheme {
        MyTripsPassengerScreen(
            state = MyTripsPassengerUiState(
                pastTrips = listOf(
                    previewTrip(id = "3", status = TripStatus.COMPLETED),
                    previewTrip(
                        id = "4",
                        origin = "Aalborg St",
                        dest = "Odense St",
                        status = TripStatus.CANCELLED,
                        priceOere = 11_800,
                        time = "14:00",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
            onNavigateToFindRide = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "PA-07 — Empty upcoming")
@Composable
private fun PreviewMyTripsEmpty() {
    HopTheme {
        MyTripsPassengerScreen(
            state = MyTripsPassengerUiState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
            onNavigateToFindRide = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "PA-07 — Loading")
@Composable
private fun PreviewMyTripsLoading() {
    HopTheme {
        MyTripsPassengerScreen(
            state = MyTripsPassengerUiState(isLoading = true),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {},
            onNavigateToChat = {},
            onNavigateToProfile = {},
            onNavigateToFindRide = {},
        )
    }
}

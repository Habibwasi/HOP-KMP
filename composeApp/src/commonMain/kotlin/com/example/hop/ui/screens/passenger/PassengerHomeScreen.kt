package com.example.hop.ui.screens.passenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Google Maps
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
// Google Places autocomplete
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
// Date
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.RecentSearch
import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.UserStats
import com.example.hop.presentation.home.HomeStatsEffect
import com.example.hop.presentation.home.HomeStatsEvent
import com.example.hop.presentation.home.HomeStatsViewModel
import com.example.hop.presentation.home.SavedPlacesEvent
import com.example.hop.presentation.home.SavedPlacesEffect
import com.example.hop.presentation.home.SavedPlacesViewModel
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.search.SearchEvent
import com.example.hop.presentation.search.SearchViewModel
import com.example.hop.presentation.trip.TripEffect
import com.example.hop.presentation.trip.TripEvent
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.TripCard
import com.example.hop.ui.components.home.toChipData
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-01 — Passenger Home Content.
 *
 * Content-only composable designed to be hosted inside [HomeScreen]'s
 * [AnimatedContent] area. Manages its own ViewModels and effects but contains
 * no Scaffold, top bar, or bottom nav — those are owned by [HomeRoute].
 */
@Composable
fun PassengerHomeContent(
    onNavigateToSearchResults: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = koinViewModel(),
    tripViewModel: TripViewModel = koinViewModel(),
    authViewModel: com.example.hop.presentation.auth.AuthViewModel = koinViewModel(),
    savedPlacesViewModel: SavedPlacesViewModel = koinViewModel(),
    homeStatsViewModel: HomeStatsViewModel = koinViewModel(),
) {
    val tripState by tripViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val placesState by savedPlacesViewModel.state.collectAsStateWithLifecycle()
    val statsState by homeStatsViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tripViewModel.onEvent(TripEvent.LoadMyTripsPassenger)
        savedPlacesViewModel.onEvent(SavedPlacesEvent.Load)
        homeStatsViewModel.onEvent(HomeStatsEvent.Load)
    }

    LaunchedEffect(savedPlacesViewModel) {
        savedPlacesViewModel.effect.collectLatest { effect ->
            when (effect) {
                is SavedPlacesEffect.ShowError -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SavedPlacesEffect.PlaceSelected -> Unit // handled inline by chip click
            }
        }
    }

    LaunchedEffect(homeStatsViewModel) {
        homeStatsViewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeStatsEffect.ShowError -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    LaunchedEffect(tripViewModel) {
        tripViewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripEffect.NavigateToTripDetail -> onNavigateToTripDetail(effect.tripId)
                is TripEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    // Derive a friendly first name from the persisted user (fullName is required upstream).
    val firstName: String? = authState.currentUser?.fullName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.substringBefore(' ')

    PassengerHomeScreen(
        trips = tripState.trips,
        isLoading = tripState.isLoading,
        isRefreshing = tripState.isRefreshing,
        firstName = firstName,
        savedPlaces = placesState.places,
        userStats = statsState.stats,
        recentSearches = statsState.recentSearches,
        activeBooking = statsState.activeBooking,
        onRefresh = {
            tripViewModel.onEvent(TripEvent.RefreshMyTripsPassenger)
            savedPlacesViewModel.onEvent(SavedPlacesEvent.Load)
            homeStatsViewModel.onEvent(HomeStatsEvent.Load)
        },
        onFindRides = { origin, dest, date, seats ->
            searchViewModel.onEvent(SearchEvent.Search(origin, dest, date, seats))
            onNavigateToSearchResults()
        },
        onTripClick = { tripId ->
            tripViewModel.onEvent(TripEvent.SelectTrip(tripId))
        },
        onAddSavedPlace = { label, address, kind ->
            savedPlacesViewModel.onEvent(SavedPlacesEvent.Add(label = label, address = address, kind = kind))
        },
        onRecentSearchClick = { recent ->
            // One-tap re-run: prefill via Search event and navigate to results.
            searchViewModel.onEvent(
                SearchEvent.Search(
                    origin = recent.originLabel,
                    dest = recent.destLabel,
                    date = "Today",
                    seats = 1,
                )
            )
            onNavigateToSearchResults()
        },
        onDeleteRecentSearch = { recent ->
            homeStatsViewModel.onEvent(HomeStatsEvent.DeleteRecentSearch(recent.id))
        },
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-01 — Passenger Home Screen.
 *
 * Stateless content renderer. The top bar and bottom nav are owned by
 * [HomeScreen] — this composable renders only the scrollable body.
 * Form input (from/to/date/seats) is held as local ephemeral state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    trips: List<TripUiModel>,
    isLoading: Boolean,
    onFindRides: (origin: String, dest: String, date: String, seats: Int) -> Unit,
    onTripClick: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
    firstName: String? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    savedPlaces: List<SavedPlace> = emptyList(),
    userStats: UserStats? = null,
    recentSearches: List<RecentSearch> = emptyList(),
    activeBooking: ActiveBooking? = null,
    onAddSavedPlace: (label: String, address: String, kind: SavedPlaceKind?) -> Unit = { _, _, _ -> },
    onRecentSearchClick: (RecentSearch) -> Unit = {},
    onDeleteRecentSearch: (RecentSearch) -> Unit = {},
) {
    // ── Local ephemeral form state ─────────────────────────────────────────────────────
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Today") }
    var seats by remember { mutableIntStateOf(1) }
    var showAddPlaceSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    // Header collapses once the user has scrolled the first item more than
    // ~200 px out of view. derivedStateOf prevents recomposition on every
    // single scroll-tick — only flips when the boolean changes.
    val headerExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 200
        }
    }

    // Derive the soonest active/confirmed trip for the countdown banner.
    // Hoisted out of LazyColumn so `remember` lives in @Composable scope.
    // Used as a fallback only when the server `activeBooking` hasn't loaded yet.
    val nextActive = remember(trips) {
        trips
            .filter {
                it.status == com.example.hop.domain.model.TripStatus.CONFIRMED ||
                it.status == com.example.hop.domain.model.TripStatus.ACTIVE
            }
            .minByOrNull { it.departsAt }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── Layer 1: Full-screen Google Map background ──────────────────────
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(55.6761, 12.5683), 11f) // Copenhagen
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
            ),
        )

        // ── Layer 2: Content overlay (transparent background lets map show) ─
        Column(modifier = Modifier.fillMaxSize()) {
        // ── Greeting banner (collapses on scroll for headroom) ──────────────
        AnimatedVisibility(
            visible = headerExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            com.example.hop.ui.components.home.GreetingBanner(firstName = firstName)
        }

        // ── Scrollable body (with pull-to-refresh) ───────────────────────────
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = HopSpacing.md,
                    vertical = HopSpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
            ) {
            // Saved places chips — one-tap prefill destination.
            item {
                val chipData = remember(savedPlaces) {
                    if (savedPlaces.isEmpty()) {
                        com.example.hop.ui.components.home.DefaultSavedPlaces
                    } else {
                        savedPlaces.map { it.toChipData() }
                    }
                }
                com.example.hop.ui.components.home.SavedPlacesRow(
                    places = chipData,
                    onPlaceClick = { place ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        toLocation = place.address ?: place.label
                    },
                    onAddPlace = { showAddPlaceSheet = true },
                )
            }

            // Recent searches — one-tap re-runs. Hidden when empty.
            if (recentSearches.isNotEmpty()) {
                item {
                    com.example.hop.ui.components.home.RecentSearchesRow(
                        searches = recentSearches,
                        onSearchClick = onRecentSearchClick,
                        onDeleteSearch = onDeleteRecentSearch,
                    )
                }
            }

            // Active-booking countdown — prefer the authoritative server
            // record (handles refunds/sync); fall back to the soonest trip in
            // the local list if the server hasn't responded yet.
            val bannerSource = activeBooking
            if (bannerSource != null) {
                item {
                    com.example.hop.ui.components.home.ActiveBookingBanner(
                        departureIso = bannerSource.departsAt,
                        origin = bannerSource.originName,
                        destination = bannerSource.destName,
                        onClick = { onTripClick(bannerSource.tripId) },
                    )
                }
            } else if (nextActive != null) {
                item {
                    com.example.hop.ui.components.home.ActiveBookingBanner(
                        departureIso = nextActive.departsAt,
                        origin = nextActive.originName,
                        destination = nextActive.destName,
                        onClick = { onTripClick(nextActive.id) },
                    )
                }
            }

            // Search card
            item {
                SearchCard(
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    selectedDate = selectedDate,
                    seats = seats,
                    onFromChange = { fromLocation = it },
                    onToChange = { toLocation = it },
                    onSwap = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val tmp = fromLocation
                        fromLocation = toLocation
                        toLocation = tmp
                    },
                    onDateChange = { selectedDate = it },
                    onPickDate = { showDatePicker = true },
                    onSeatsDecrease = { if (seats > 1) seats-- },
                    onSeatsIncrease = { if (seats < 4) seats++ },
                    onFindRides = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFindRides(fromLocation, toLocation, selectedDate, seats)
                    },
                )
            }

            // Popular routes — curated until the API ships.
            item {
                Text(
                    text = "Popular routes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HopColors.authTextPrimary,
                )
            }
            item {
                com.example.hop.ui.components.home.SuggestedRoutesRow(
                    routes = com.example.hop.ui.components.home.DefaultSuggestedRoutes,
                    onRouteClick = { route ->
                        fromLocation = route.origin
                        toLocation = route.destination
                    },
                )
            }

            // Tips & announcements pager
            item {
                com.example.hop.ui.components.home.TipsPager(
                    tips = com.example.hop.ui.components.home.DefaultHomeTips,
                )
            }

            // Trust stats — wired to /users/me/stats; falls back to placeholders before first response.
            item {
                val ratingTimes10: Int = userStats?.averageRating
                    ?.let { (it * 10).toInt() }
                    ?: 0
                val completed: Int = userStats?.completedTrips ?: trips.size
                com.example.hop.ui.components.home.TrustStatsCard(
                    ratingTimes10 = ratingTimes10,
                    completedTrips = completed,
                    co2SavedKg = 0,
                )
            }

            // Referral promo
            item {
                com.example.hop.ui.components.home.ReferralCard(
                    rewardDkk = 50,
                    onShare = { /* TODO referral share — wires to PR-04 */ },
                )
            }

            // Section heading
            item {
                Text(
                    text = "Upcoming trips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HopColors.authTextPrimary,
                    modifier = Modifier.padding(top = HopSpacing.xs),
                )
            }

            // Loading / empty / list
            if (isLoading) {
                items(3) {
                    com.example.hop.ui.components.SkeletonBox(
                        height = 96.dp,
                        cornerRadius = 16.dp,
                    )
                }
            } else if (trips.isEmpty()) {
                item {
                    EmptyState(
                        headline = "No upcoming trips",
                        subtext = "Find a ride and book your first trip",
                        ctaLabel = "Find rides",
                        onCtaClick = {
                            onFindRides(fromLocation, toLocation, selectedDate, seats)
                        },
                    )
                }
            } else {
                items(trips, key = { it.id }) { tripUiModel ->
                    TripCard(
                        driverName = "Driver",
                        driverInitials = "D",
                        driverRating = 5.0f,
                        originName = tripUiModel.originName,
                        destinationName = tripUiModel.destName,
                        departureTime = tripUiModel.departsAt,
                        tripModel = when (tripUiModel.model) {
                            TripModel.A -> BadgeType.ModelA
                            TripModel.B -> BadgeType.ModelB
                            TripModel.UNKNOWN -> BadgeType.Custom(
                                label = "UNKNOWN",
                                background = HopColors.surfaceElevated,
                                contentColor = HopColors.authTextSecondary,
                            )
                        },
                        pricePerSeatOere = tripUiModel.priceOerePerSeat,
                        onClick = { onTripClick(tripUiModel.id) },
                        modifier = if (tripUiModel.isBroken) Modifier.alpha(0.5f) else Modifier,
                    )
                }
            }

            // Bottom padding so last card clears nav bar
            item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
        }
        } // end PullToRefreshBox
        } // end Column (Layer 2)
    } // end Box

    // ── Dialogs (rendered outside the Box so they overlay everything) ────────
    if (showDatePicker) {
        @OptIn(ExperimentalMaterial3Api::class)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
                        selectedDate = "${date.dayOfMonth} ${months[date.monthNumber - 1]}"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            @OptIn(ExperimentalMaterial3Api::class)
            DatePicker(state = datePickerState)
        }
    }

    if (showAddPlaceSheet) {
        com.example.hop.ui.components.home.AddSavedPlaceSheet(
            onDismiss = { showAddPlaceSheet = false },
            onSave = { label, address, kind ->
                onAddSavedPlace(label, address, kind)
            },
        )
    }
}

// ── Search card ───────────────────────────────────────────────────────────────

@Composable
private fun SearchCard(
    fromLocation: String,
    toLocation: String,
    selectedDate: String,
    seats: Int,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
    onDateChange: (String) -> Unit,
    onPickDate: () -> Unit,
    onSeatsDecrease: () -> Unit,
    onSeatsIncrease: () -> Unit,
    onFindRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)
    val context = LocalContext.current
    val placesClient = remember(context) {
        if (Places.isInitialized()) Places.createClient(context) else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = cardShape, ambientColor = Color(0x26000000))
            .clip(cardShape)
            .background(Color.White)
            .padding(HopSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            // ── From / To block with swap ─────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // From row
                    LocationRow(
                        value = fromLocation,
                        onValueChange = onFromChange,
                        onSuggestionSelected = onFromChange,
                        placeholder = "From — city or address",
                        icon = Icons.Filled.LocationOn,
                        iconTint = HopColors.primaryGreen,
                        iconDescription = "Origin",
                        placesClient = placesClient,
                    )

                    HorizontalDivider(
                        color = Color(0xFFEEEEEE),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 40.dp),
                    )

                    // To row
                    LocationRow(
                        value = toLocation,
                        onValueChange = onToChange,
                        onSuggestionSelected = onToChange,
                        placeholder = "To — city or address",
                        icon = Icons.Filled.LocationOn,
                        iconTint = HopColors.error,
                        iconDescription = "Destination",
                        placesClient = placesClient,
                    )
                }

                // Swap button — centred on the divider between the two rows
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = HopSpacing.xs)
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFDDDDDD), CircleShape)
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSwap,
                        )
                        .semantics {
                            contentDescription = "Swap origin and destination"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = null,
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // ── Date row ─────────────────────────────────────────────────────
            DateRow(
                selectedDate = selectedDate,
                onDateChange = onDateChange,
                onPickDate = onPickDate,
            )

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // ── Seat selector ─────────────────────────────────────────────────
            SeatRow(
                seats = seats,
                onDecrease = onSeatsDecrease,
                onIncrease = onSeatsIncrease,
            )

            Spacer(modifier = Modifier.height(HopSpacing.md))

            // ── Find rides button ─────────────────────────────────────────────
            HopButton(
                text = "Find rides",
                onClick = onFindRides,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Location row ──────────────────────────────────────────────────────────────

@Composable
private fun LocationRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    iconTint: Color,
    iconDescription: String,
    placesClient: PlacesClient?,
    modifier: Modifier = Modifier,
) {
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    // Fetch autocomplete suggestions with 350 ms debounce.
    // LaunchedEffect cancels the previous coroutine whenever `value` changes,
    // giving us debounce for free.
    LaunchedEffect(value) {
        if (value.length >= 2 && placesClient != null) {
            delay(350L)
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(value)
                    .build()
                val result = suspendCancellableCoroutine { cont ->
                    placesClient
                        .findAutocompletePredictions(request)
                        .addOnSuccessListener { cont.resume(it.autocompletePredictions) }
                        .addOnFailureListener { cont.resume(emptyList()) }
                }
                suggestions = result
            } catch (_: Exception) {
                suggestions = emptyList()
            }
        } else {
            suggestions = emptyList()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HopSpacing.sm, bottom = HopSpacing.sm, end = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Normal,
                ),
                cursorBrush = SolidColor(HopColors.primaryGreen),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFB0B0B0),
                                    fontWeight = FontWeight.Normal,
                                ),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        // Suggestions dropdown
        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
            ) {
                suggestions.take(4).forEachIndexed { index, prediction ->
                    val label = prediction.getFullText(null).toString()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSuggestionSelected(label)
                                suggestions = emptyList()
                            }
                            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(HopSpacing.sm))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1A1A1A)),
                            maxLines = 1,
                        )
                    }
                    if (index < suggestions.size - 1 && index < 3) {
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ── Date row ──────────────────────────────────────────────────────────────────

@Composable
private fun DateRow(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onPickDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Date",
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = "Date",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF888888)),
            )
        }

        // Date chips
        Row(horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs)) {
            DateChip(
                label = "Today",
                isSelected = selectedDate == "Today",
                onClick = { onDateChange("Today") },
            )
            DateChip(
                label = "Tomorrow",
                isSelected = selectedDate == "Tomorrow",
                onClick = { onDateChange("Tomorrow") },
            )
            // Custom date selector — opens DatePickerDialog
            val isCustom = selectedDate != "Today" && selectedDate != "Tomorrow"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (isCustom) HopColors.primaryGreen else Color(0xFFDDDDDD),
                        RoundedCornerShape(20.dp),
                    )
                    .background(if (isCustom) HopColors.primaryGreen.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable(onClick = onPickDate)
                    .padding(horizontal = HopSpacing.sm, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isCustom) selectedDate else "Pick",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isCustom) HopColors.primaryGreen else Color(0xFF444444),
                        fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isCustom) HopColors.primaryGreen else Color(0xFF888888),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (isSelected) HopColors.primaryLime else Color.Transparent
    val border = if (isSelected) HopColors.primaryLime else Color(0xFFDDDDDD)
    val textColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF444444)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}

// ── Seat row ──────────────────────────────────────────────────────────────────

@Composable
private fun SeatRow(
    seats: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HopSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Seats",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            ),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            // Decrease button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (seats > 1) Color(0xFFCCCCCC) else Color(0xFFEEEEEE),
                        shape = CircleShape,
                    )
                    .background(Color.White)
                    .clickable(
                        enabled = seats > 1,
                        onClick = onDecrease,
                    )
                    .semantics { contentDescription = "Decrease seats" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = null,
                    tint = if (seats > 1) Color(0xFF1A1A1A) else Color(0xFFCCCCCC),
                    modifier = Modifier.size(16.dp),
                )
            }

            // Seat count
            Text(
                text = "$seats",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                ),
                modifier = Modifier.width(20.dp),
            )

            // Increase button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (seats < 4) HopColors.primaryGreen else Color(0xFFEEEEEE),
                        shape = CircleShape,
                    )
                    .background(if (seats < 4) HopColors.primaryGreen.copy(alpha = 0.08f) else Color.White)
                    .clickable(
                        enabled = seats < 4,
                        onClick = onIncrease,
                    )
                    .semantics { contentDescription = "Increase seats" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = if (seats < 4) HopColors.primaryGreen else Color(0xFFCCCCCC),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun PassengerBottomNavBar(
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
        // Home — always selected on this screen
        NavigationBarItem(
            selected = true,
            onClick = { /* already on home */ },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HopColors.primaryLime,
                selectedTextColor = HopColors.primaryLime,
                indicatorColor = HopColors.primaryLime.copy(alpha = 0.12f),
                unselectedIconColor = HopColors.authTextSecondary,
                unselectedTextColor = HopColors.authTextSecondary,
            ),
        )

        // My Trips
        NavigationBarItem(
            selected = false,
            onClick = onMyTrips,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = "My Trips",
                    modifier = Modifier.size(24.dp),
                )
            },
            label = {
                Text(
                    text = "My Trips",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HopColors.primaryLime,
                selectedTextColor = HopColors.primaryLime,
                indicatorColor = HopColors.primaryLime.copy(alpha = 0.12f),
                unselectedIconColor = HopColors.authTextSecondary,
                unselectedTextColor = HopColors.authTextSecondary,
            ),
        )

        // Chat
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
            label = {
                Text(
                    text = "Chat",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HopColors.primaryLime,
                selectedTextColor = HopColors.primaryLime,
                indicatorColor = HopColors.primaryLime.copy(alpha = 0.12f),
                unselectedIconColor = HopColors.authTextSecondary,
                unselectedTextColor = HopColors.authTextSecondary,
            ),
        )

        // Profile
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
            label = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HopColors.primaryLime,
                selectedTextColor = HopColors.primaryLime,
                indicatorColor = HopColors.primaryLime.copy(alpha = 0.12f),
                unselectedIconColor = HopColors.authTextSecondary,
                unselectedTextColor = HopColors.authTextSecondary,
            ),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Passenger Home — empty state", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PassengerHomeEmptyPreview() {
    HopTheme {
        PassengerHomeScreen(
            trips = emptyList(),
            isLoading = false,
            onFindRides = { _, _, _, _ -> },
            onTripClick = {},
        )
    }
}

@Preview(name = "Passenger Home — loading", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PassengerHomeLoadingPreview() {
    HopTheme {
        PassengerHomeScreen(
            trips = emptyList(),
            isLoading = true,
            onFindRides = { _, _, _, _ -> },
            onTripClick = {},
        )
    }
}

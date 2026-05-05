package com.example.hop.ui.screens.passenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Brush
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
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.activity.compose.BackHandler

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
    onNavigateToSearchResults: (origin: String, dest: String, date: String, seats: Int) -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
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
            onNavigateToSearchResults(origin, dest, date, seats)
        },
        onTripClick = { tripId ->
            tripViewModel.onEvent(TripEvent.SelectTrip(tripId))
        },
        onAddSavedPlace = { label, address, kind ->
            savedPlacesViewModel.onEvent(SavedPlacesEvent.Add(label = label, address = address, kind = kind))
        },
        onRecentSearchClick = { recent ->
            // One-tap re-run: pass search params directly to navigation.
            onNavigateToSearchResults(
                recent.originLabel,
                recent.destLabel,
                "Today",
                1,
            )
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
    // ISO date string (yyyy-MM-dd) sent to the API. "today" / "tomorrow" resolved in TripRepositoryImpl.
    var selectedDateIso by remember { mutableStateOf("today") }
    var seats by remember { mutableIntStateOf(1) }
    var showAddPlaceSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSeatPicker by remember { mutableStateOf(false) }
    var locationPickerField by remember { mutableStateOf<String?>(null) }
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
        // Lime→background gradient band — sits behind the greeting banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            HopColors.primaryLime.copy(alpha = 0.22f),
                            HopColors.background,
                        )
                    )
                )
        )
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

            // Search hero (replaces old multi-row SearchCard)
            item {
                SearchHero(
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    selectedDate = selectedDate,
                    seats = seats,
                    onFromClick = { locationPickerField = "from" },
                    onToClick = { locationPickerField = "to" },
                    onSwap = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val tmp = fromLocation
                        fromLocation = toLocation
                        toLocation = tmp
                    },
                    onPickDate = { showDatePicker = true },
                    onPickSeats = { showSeatPicker = true },
                    onSearch = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFindRides(fromLocation, toLocation, selectedDateIso, seats)
                    },
                )
            }

            // Popular routes — curated until the API ships.
//            item {
//                Text(
//                    text = "Popular routes",
//                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                    color = HopColors.authTextPrimary,
//                )
//            }
//            item {
//                com.example.hop.ui.components.home.SuggestedRoutesRow(
//                    routes = com.example.hop.ui.components.home.DefaultSuggestedRoutes,
//                    onRouteClick = { route ->
//                        fromLocation = route.origin
//                        toLocation = route.destination
//                    },
//                )
//            }

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

//            // Referral promo
//            item {
//                com.example.hop.ui.components.home.ReferralCard(
//                    rewardDkk = 50,
//                    onShare = { /* TODO referral share — wires to PR-04 */ },
//                )
//            }

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
            if (isLoading || trips.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = HopSpacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedLoadingIndicator()
                    }
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
        } // end Column
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
                        selectedDateIso = date.toString() // yyyy-MM-dd
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

    if (showSeatPicker) {
        SeatPickerSheet(
            currentSeats = seats,
            onDismiss = { showSeatPicker = false },
            onSeatsSelected = { selected ->
                seats = selected
                showSeatPicker = false
            },
        )
    }

    // ── Location picker overlay (full-screen) ──────────────────────────────
    locationPickerField?.let { field ->
        Popup(
            properties = PopupProperties(focusable = true),
            onDismissRequest = { locationPickerField = null },
        ) {
            BackHandler { locationPickerField = null }
            LocationPickerOverlay(
                title = if (field == "from") "Where from?" else "Where to?",
                initialText = if (field == "from") fromLocation else toLocation,
                savedPlaces = savedPlaces,
                recentSearches = recentSearches,
                onDismiss = { locationPickerField = null },
                onConfirm = { address ->
                    if (field == "from") fromLocation = address
                    else toLocation = address
                    locationPickerField = null
                },
                onRouteConfirm = { origin, dest ->
                    fromLocation = origin
                    toLocation = dest
                    locationPickerField = null
                },
                onRequestAddPlace = {
                    locationPickerField = null
                    showAddPlaceSheet = true
                },
            )
        }
    }
}

// ── Search hero (BlaBlaCar × Bolt hybrid) ────────────────────────────────────

@Composable
private fun SearchHero(
    fromLocation: String,
    toLocation: String,
    selectedDate: String,
    seats: Int,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSwap: () -> Unit,
    onPickDate: () -> Unit,
    onPickSeats: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSearch = fromLocation.isNotBlank() && toLocation.isNotBlank()
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = cardShape, ambientColor = Color(0x1A000000))
            .clip(cardShape)
            .background(Color.White)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
    ) {
        Column {
            // ── From / To with timeline rail ──────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                TimelineRail(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 18.dp, bottom = 18.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp),
                ) {
                    // From tappable row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onFromClick,
                            )
                            .padding(top = HopSpacing.sm, bottom = HopSpacing.sm, end = 44.dp),
                    ) {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF888888),
                            ),
                        )
                        Text(
                            text = fromLocation.takeIf { it.isNotEmpty() } ?: "Where from?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (fromLocation.isEmpty()) Color(0xFFB8B8B8) else Color(0xFF1A1A1A),
                                fontWeight = if (fromLocation.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                            ),
                            maxLines = 1,
                        )
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    // To tappable row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToClick,
                            )
                            .padding(top = HopSpacing.sm, bottom = HopSpacing.sm, end = 44.dp),
                    ) {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF888888),
                            ),
                        )
                        Text(
                            text = toLocation.takeIf { it.isNotEmpty() } ?: "Where to?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (toLocation.isEmpty()) Color(0xFFB8B8B8) else Color(0xFF1A1A1A),
                                fontWeight = if (toLocation.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                            ),
                            maxLines = 1,
                        )
                    }
                }
                // Swap button centred between the two rows on the right
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
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Date + Seats pills ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
            ) {
                DatePill(selectedDate = selectedDate, onClick = onPickDate)
                SeatsPill(seats = seats, onClick = onPickSeats)
            }

            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // ── Search CTA ────────────────────────────────────────────────────
            HopButton(
                text = "Search",
                onClick = onSearch,
                enabled = canSearch,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Timeline rail ─────────────────────────────────────────────────────────────

@Composable
private fun TimelineRail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(HopColors.primaryGreen),
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(34.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            HopColors.primaryGreen.copy(alpha = 0.35f),
                            HopColors.error.copy(alpha = 0.35f),
                        )
                    )
                ),
        )
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = HopColors.error,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ── Date pill ─────────────────────────────────────────────────────────────────

@Composable
private fun DatePill(
    selectedDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = selectedDate,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ── Seats pill ────────────────────────────────────────────────────────────────

@Composable
private fun SeatsPill(
    seats: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (seats == 1) "1 seat" else "$seats seats",
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ── Seat picker bottom sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeatPickerSheet(
    currentSeats: Int,
    onDismiss: () -> Unit,
    onSeatsSelected: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.lg, vertical = HopSpacing.md),
        ) {
            Text(
                text = "How many seats?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                ),
            )
            Spacer(modifier = Modifier.height(HopSpacing.md))
            (1..4).forEach { n ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (n == currentSeats) HopColors.primaryLime.copy(alpha = 0.18f)
                            else Color.Transparent
                        )
                        .clickable { onSeatsSelected(n) }
                        .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (n == currentSeats) HopColors.primaryLime
                                else HopColors.cardSurfaceMuted
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (n == currentSeats) Color(0xFF1A1A1A) else HopColors.authTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(HopSpacing.md))
                    Text(
                        text = if (n == 1) "1 seat" else "$n seats",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (n == currentSeats) FontWeight.SemiBold else FontWeight.Normal,
                            color = HopColors.authTextPrimary,
                        ),
                    )
                }
                if (n < 4) {
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(start = 48.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(HopSpacing.xl))
        }
    }
}

// ── Location row (autocomplete inline helper used by LocationPickerOverlay) ────

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

// ── Location picker overlay ───────────────────────────────────────────────────

/**
 * Full-screen map overlay that appears when the user taps From or To.
 * Supports both text search (with autocomplete) and pin-on-map.
 * Tapping a suggestion confirms immediately; dragging the map and pressing
 * "Confirm pin" reverse-geocodes the crosshair center.
 *
 * When the search field is empty, a quick-action chip row (current location +
 * saved places) and a recent-searches list are shown for one-tap fills.
 * Tapping a recent-search row calls [onRouteConfirm] which sets both
 * From and To at once.
 */
@Composable
internal fun LocationPickerOverlay(
    title: String,
    initialText: String,
    savedPlaces: List<SavedPlace>,
    recentSearches: List<RecentSearch>,
    onDismiss: () -> Unit,
    onConfirm: (address: String) -> Unit,
    onRouteConfirm: (origin: String, dest: String) -> Unit,
    onRequestAddPlace: () -> Unit,
) {
    val context = LocalContext.current
    val placesClient = remember(context) {
        if (Places.isInitialized()) Places.createClient(context) else null
    }

    var searchText by remember { mutableStateOf(initialText) }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var pinnedAddress by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(55.6761, 12.5683), 13f)
    }

    // Reverse-geocode when the camera stops moving
    val isCameraMoving = cameraPositionState.isMoving
    LaunchedEffect(isCameraMoving) {
        if (!isCameraMoving) {
            val pos = cameraPositionState.position.target
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(context, Locale.getDefault())
                        .getFromLocation(pos.latitude, pos.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val a = addresses[0]
                        listOfNotNull(a.thoroughfare, a.locality, a.countryName)
                            .joinToString(", ").ifEmpty { null }
                    } else null
                }.getOrNull()
            }
            if (result != null) pinnedAddress = result
        }
    }

    // Autocomplete with 350 ms debounce
    LaunchedEffect(searchText) {
        if (searchText.length >= 2 && placesClient != null) {
            delay(350L)
            suggestions = try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchText)
                    .build()
                suspendCancellableCoroutine { cont ->
                    placesClient
                        .findAutocompletePredictions(request)
                        .addOnSuccessListener { cont.resume(it.autocompletePredictions) }
                        .addOnFailureListener { cont.resume(emptyList()) }
                }
            } catch (_: Exception) { emptyList() }
        } else {
            suggestions = emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Map layer ─────────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
            ),
        )

        // ── Centered pin (tip points at map centre) ───────────────────────────
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Pin location",
            tint = HopColors.primaryGreen,
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
                .offset(y = (-20).dp),
        )

        // ── Search bar (top) ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(HopSpacing.md),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1A1A1A),
                    )
                }
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush = SolidColor(HopColors.primaryGreen),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFB0B0B0),
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // ── Quick-action chips (saved places + current location) ─────────
            // Visible only when the text field is empty
            if (searchText.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val chipData = remember(savedPlaces) {
                    if (savedPlaces.isEmpty()) com.example.hop.ui.components.home.DefaultSavedPlaces
                    else savedPlaces.map { it.toChipData() }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
                ) {
                    // Current location chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(HopColors.primaryLime.copy(alpha = 0.15f))
                            .border(1.dp, HopColors.primaryLime, RoundedCornerShape(20.dp))
                            .clickable {
                                val addr = pinnedAddress.takeIf { it.isNotEmpty() }
                                if (addr != null) onConfirm(addr)
                            }
                            .padding(horizontal = HopSpacing.sm, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = HopColors.primaryGreen,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HopColors.primaryGreen,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                    // Saved place chips
                    chipData.take(3).forEach { place ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(HopColors.cardSurfaceMuted)
                                .border(1.dp, HopColors.cardBorder, RoundedCornerShape(20.dp))
                                .clickable { onConfirm(place.address ?: place.label) }
                                .padding(horizontal = HopSpacing.sm, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = place.icon,
                                contentDescription = null,
                                tint = HopColors.primaryGreen,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = place.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HopColors.authTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }
                    // Add chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, HopColors.authInputBorder, RoundedCornerShape(20.dp))
                            .clickable { onRequestAddPlace() }
                            .padding(horizontal = HopSpacing.sm, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add place",
                            tint = HopColors.authTextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HopColors.authTextSecondary,
                            ),
                        )
                    }
                }

                // Recent searches
                if (recentSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                    ) {
                        recentSearches.take(5).forEachIndexed { index, search ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRouteConfirm(search.originLabel, search.destLabel) }
                                    .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = HopColors.authTextSecondary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(HopSpacing.sm))
                                Text(
                                    text = "${search.originLabel} → ${search.destLabel}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = HopColors.authTextPrimary,
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (index < minOf(recentSearches.size, 5) - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF5F5F5),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 40.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Autocomplete suggestions dropdown
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                ) {
                    suggestions.take(5).forEachIndexed { index, prediction ->
                        val label = prediction.getFullText(null).toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirm(label) }
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
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF1A1A1A),
                                ),
                                maxLines = 1,
                            )
                        }
                        if (index < minOf(suggestions.size, 5) - 1) {
                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // ── Confirm pin button (bottom) ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(HopSpacing.md),
        ) {
            val confirmLabel = when {
                pinnedAddress.isNotEmpty() -> "Use: $pinnedAddress"
                else -> "Confirm pin location"
            }
            HopButton(
                text = confirmLabel,
                onClick = {
                    val addr = searchText.trim().takeIf { it.isNotEmpty() }
                        ?: pinnedAddress.takeIf { it.isNotEmpty() }
                    if (addr != null) onConfirm(addr)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Animated Loading Indicator ────────────────────────────────────────────────

/**
 * Brand-consistent loading indicator: the cartoon Hop car (with smiley
 * passenger faces, spinning wheels, and a soft ground shadow) drives in from
 * the left and then stays in the centre, bobbing gently, while the trips load.
 * Drawn on a transparent canvas so it blends seamlessly into the surrounding
 * surface, matching the splash and onboarding style.
 */
@Composable
private fun AnimatedLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "carLoading")

    // One-shot drive-in: -1f (off-screen left) → 0f (centre).
    val driveIn = remember { Animatable(-1f) }
    LaunchedEffect(Unit) {
        driveIn.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        )
    }

    val wheelAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
        ),
        label = "wheelAngle",
    )

    val bobBase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "carBob",
    )

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "textAlpha",
    )

    // Once parked, bob gently. While driving in, no bob.
    val carCenterFraction = driveIn.value
    val parked = carCenterFraction >= 0f
    val carBobFactor = if (parked) 1f else 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 240.dp, height = 130.dp),
        ) {
            val sx = size.width / 240f
            val sy = size.height / 150f
            val sr = minOf(sx, sy)
            fun x(v: Float) = v * sx
            fun y(v: Float) = v * sy
            fun r(v: Float) = v * sr

            val travelVp = carCenterFraction * 260f  // starts at -260vp, parks at 0 (centre of 240vp = x120)
            val bobVp = bobBase * carBobFactor

            // Soft ground shadow that follows the car & shrinks with bob.
            val shadowScale = 1f - 0.22f * carBobFactor * (-bobBase / 4f)
            val shadowW = x(120f) * shadowScale
            val shadowH = y(8f) * shadowScale
            drawOval(
                color = LoadingShadow.copy(alpha = 0.22f * shadowScale),
                topLeft = Offset(x(120f + travelVp) - shadowW / 2f, y(110f) - shadowH / 2f),
                size = Size(shadowW, shadowH),
            )

            withTransform({ translate((travelVp + 20f) * sx, bobVp * sy) }) {
                // Lower body
                drawRoundRect(
                    color = LoadingCarBody1,
                    topLeft = Offset(x(30f), y(58f)),
                    size = Size(x(140f), y(34f)),
                    cornerRadius = CornerRadius(r(10f)),
                )
                // Cabin
                drawRoundRect(
                    color = LoadingCarBody2,
                    topLeft = Offset(x(52f), y(38f)),
                    size = Size(x(96f), y(28f)),
                    cornerRadius = CornerRadius(r(10f)),
                )
                // Windows
                drawRoundRect(
                    color = LoadingCarWindow.copy(alpha = 0.95f),
                    topLeft = Offset(x(58f), y(43f)),
                    size = Size(x(36f), y(20f)),
                    cornerRadius = CornerRadius(r(5f)),
                )
                drawRoundRect(
                    color = LoadingCarWindow.copy(alpha = 0.95f),
                    topLeft = Offset(x(106f), y(43f)),
                    size = Size(x(36f), y(20f)),
                    cornerRadius = CornerRadius(r(5f)),
                )

                // Passenger heads
                drawCircle(LoadingPassL, r(8f), Offset(x(76f), y(53f)))
                drawCircle(LoadingPassR, r(8f), Offset(x(124f), y(53f)))

                // Eyes
                val eyeR = r(1.3f)
                drawCircle(LoadingFaceFeature, eyeR, Offset(x(73.5f), y(51.5f)))
                drawCircle(LoadingFaceFeature, eyeR, Offset(x(78.5f), y(51.5f)))
                drawCircle(LoadingFaceFeature, eyeR, Offset(x(121.5f), y(51.5f)))
                drawCircle(LoadingFaceFeature, eyeR, Offset(x(126.5f), y(51.5f)))

                // Smiles
                val smileStroke = Stroke(width = r(1.2f), cap = StrokeCap.Round)
                drawPath(
                    path = Path().apply {
                        moveTo(x(73f), y(55.5f))
                        quadraticBezierTo(x(76f), y(58f), x(79f), y(55.5f))
                    },
                    color = LoadingSmile,
                    style = smileStroke,
                )
                drawPath(
                    path = Path().apply {
                        moveTo(x(121f), y(55.5f))
                        quadraticBezierTo(x(124f), y(58f), x(127f), y(55.5f))
                    },
                    color = LoadingSmile,
                    style = smileStroke,
                )

                // Bumper stripe + lights
                drawRoundRect(
                    color = LoadingCarStripe,
                    topLeft = Offset(x(30f), y(82f)),
                    size = Size(x(140f), y(6f)),
                    cornerRadius = CornerRadius(r(3f)),
                )
                drawRoundRect(
                    color = LoadingCarLightF,
                    topLeft = Offset(x(162f), y(66f)),
                    size = Size(x(8f), y(7f)),
                    cornerRadius = CornerRadius(r(2.5f)),
                )
                drawRoundRect(
                    color = LoadingCarLightR,
                    topLeft = Offset(x(30f), y(66f)),
                    size = Size(x(8f), y(7f)),
                    cornerRadius = CornerRadius(r(2.5f)),
                )

                // Wheels
                drawSpinningWheel(Offset(x(60f), y(92f)), r(13f), wheelAngle)
                drawSpinningWheel(Offset(x(140f), y(92f)), r(13f), wheelAngle)
            }
        }

        Spacer(modifier = Modifier.height(HopSpacing.sm))

        Text(
            text = "Finding rides...",
            style = MaterialTheme.typography.labelSmall.copy(
                color = HopColors.authTextSecondary.copy(alpha = textAlpha),
            ),
        )
    }
}

private val LoadingCarBody1    = Color(0xFF26C6DA)
private val LoadingCarBody2    = Color(0xFF00ACC1)
private val LoadingCarWindow   = Color(0xFFE0F7FA)
private val LoadingCarStripe   = Color(0xFF0097A7)
private val LoadingCarLightF   = Color(0xFFFFF59D)
private val LoadingCarLightR   = Color(0xFFEF9A9A)
private val LoadingPassL       = Color(0xFFFFCC80)
private val LoadingPassR       = Color(0xFFFFAB91)
private val LoadingFaceFeature = Color(0xFF3E2723)
private val LoadingSmile       = Color(0xFFE64A19)
private val LoadingWheelOuter  = Color(0xFF37474F)
private val LoadingWheelMid    = Color(0xFF78909C)
private val LoadingWheelHub    = Color(0xFFB0BEC5)
private val LoadingWheelSpoke  = Color(0xFF546E7A)
private val LoadingShadow      = Color(0xFF263238)

// Scene colours — kept for tints used inline above (Brush gradients use Color literals directly)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpinningWheel(
    center: Offset,
    radius: Float,
    angleDeg: Float,
) {
    drawCircle(LoadingWheelOuter, radius, center)
    drawCircle(LoadingWheelMid, radius * 0.65f, center)
    rotate(angleDeg, center) {
        val spoke = radius * 0.85f
        val w = radius * 0.18f
        drawLine(
            color = LoadingWheelSpoke,
            start = Offset(center.x, center.y - spoke),
            end = Offset(center.x, center.y + spoke),
            strokeWidth = w,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = LoadingWheelSpoke,
            start = Offset(center.x - spoke, center.y),
            end = Offset(center.x + spoke, center.y),
            strokeWidth = w,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(LoadingWheelHub, radius * 0.28f, center)
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

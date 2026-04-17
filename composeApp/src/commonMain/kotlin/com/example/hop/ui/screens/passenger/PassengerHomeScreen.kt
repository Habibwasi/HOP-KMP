package com.example.hop.ui.screens.passenger

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
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
) {
    val tripState by tripViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tripViewModel.onEvent(TripEvent.LoadMyTripsPassenger)
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

    PassengerHomeScreen(
        trips = tripState.trips,
        isLoading = tripState.isLoading,
        onFindRides = { origin, dest, date, seats ->
            searchViewModel.onEvent(SearchEvent.Search(origin, dest, date, seats))
            onNavigateToSearchResults()
        },
        onTripClick = { tripId ->
            tripViewModel.onEvent(TripEvent.SelectTrip(tripId))
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
@Composable
fun PassengerHomeScreen(
    trips: List<TripUiModel>,
    isLoading: Boolean,
    onFindRides: (origin: String, dest: String, date: String, seats: Int) -> Unit,
    onTripClick: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local ephemeral form state ────────────────────────────────────────────
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Today") }
    var seats by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface),
    ) {
        // ── Scrollable body ──────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = HopSpacing.md,
                vertical = HopSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
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
                        val tmp = fromLocation
                        fromLocation = toLocation
                        toLocation = tmp
                    },
                    onDateChange = { selectedDate = it },
                    onSeatsDecrease = { if (seats > 1) seats-- },
                    onSeatsIncrease = { if (seats < 4) seats++ },
                    onFindRides = { onFindRides(fromLocation, toLocation, selectedDate, seats) },
                )
            }

            // Section heading
            item {
                Text(
                    text = "Upcoming trips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HopColors.textPrimary,
                    modifier = Modifier.padding(top = HopSpacing.xs),
                )
            }

            // Loading / empty / list
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = HopSpacing.xl),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = HopColors.primaryLime,
                            modifier = Modifier.size(36.dp),
                        )
                    }
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
                                contentColor = HopColors.textSecondary,
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
    onSeatsDecrease: () -> Unit,
    onSeatsIncrease: () -> Unit,
    onFindRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

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
                        placeholder = "From — city or address",
                        icon = Icons.Filled.LocationOn,
                        iconTint = HopColors.primaryGreen,
                        iconDescription = "Origin",
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
                        placeholder = "To — city or address",
                        icon = Icons.Filled.LocationOn,
                        iconTint = HopColors.error,
                        iconDescription = "Destination",
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
    placeholder: String,
    icon: ImageVector,
    iconTint: Color,
    iconDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HopSpacing.sm, bottom = HopSpacing.sm, end = 44.dp), // right padding leaves room for swap button
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            tint = iconTint,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 0.dp),
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
}

// ── Date row ──────────────────────────────────────────────────────────────────

@Composable
private fun DateRow(
    selectedDate: String,
    onDateChange: (String) -> Unit,
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
            // Custom date selector trigger
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
                    .clickable { /* platform date picker — post-MVP */ }
                    .padding(horizontal = HopSpacing.sm, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedDate != "Today" && selectedDate != "Tomorrow") selectedDate else "Pick",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF444444)),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF888888),
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
        containerColor = HopColors.surfaceElevated,
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
                unselectedIconColor = HopColors.textSecondary,
                unselectedTextColor = HopColors.textSecondary,
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
                unselectedIconColor = HopColors.textSecondary,
                unselectedTextColor = HopColors.textSecondary,
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
                unselectedIconColor = HopColors.textSecondary,
                unselectedTextColor = HopColors.textSecondary,
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
                unselectedIconColor = HopColors.textSecondary,
                unselectedTextColor = HopColors.textSecondary,
            ),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Passenger Home — empty state", showBackground = true, backgroundColor = 0xFF1A1A1A)
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

@Preview(name = "Passenger Home — loading", showBackground = true, backgroundColor = 0xFF1A1A1A)
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

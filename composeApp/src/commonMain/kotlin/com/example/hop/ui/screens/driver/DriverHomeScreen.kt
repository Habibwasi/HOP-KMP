package com.example.hop.ui.screens.driver

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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.domain.model.UserRole
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.RoleTogglePill
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
 * DR-01 — Driver Home Route.
 *
 * Wires [DriverViewModel] from Koin, dispatches [DriverEvent.LoadDriverHome] on
 * entry, handles one-shot effects, and delegates all rendering to the stateless
 * [DriverHomeScreen].
 */
@Composable
fun DriverHomeRoute(
    onNavigateToPassengerHome: () -> Unit,
    onNavigateToPostTripModelSelect: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    onNavigateToTaxDashboard: () -> Unit,
    onNavigateToNotifications: () -> Unit,
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
                is DriverEffect.NavigateToPostTrip -> onNavigateToPostTripModelSelect()
                is DriverEffect.NavigateToTripDetail -> onNavigateToTripDetail(effect.tripId)
                is DriverEffect.NavigateToTaxDashboard -> onNavigateToTaxDashboard()
                is DriverEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                // Onboarding effects are handled by their own routes; ignore here.
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        DriverHomeScreen(
            state = state,
            onPostTrip = { viewModel.onEvent(DriverEvent.RequestPostTrip) },
            onTripClick = { tripId -> viewModel.onEvent(DriverEvent.SelectTrip(tripId)) },
            onEarningsBannerClick = { viewModel.onEvent(DriverEvent.TapEarningsBanner) },
            onNavigateToPassengerHome = onNavigateToPassengerHome,
            onMyTrips = onNavigateToMyTrips,
            onChat = onNavigateToChat,
            onProfile = onNavigateToProfile,
            onNotifications = onNavigateToNotifications,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-01 — Driver Home Screen.
 *
 * Stateless renderer. Receives all state and callbacks from [DriverHomeRoute].
 * Role toggle is held locally — switching to PASSENGER fires [onNavigateToPassengerHome].
 */
@Composable
fun DriverHomeScreen(
    state: DriverUiState,
    onPostTrip: () -> Unit,
    onTripClick: (tripId: String) -> Unit,
    onEarningsBannerClick: () -> Unit,
    onNavigateToPassengerHome: () -> Unit,
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRole by remember { mutableStateOf(UserRole.DRIVER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        DriverTopBar(
            selectedRole = selectedRole,
            onRoleChange = { role ->
                selectedRole = role
                if (role == UserRole.PASSENGER) onNavigateToPassengerHome()
            },
            onNotifications = onNotifications,
        )

        // ── Scrollable body ───────────────────────────────────────────────────
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
            // Earnings banner
            item {
                EarningsBanner(
                    monthlyEarningsOere = state.monthlyEarningsOere,
                    estimatedTaxOere = state.estimatedTaxOere,
                    onClick = onEarningsBannerClick,
                )
            }

            // Post a Trip button
            item {
                HopButton(
                    text = "Post a Trip",
                    onClick = onPostTrip,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Section heading
            item {
                Text(
                    text = "My Trips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HopColors.textPrimary,
                    modifier = Modifier.padding(top = HopSpacing.xs),
                )
            }

            // Loading / empty / list
            if (state.isLoading) {
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
            } else if (state.trips.isEmpty()) {
                item {
                    EmptyState(
                        headline = "Post your first trip to start earning",
                        subtext = "Set a route, pick a time, and let passengers book seats.",
                        ctaLabel = "Post a trip",
                        onCtaClick = onPostTrip,
                    )
                }
            } else {
                items(state.trips, key = { it.id }) { tripUiModel ->
                    DriverTripCard(
                        tripUiModel = tripUiModel,
                        onClick = { onTripClick(tripUiModel.id) },
                        modifier = if (tripUiModel.isBroken) Modifier.alpha(0.5f) else Modifier,
                    )
                }
            }

            // Bottom padding so last card clears nav bar
            item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
        }

        // ── Bottom navigation bar ─────────────────────────────────────────────
        DriverBottomNavBar(
            onMyTrips = onMyTrips,
            onChat = onChat,
            onProfile = onProfile,
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DriverTopBar(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hop logotype
        Text(
            text = "HOP",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HopColors.primaryLime,
                letterSpacing = 3.sp,
            ),
            modifier = Modifier.weight(1f),
        )

        // Role toggle pill
        RoleTogglePill(
            selectedRole = selectedRole,
            onRoleChange = onRoleChange,
        )

        Spacer(modifier = Modifier.width(HopSpacing.sm))

        // Bell icon button
        IconButton(
            onClick = onNotifications,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = HopColors.textSecondary,
                modifier = Modifier.size(24.dp),
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
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 0.sp,
                ),
            )

            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // Estimated tax subtext
            Text(
                text = "Est. tax this month: DKK ${estimatedTaxOere / 100}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF1A1A1A).copy(alpha = 0.65f),
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        // Tap hint chevron (top-right)
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF1A1A1A).copy(alpha = 0.35f),
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
                            background = HopColors.surfaceElevated,
                            contentColor = HopColors.textSecondary,
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
                        TripStatus.UNKNOWN -> BadgeType.Custom(
                            label = "UNKNOWN",
                            background = HopColors.surfaceElevated,
                            contentColor = HopColors.textSecondary,
                        )
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
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
                    text = "DKK ${tripUiModel.trip.driverNetOere / 100}/seat",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        fontSize = 16.sp,
                    ),
                )
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
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1A1A1A)),
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
                    .background(Color(0xFF1A1A1A)),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = destination,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A1A1A),
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
        containerColor = HopColors.surfaceElevated,
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
            icon = Icons.Outlined.Chat,
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
            unselectedIconColor = HopColors.textSecondary,
            unselectedTextColor = HopColors.textSecondary,
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
            state = DriverUiState(
                isLoading = false,
                trips = emptyList(),
                monthlyEarningsOere = 0,
                estimatedTaxOere = 0,
            ),
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
            onNavigateToPassengerHome = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
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
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
            onNavigateToPassengerHome = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
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
            onPostTrip = {},
            onTripClick = {},
            onEarningsBannerClick = {},
            onNavigateToPassengerHome = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
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

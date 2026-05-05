package com.example.hop.ui.screens.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import com.example.hop.ui.components.HopLogo
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.UserRole
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.home.HomeStatsEvent
import com.example.hop.presentation.home.HomeStatsViewModel
import com.example.hop.ui.components.RoleTogglePill
import com.example.hop.ui.components.UnreadBadge
import com.example.hop.ui.screens.driver.DriverHomeContent
import com.example.hop.ui.screens.driver.DriverHomeScreen
import com.example.hop.ui.screens.passenger.PassengerHomeContent
import com.example.hop.ui.screens.passenger.PassengerHomeScreen
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * SH-01 — Home Route.
 *
 * Owns the [selectedRole] toggle. The [RoleTogglePill] is always visible so
 * any logged-in user can freely switch to the Driver tab and explore it.
 *
 * Driver onboarding is triggered lazily — only when a non-driver taps
 * "Post a Trip" inside [DriverHomeContent], not at role-toggle time.
 *
 * A single [Scaffold] (and its [SnackbarHostState]) is shared between both
 * sub-content composables to avoid layering two scaffolds on top of each other.
 */
@Composable
fun HomeRoute(
    onNavigateToSearchResults: (origin: String, dest: String, date: String, seats: Int) -> Unit,
    onNavigateToMyTripsPassenger: () -> Unit,
    onNavigateToMyTripsDriver: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    onNavigateToTripDetailDriver: (tripId: String) -> Unit,
    onNavigateToPostTripModelSelect: () -> Unit,
    onNavigateToTaxDashboard: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDriverRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    /** Total unseen notifications — drives the bell icon badge. */
    notificationsUnread: Int = 0,
    /** Total unread chat messages — drives the bottom nav chat badge. */
    chatUnread: Int = 0,
    authViewModel: AuthViewModel,
    homeStatsViewModel: HomeStatsViewModel = koinViewModel(),
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val homeStatsState by homeStatsViewModel.state.collectAsStateWithLifecycle()
    val hasDriverRole = authState.currentUser?.roles?.contains(UserRole.DRIVER) == true

    // Fetch the unread notifications count once on first composition. The
    // PassengerHomeContent owns its own HomeStatsViewModel instance for its
    // own state (recent searches etc.), so this is a separate fetch — that's
    // acceptable: the badge is small and refreshes on home re-entry.
    LaunchedEffect(Unit) {
        homeStatsViewModel.onEvent(HomeStatsEvent.Load)
    }

    var selectedRole by rememberSaveable(
        stateSaver = Saver(
            save = { it.name },
            restore = { name -> UserRole.entries.firstOrNull { it.name == name } ?: UserRole.PASSENGER },
        ),
    ) { mutableStateOf(UserRole.PASSENGER) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Only reset to PASSENGER when the DRIVER role is actively revoked
    // (had it → lost it). We must NOT reset when hasDriverRole is simply
    // false on first load — that would kick a non-driver off the Driver
    // tab they deliberately chose and cause the "back goes to Passenger"
    // symptom.
    var prevHasDriverRole by remember { mutableStateOf(hasDriverRole) }
    LaunchedEffect(hasDriverRole) {
        if (prevHasDriverRole && !hasDriverRole) selectedRole = UserRole.PASSENGER
        prevHasDriverRole = hasDriverRole
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        HomeScreen(
            selectedRole = selectedRole,
            onRoleChange = { selectedRole = it },
            notificationsUnread = homeStatsState.unreadCount.coerceAtLeast(notificationsUnread),
            chatUnread = chatUnread,
            onMyTrips = {
                if (selectedRole == UserRole.DRIVER) onNavigateToMyTripsDriver()
                else onNavigateToMyTripsPassenger()
            },
            onChat = onNavigateToChat,
            onProfile = onNavigateToProfile,
            onNotifications = onNavigateToNotifications,
            modifier = Modifier.padding(innerPadding),
            content = { role ->
                when (role) {
                    UserRole.DRIVER -> DriverHomeContent(
                        hasDriverRole = hasDriverRole,
                        onNavigateToPostTripModelSelect = onNavigateToPostTripModelSelect,
                        onNavigateToTripDetail = onNavigateToTripDetailDriver,
                        onNavigateToTaxDashboard = onNavigateToTaxDashboard,
                        onNavigateToDriverRegistration = onNavigateToDriverRegistration,
                        snackbarHostState = snackbarHostState,
                    )
                    else -> PassengerHomeContent(
                        onNavigateToSearchResults = onNavigateToSearchResults,
                        onNavigateToTripDetail = onNavigateToTripDetail,
                        snackbarHostState = snackbarHostState,
                    )
                }
            },
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * SH-01 — Home Screen.
 *
 * Stateless renderer. Owns the shared top bar, the animated content area, and
 * the shared bottom navigation bar. The [content] slot is responsible for
 * rendering the body for the current [selectedRole] — keeping this composable
 * free of ViewModel / Koin dependencies so it can be previewed in isolation.
 */
@Composable
fun HomeScreen(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    notificationsUnread: Int = 0,
    chatUnread: Int = 0,
    content: @Composable (role: UserRole) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {
        // ── Shared top bar ────────────────────────────────────────────────────
        HomeTopBar(
            selectedRole = selectedRole,
            onRoleChange = onRoleChange,
            onNotifications = onNotifications,            notificationsUnread = notificationsUnread,        )

        // ── Animated content area ─────────────────────────────────────────────
        AnimatedContent(
            targetState = selectedRole,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 200))
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "homeContentSwitch",
        ) { role ->
            content(role)
        }

        // ── Shared bottom nav bar ─────────────────────────────────────────────
        HomeBottomNavBar(
            onMyTrips = onMyTrips,
            onChat = onChat,
            onProfile = onProfile,            chatUnread = chatUnread,        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    notificationsUnread: Int = 0,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hop logotype
        HopLogo()

        Spacer(modifier = Modifier.weight(1f))

        // Role toggle pill — always visible; driver tap redirects to registration
        // if the user hasn't completed the driver onboarding flow yet.
        RoleTogglePill(
            selectedRole = selectedRole,
            onRoleChange = onRoleChange,
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))

        // Bell icon with unread badge overlay
        Box(modifier = Modifier.size(40.dp)) {
            IconButton(
                onClick = onNotifications,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = HopColors.authTextSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }
            UnreadBadge(
                count = notificationsUnread,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp),
            )
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun HomeBottomNavBar(
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    chatUnread: Int = 0,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = HopColors.authInputSurface,
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
                Box {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(24.dp),
                    )
                    UnreadBadge(
                        count = chatUnread,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp),
                    )
                }
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
                    imageVector = Icons.Outlined.Person,
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

@Preview(name = "Home — Passenger only (no driver role)", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenPassengerOnlyPreview() {
    HopTheme {
        HomeScreen(
            selectedRole = UserRole.PASSENGER,
            onRoleChange = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
        ) {
            PassengerHomeScreen(
                trips = emptyList(),
                isLoading = false,
                onFindRides = { _, _, _, _ -> },
                onTripClick = {},
            )
        }
    }
}

@Preview(name = "Home — Driver role, passenger selected", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenDriverRolePassengerPreview() {
    HopTheme {
        HomeScreen(
            selectedRole = UserRole.PASSENGER,
            onRoleChange = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
        ) {
            PassengerHomeScreen(
                trips = emptyList(),
                isLoading = false,
                onFindRides = { _, _, _, _ -> },
                onTripClick = {},
            )
        }
    }
}

@Preview(name = "Home — Driver role, driver selected", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenDriverRoleDriverPreview() {
    HopTheme {
        HomeScreen(
            selectedRole = UserRole.DRIVER,
            onRoleChange = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
        ) {
            DriverHomeScreen(
                state = DriverUiState(),
                hasDriverRole = true,
                onPostTrip = {},
                onTripClick = {},
                onEarningsBannerClick = {},
            )
        }
    }
}

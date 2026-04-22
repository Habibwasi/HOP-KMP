package com.example.hop.ui.screens.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.hop.ui.components.RoleTogglePill
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
 * Owns the [selectedRole] toggle and reads [AuthViewModel.state] to decide
 * whether the [RoleTogglePill] should be visible. Driver content is only
 * accessible when the current user holds the [UserRole.DRIVER] role.
 *
 * A single [Scaffold] (and its [SnackbarHostState]) is shared between both
 * sub-content composables to avoid layering two scaffolds on top of each other.
 */
@Composable
fun HomeRoute(
    onNavigateToSearchResults: () -> Unit,
    onNavigateToMyTripsPassenger: () -> Unit,
    onNavigateToMyTripsDriver: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    onNavigateToPostTripModelSelect: () -> Unit,
    onNavigateToTaxDashboard: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val hasDriverRole = authState.currentUser?.roles?.contains(UserRole.DRIVER) == true

    var selectedRole by rememberSaveable(
        stateSaver = Saver(
            save = { it.name },
            restore = { name -> UserRole.entries.firstOrNull { it.name == name } ?: UserRole.PASSENGER },
        ),
    ) { mutableStateOf(UserRole.PASSENGER) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Reset to PASSENGER if the user's driver role is revoked.
    LaunchedEffect(hasDriverRole) {
        if (!hasDriverRole) selectedRole = UserRole.PASSENGER
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        HomeScreen(
            selectedRole = selectedRole,
            hasDriverRole = hasDriverRole,
            onRoleChange = { selectedRole = it },
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
                        onNavigateToPostTripModelSelect = onNavigateToPostTripModelSelect,
                        onNavigateToTripDetail = onNavigateToTripDetail,
                        onNavigateToTaxDashboard = onNavigateToTaxDashboard,
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
    hasDriverRole: Boolean,
    onRoleChange: (UserRole) -> Unit,
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
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
            hasDriverRole = hasDriverRole,
            onRoleChange = onRoleChange,
            onNotifications = onNotifications,
        )

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
            onProfile = onProfile,
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    selectedRole: UserRole,
    hasDriverRole: Boolean,
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
        HopLogo()

        Spacer(modifier = Modifier.weight(1f))

        // Role toggle pill — only visible when the user holds the DRIVER role
        if (hasDriverRole) {
            RoleTogglePill(
                selectedRole = selectedRole,
                onRoleChange = onRoleChange,
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
        }

        // Bell icon
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
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun HomeBottomNavBar(
    onMyTrips: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
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
                Icon(
                    imageVector = Icons.Outlined.Chat,
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
            hasDriverRole = false,
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
            hasDriverRole = true,
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
            hasDriverRole = true,
            onRoleChange = {},
            onMyTrips = {},
            onChat = {},
            onProfile = {},
            onNotifications = {},
        ) {
            DriverHomeScreen(
                state = DriverUiState(),
                onPostTrip = {},
                onTripClick = {},
                onEarningsBannerClick = {},
            )
        }
    }
}

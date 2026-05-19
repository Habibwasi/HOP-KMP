package com.example.hop.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.hop.network.ConnectivityObserver
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.components.NoInternetBanner
import com.example.hop.ui.screens.auth.ForgotPasswordRoute
import com.example.hop.ui.screens.auth.SetNewPasswordRoute
import com.example.hop.ui.screens.auth.LoginRoute
import com.example.hop.ui.screens.auth.OnboardingScreen
import com.example.hop.ui.screens.auth.SignUpRoute
import com.example.hop.ui.screens.passenger.BookingConfirmationRoute
import com.example.hop.ui.screens.passenger.BookingSuccessRoute
import com.example.hop.ui.screens.passenger.CancellationConfirmationRoute
import com.example.hop.ui.screens.passenger.MyTripsPassengerRoute
import com.example.hop.ui.screens.shared.HomeRoute
import com.example.hop.ui.screens.shared.SplashRoute
import com.example.hop.ui.screens.passenger.SearchResultsRoute
import com.example.hop.ui.screens.settlement.PassengerSettlementRoute
import com.example.hop.ui.screens.settlement.DriverSettlementRoute
import com.example.hop.ui.screens.settlement.PastTripDetailDriverRoute
import com.example.hop.ui.screens.passenger.TripDetailActiveRoute
import com.example.hop.ui.screens.passenger.TripDetailRoute
import com.example.hop.ui.screens.passenger.RateDriverRoute
import com.example.hop.ui.screens.driver.MyTripsDriverRoute
import com.example.hop.ui.screens.driver.TripDetailActiveDriverRoute
import com.example.hop.ui.screens.driver.MarkTripCompleteRoute
import com.example.hop.ui.screens.driver.RatePassengerRoute
import com.example.hop.ui.screens.driver.CarDetailsRoute
import com.example.hop.ui.screens.driver.EnableDriverMobilepayRoute
import com.example.hop.ui.screens.driver.ReviewPendingRoute
import com.example.hop.ui.screens.driver.PostTripModelSelectRoute
import com.example.hop.ui.screens.driver.PostTripModelARoute
import com.example.hop.ui.screens.driver.PostTripModelBRoute
import com.example.hop.ui.screens.driver.PriceReviewRoute
import com.example.hop.ui.screens.driver.TaxDashboardRoute
import com.example.hop.ui.screens.driver.TaxReportDownloadRoute
import com.example.hop.ui.screens.shared.NotificationsRoute
import com.example.hop.ui.screens.shared.ChatRoute
import com.example.hop.ui.screens.shared.OtherProfileRoute
import com.example.hop.ui.screens.shared.OwnProfileRoute
import com.example.hop.ui.screens.shared.SettingsRoute
import org.koin.compose.koinInject

/**
 * Root NavHost for the Hop app.
 *
 * Auth flow (Splash → SignUp / Login → OtpVerification) is wired to real screens
 * once they are built; for now every destination shows a [TodoScreen] stub.
 *
 * Navigation into auth destinations is driven by [AuthEffectHandler], not by
 * direct NavController calls inside composables.
 */
@Composable
fun HopNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val connectivityObserver: ConnectivityObserver = koinInject()
    val isConnected by connectivityObserver.isConnected.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = HopRoutes.Splash,
        ) {

        // ── Onboarding ────────────────────────────────────────────────────

        composable<HopRoutes.Splash> {
            SplashRoute(
                onComplete = {
                    navController.navigate(HopRoutes.Onboarding) {
                        popUpTo(HopRoutes.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.Onboarding> {
            OnboardingScreen(
                onNavigateToSignUp = { navController.navigate(HopRoutes.SignUp) },
                onNavigateToLogin  = { navController.navigate(HopRoutes.Login) },
            )
        }

        composable<HopRoutes.SignUp> {
            SignUpRoute(
                onNavigateToHome = {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(HopRoutes.Login)
                },
            )
        }

        composable<HopRoutes.Login> {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(HopRoutes.ForgotPassword)
                },
                onNavigateToSignUp = {
                    navController.navigate(HopRoutes.SignUp)
                },
            )
        }

        composable<HopRoutes.ForgotPassword> {
            ForgotPasswordRoute(
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.SetNewPassword> {
            SetNewPasswordRoute(
                onPasswordUpdated = {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        // ── Home (Passenger + Driver via role toggle) ──────────────────────────────

        composable<HopRoutes.Home> {
            HomeRoute(
                onNavigateToSearchResults = { origin, dest, date, seats ->
                    navController.navigate(HopRoutes.SearchResults(origin, dest, date, seats))
                },
                onNavigateToMyTripsPassenger = {
                    navController.navigate(HopRoutes.MyTripsPassenger)
                },
                onNavigateToMyTripsDriver = {
                    navController.navigate(HopRoutes.MyTripsDriver)
                },
                onNavigateToChat = { bookingId ->
                    if (bookingId != null) {
                        navController.navigate(HopRoutes.Chat(bookingId = bookingId))
                    } else {
                        // No active booking — show My Trips so the user can find one.
                        navController.navigate(HopRoutes.MyTripsPassenger)
                    }
                },
                onNavigateToProfile = {
                    val userId = "" // currentUserId from AuthViewModel is not in scope here;
                    // navigate with empty string — OwnProfileRoute calls /users/me, not /users/:id
                    navController.navigate(HopRoutes.Profile(userId = userId))
                },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(HopRoutes.TripDetail(id = tripId))
                },
                onNavigateToTripDetailActive = { bookingId ->
                    navController.navigate(HopRoutes.TripDetailActive(bookingId = bookingId))
                },
                onNavigateToTripDetailDriver = { tripId ->
                    navController.navigate(HopRoutes.TripDetailActiveDriver(tripId = tripId))
                },
                onNavigateToPostTripModelSelect = {
                    navController.navigate(HopRoutes.PostTripModelSelect)
                },
                onNavigateToTaxDashboard = {
                    navController.navigate(HopRoutes.TaxDashboard)
                },
                onNavigateToNotifications = {
                    navController.navigate(HopRoutes.Notifications)
                },
                onNavigateToDriverRegistration = {
                    navController.navigate(HopRoutes.EnableDriverStep1)
                },
                authViewModel = authViewModel,
            )
        }

        // ── Passenger ─────────────────────────────────────────────────────

        composable<HopRoutes.SearchResults> { backStackEntry ->
            val route: HopRoutes.SearchResults = backStackEntry.toRoute()
            SearchResultsRoute(
                origin = route.origin,
                dest = route.dest,
                date = route.date,
                seats = route.seats,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(HopRoutes.TripDetail(id = tripId))
                },
            )
        }

        composable<HopRoutes.TripDetail> { backStackEntry ->
            val route: HopRoutes.TripDetail = backStackEntry.toRoute()
            TripDetailRoute(
                tripId = route.id,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToOtherProfile = { driverId ->
                    navController.navigate(HopRoutes.OtherProfile(userId = driverId))
                },
                onNavigateToBookingConfirmation = { tripId ->
                    navController.navigate(HopRoutes.BookingConfirmation(tripId = tripId))
                },
            )
        }

        composable<HopRoutes.BookingConfirmation> { backStackEntry ->
            val route: HopRoutes.BookingConfirmation = backStackEntry.toRoute()
            BookingConfirmationRoute(
                tripId = route.tripId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSuccess = { bookingId ->
                    navController.navigate(HopRoutes.BookingSuccess(bookingId = bookingId)) {
                        popUpTo(HopRoutes.BookingConfirmation(tripId = "")) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.BookingSuccess> { backStackEntry ->
            val route: HopRoutes.BookingSuccess = backStackEntry.toRoute()
            BookingSuccessRoute(
                bookingId = route.bookingId,
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(HopRoutes.Home) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.MyTripsPassenger> {
            MyTripsPassengerRoute(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTripDetailActive = { tripId ->
                    navController.navigate(HopRoutes.TripDetailActive(bookingId = tripId))
                },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(HopRoutes.TripDetail(id = tripId))
                },
                onNavigateToPassengerSettlement = { bookingId ->
                    navController.navigate(HopRoutes.PassengerSettlement(bookingId = bookingId))
                },
                onNavigateToHome = {
                    // popBackStack restores the existing Home entry, preserving
                    // the selectedRole in rememberSaveable (e.g. DRIVER tab).
                    // navigate() would create a new entry and reset to PASSENGER.
                    navController.popBackStack<HopRoutes.Home>(inclusive = false)
                },
                onNavigateToChat = {
                    // Already on MyTrips — user can tap a trip row to reach chat.
                },
                onNavigateToProfile = {
                    navController.navigate(HopRoutes.Profile(userId = ""))
                },
                onNavigateToFindRide = {
                    navController.navigate(HopRoutes.SearchResults())
                },
            )
        }

        composable<HopRoutes.TripDetailActive> { backStackEntry ->
            val route: HopRoutes.TripDetailActive = backStackEntry.toRoute()
            TripDetailActiveRoute(
                bookingId = route.bookingId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToChat = { bookingId ->
                    navController.navigate(HopRoutes.Chat(bookingId = bookingId))
                },
                onNavigateToCancellationConfirmation = { bookingId ->
                    navController.navigate(HopRoutes.CancellationConfirmation(bookingId = bookingId)) {
                        popUpTo(HopRoutes.TripDetailActive(bookingId = bookingId)) { inclusive = true }
                    }
                },
                onNavigateToPassengerSettlement = { bookingId ->
                    navController.navigate(HopRoutes.PassengerSettlement(bookingId = bookingId))
                },
            )
        }

        composable<HopRoutes.RateDriver> { backStackEntry ->
            val route: HopRoutes.RateDriver = backStackEntry.toRoute()
            RateDriverRoute(
                bookingId = route.bookingId,
                onNavigateBack = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
            )
        }

        composable<HopRoutes.CancellationConfirmation> { backStackEntry ->
            val route: HopRoutes.CancellationConfirmation = backStackEntry.toRoute()
            CancellationConfirmationRoute(
                bookingId = route.bookingId,
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.MyTripsPassenger) { inclusive = true }
                    }
                },
            )
        }

        // ── Driver ────────────────────────────────────────────────────────


        composable<HopRoutes.EnableDriverStep1> {
            CarDetailsRoute(
                onNavigateToHome = {
                    // Proceed to MobilePay step (step 2) instead of going directly home.
                    navController.navigate(HopRoutes.EnableDriverStep2)
                },
                onNavigateToReviewPending = {
                    navController.navigate(HopRoutes.EnableDriverStep2)
                },
                onNavigateBack = { navController.navigateUp() },
                authViewModel = authViewModel,
            )
        }

        composable<HopRoutes.EnableDriverStep2> {
            // DR-03 — MobilePay number step (replaces defunct licence upload).
            EnableDriverMobilepayRoute(
                onNavigateToHome = {
                    navController.popBackStack<HopRoutes.Home>(inclusive = false)
                },
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.EnableDriverStep3> {
            ReviewPendingRoute(
                onNavigateToHome = {
                    navController.popBackStack<HopRoutes.Home>(inclusive = false)
                },
            )
        }

        composable<HopRoutes.PostTripModelSelect> {
            PostTripModelSelectRoute(
                onNavigateToModelA = { navController.navigate(HopRoutes.PostTripModelA) },
                onNavigateToModelB = { navController.navigate(HopRoutes.PostTripModelB) },
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.PostTripModelA> { currentEntry ->
            val parentEntry = remember(currentEntry) {
                navController.getBackStackEntry<HopRoutes.PostTripModelSelect>()
            }
            PostTripModelARoute(
                onNavigateToPriceReview = { navController.navigate(HopRoutes.PriceReview) },
                onNavigateBack = { navController.navigateUp() },
                viewModel = koinViewModel(viewModelStoreOwner = parentEntry),
            )
        }

        composable<HopRoutes.PostTripModelB> { currentEntry ->
            val parentEntry = remember(currentEntry) {
                navController.getBackStackEntry<HopRoutes.PostTripModelSelect>()
            }
            PostTripModelBRoute(
                onNavigateToPriceReview = { navController.navigate(HopRoutes.PriceReview) },
                onNavigateBack = { navController.navigateUp() },
                viewModel = koinViewModel(viewModelStoreOwner = parentEntry),
            )
        }

        composable<HopRoutes.PriceReview> { currentEntry ->
            val parentEntry = remember(currentEntry) {
                navController.getBackStackEntry<HopRoutes.PostTripModelSelect>()
            }
            PriceReviewRoute(
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsDriver) {
                        popUpTo(HopRoutes.PostTripModelSelect) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.navigateUp() },
                viewModel = koinViewModel(viewModelStoreOwner = parentEntry),
            )
        }

        composable<HopRoutes.MyTripsDriver> {
            MyTripsDriverRoute(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTripDetailActiveDriver = { tripId ->
                    navController.navigate(HopRoutes.TripDetailActiveDriver(tripId = tripId))
                },
                onNavigateToDriverSettlement = { tripId ->
                    navController.navigate(HopRoutes.DriverSettlement(tripId = tripId))
                },
                onNavigateToPastTripDetail = { tripId ->
                    navController.navigate(HopRoutes.PastTripDetailDriver(tripId = tripId))
                },
                onNavigateToPostTrip = {
                    navController.navigate(HopRoutes.PostTripModelSelect)
                },
                onNavigateToHome = {
                    // popBackStack preserves the existing Home entry and its
                    // rememberSaveable state (DRIVER tab selected).
                    navController.popBackStack<HopRoutes.Home>(inclusive = false)
                },
                onNavigateToChat = {
                    // Already on MyTripsDriver — user can tap a trip row to reach chat.
                },
                onNavigateToProfile = {
                    navController.navigate(HopRoutes.Profile(userId = ""))
                },
            )
        }

        composable<HopRoutes.TripDetailActiveDriver> { backStackEntry ->
            val route: HopRoutes.TripDetailActiveDriver = backStackEntry.toRoute()
            TripDetailActiveDriverRoute(
                tripId = route.tripId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToMarkTripComplete = { tripId, driverNetOere ->
                    navController.navigate(HopRoutes.MarkTripComplete(tripId = tripId, driverNetOere = driverNetOere))
                },
                onNavigateToRatePassenger = { bookingId, passengerName, passengerInitials,
                                              remainingIds, remainingNames, remainingInitials ->
                    navController.navigate(
                        HopRoutes.RatePassenger(
                            bookingId = bookingId,
                            passengerName = passengerName,
                            passengerInitials = passengerInitials,
                            remainingBookingIds = remainingIds,
                            remainingPassengerNames = remainingNames,
                            remainingPassengerInitials = remainingInitials,
                        )
                    ) {
                        popUpTo(HopRoutes.TripDetailActiveDriver(tripId = route.tripId)) { inclusive = true }
                    }
                },
                onNavigateToChat = { bookingId ->
                    navController.navigate(HopRoutes.Chat(bookingId = bookingId))
                },
            )
        }

        composable<HopRoutes.MarkTripComplete> { backStackEntry ->
            val route: HopRoutes.MarkTripComplete = backStackEntry.toRoute()
            MarkTripCompleteRoute(
                tripId = route.tripId,
                driverNetOere = route.driverNetOere,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToRatePassenger = { bookingId, passengerName, passengerInitials,
                                              remainingIds, remainingNames, remainingInitials ->
                    navController.navigate(
                        HopRoutes.RatePassenger(
                            bookingId = bookingId,
                            passengerName = passengerName,
                            passengerInitials = passengerInitials,
                            remainingBookingIds = remainingIds,
                            remainingPassengerNames = remainingNames,
                            remainingPassengerInitials = remainingInitials,
                        )
                    ) {
                        popUpTo(HopRoutes.MyTripsDriver) { inclusive = false }
                    }
                },
                onNavigateToDriverSettlement = { tripId ->
                    navController.navigate(HopRoutes.DriverSettlement(tripId = tripId)) {
                        popUpTo<HopRoutes.MarkTripComplete> { inclusive = true }
                    }
                },
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsDriver) {
                        popUpTo(HopRoutes.MyTripsDriver) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.RatePassenger> { backStackEntry ->
            val route: HopRoutes.RatePassenger = backStackEntry.toRoute()
            RatePassengerRoute(
                bookingId = route.bookingId,
                passengerName = route.passengerName,
                passengerInitials = route.passengerInitials,
                onNavigateBack = {
                    navController.navigate(HopRoutes.MyTripsDriver) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
                onNavigateToDriverHome = {
                    if (route.remainingBookingIds.isNotEmpty()) {
                        navController.navigate(
                            HopRoutes.RatePassenger(
                                bookingId = route.remainingBookingIds.first(),
                                passengerName = route.remainingPassengerNames.firstOrNull() ?: "",
                                passengerInitials = route.remainingPassengerInitials.firstOrNull() ?: "",
                                remainingBookingIds = route.remainingBookingIds.drop(1),
                                remainingPassengerNames = route.remainingPassengerNames.drop(1),
                                remainingPassengerInitials = route.remainingPassengerInitials.drop(1),
                            )
                        ) {
                            popUpTo<HopRoutes.RatePassenger> { inclusive = true }
                        }
                    } else {
                        navController.popBackStack<HopRoutes.Home>(inclusive = false)
                    }
                },
            )
        }

        composable<HopRoutes.TaxDashboard> {
            TaxDashboardRoute(
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.TaxReportDownload> {
            TaxReportDownloadRoute(
                onNavigateBack = { navController.navigateUp() },
            )
        }

        // ── Shared ────────────────────────────────────────────────────────

        composable<HopRoutes.Profile> { backStackEntry ->
            val route: HopRoutes.Profile = backStackEntry.toRoute()
            OwnProfileRoute(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPhoneVerification = { /* phone already verified at registration */ },
                onNavigateToEditCar = {
                    navController.navigate(HopRoutes.EnableDriverStep1)
                },
                onNavigateToSettings = {
                    navController.navigate(HopRoutes.Settings)
                },
            )
        }

        composable<HopRoutes.OtherProfile> { backStackEntry ->
            val route: HopRoutes.OtherProfile = backStackEntry.toRoute()
            OtherProfileRoute(
                userId = route.userId,
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.Chat> { backStackEntry ->
            val route: HopRoutes.Chat = backStackEntry.toRoute()
            ChatRoute(
                bookingId      = route.bookingId,
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable<HopRoutes.Notifications> {
            NotificationsRoute(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSearch = { navController.navigate(HopRoutes.SearchResults()) },
            )
        }

        composable<HopRoutes.Settings> {
            SettingsRoute(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditProfile = { navController.navigate(HopRoutes.Profile(userId = "")) },
                onNavigateToChangePassword = { navController.navigate(HopRoutes.ForgotPassword) },
                onNavigateToHelpCentre = { uriHandler.openUri("https://ridly.dk/help") },
                onNavigateToContactUs = { uriHandler.openUri("mailto:support@ridly.dk") },
                onNavigateToTermsOfService = { uriHandler.openUri("https://ridly.dk/terms") },
                onNavigateToPrivacyPolicy = { uriHandler.openUri("https://ridly.dk/privacy") },
                onLogout = onLogout,
            )
        }

        // ── Settlement ────────────────────────────────────────────────────

        composable<HopRoutes.PassengerSettlement> { backStackEntry ->
            val route: HopRoutes.PassengerSettlement = backStackEntry.toRoute()
            PassengerSettlementRoute(
                bookingId = route.bookingId,
                onNavigateBack = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
                onSettlementComplete = {
                    navController.navigate(HopRoutes.RateDriver(bookingId = route.bookingId)) {
                        popUpTo(HopRoutes.PassengerSettlement(bookingId = route.bookingId)) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.DriverSettlement> { backStackEntry ->
            val route: HopRoutes.DriverSettlement = backStackEntry.toRoute()
            DriverSettlementRoute(
                tripId = route.tripId,
                onNavigateBack = { navController.navigateUp() },
                onSettlementComplete = {
                    navController.navigate(HopRoutes.MyTripsDriver) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
            )
        }

        composable<HopRoutes.DriverSettlementByBooking> { backStackEntry ->
            val route: HopRoutes.DriverSettlementByBooking = backStackEntry.toRoute()
            DriverSettlementRoute(
                tripId = "",
                bookingIdForResolution = route.bookingId,
                onNavigateBack = { navController.navigateUp() },
                onSettlementComplete = {
                    navController.navigate(HopRoutes.MyTripsDriver) {
                        popUpTo(HopRoutes.Home) { inclusive = false }
                    }
                },
            )
        }

        composable<HopRoutes.PastTripDetailDriver> { backStackEntry ->
            val route: HopRoutes.PastTripDetailDriver = backStackEntry.toRoute()
            PastTripDetailDriverRoute(
                tripId = route.tripId,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }

    NoInternetBanner(
        isVisible = !isConnected,
        modifier = Modifier.align(Alignment.TopCenter),
    )
    }
}

/** Temporary stub shown until a real screen composable is implemented. */
@Composable
private fun TodoScreen(name: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "TODO: $name",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

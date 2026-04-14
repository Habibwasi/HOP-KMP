package com.example.hop.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.hop.ui.screens.auth.LoginRoute
import com.example.hop.ui.screens.auth.OnboardingScreen
import com.example.hop.ui.screens.auth.OtpVerificationRoute
import com.example.hop.ui.screens.auth.SignUpRoute
import com.example.hop.ui.screens.passenger.BookingConfirmationRoute
import com.example.hop.ui.screens.passenger.BookingSuccessRoute
import com.example.hop.ui.screens.passenger.CancellationConfirmationRoute
import com.example.hop.ui.screens.passenger.MobilePayHandoffRoute
import com.example.hop.ui.screens.passenger.MyTripsPassengerRoute
import com.example.hop.ui.screens.passenger.PassengerHomeRoute
import com.example.hop.ui.screens.passenger.SearchResultsRoute
import com.example.hop.ui.screens.passenger.TripDetailActiveRoute
import com.example.hop.ui.screens.passenger.RateDriverRoute

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
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HopRoutes.Splash,
        modifier = modifier,
    ) {

        // ── Onboarding ────────────────────────────────────────────────────

        composable<HopRoutes.Splash> {
            OnboardingScreen(
                onNavigateToSignUp = { navController.navigate(HopRoutes.SignUp) },
                onNavigateToLogin  = { navController.navigate(HopRoutes.Login) },
            )
        }

        composable<HopRoutes.SignUp> {
            SignUpRoute(
                onNavigateToOtpVerification = { phone ->
                    navController.navigate(HopRoutes.OtpVerification(phone = phone))
                },
                onNavigateToLogin = {
                    navController.navigate(HopRoutes.Login)
                },
            )
        }

        composable<HopRoutes.Login> {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(HopRoutes.PassengerHome) {
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
            // TODO: Replace with ForgotPasswordScreen composable (ON-03b)
            TodoScreen("Forgot Password")
        }

        composable<HopRoutes.OtpVerification> { backStackEntry ->
            val route: HopRoutes.OtpVerification = backStackEntry.toRoute()
            OtpVerificationRoute(
                phone = route.phone,
                onNavigateToHome = {
                    navController.navigate(HopRoutes.PassengerHome) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        // ── Passenger ─────────────────────────────────────────────────────

        composable<HopRoutes.PassengerHome> {
            PassengerHomeRoute(
                onNavigateToSearchResults = {
                    navController.navigate(HopRoutes.SearchResults)
                },
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger)
                },
                onNavigateToChat = {
                    // TODO: Replace with ChatScreen navigation (post-MVP)
                },
                onNavigateToProfile = {
                    // TODO: Replace with ProfileScreen navigation
                },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(HopRoutes.TripDetail(id = tripId))
                },
                onNavigateToDriverHome = {
                    navController.navigate(HopRoutes.DriverHome)
                },
            )
        }

        composable<HopRoutes.SearchResults> {
            SearchResultsRoute(
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
                onNavigateToMobilePayHandoff = { bookingId ->
                    navController.navigate(HopRoutes.MobilePayHandoff(bookingId = bookingId))
                },
            )
        }

        composable<HopRoutes.MobilePayHandoff> { backStackEntry ->
            val route: HopRoutes.MobilePayHandoff = backStackEntry.toRoute()
            MobilePayHandoffRoute(
                bookingId = route.bookingId,
                onNavigateToSuccess = { bookingId ->
                    navController.navigate(HopRoutes.BookingSuccess(bookingId = bookingId)) {
                        popUpTo(HopRoutes.BookingConfirmation(tripId = "")) { inclusive = true }
                    }
                },
                onNavigateBackToBookingConfirmation = {
                    navController.navigateUp()
                },
            )
        }

        composable<HopRoutes.BookingSuccess> { backStackEntry ->
            val route: HopRoutes.BookingSuccess = backStackEntry.toRoute()
            BookingSuccessRoute(
                bookingId = route.bookingId,
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.PassengerHome) { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(HopRoutes.PassengerHome) {
                        popUpTo(HopRoutes.PassengerHome) { inclusive = true }
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
                onNavigateToHome = {
                    navController.navigate(HopRoutes.PassengerHome) {
                        popUpTo(HopRoutes.PassengerHome) { inclusive = false }
                    }
                },
                onNavigateToChat = {
                    // TODO: Replace with ChatScreen navigation (post-MVP)
                },
                onNavigateToProfile = {
                    // TODO: Replace with ProfileScreen navigation
                },
                onNavigateToFindRide = {
                    navController.navigate(HopRoutes.PassengerHome) {
                        popUpTo(HopRoutes.PassengerHome) { inclusive = false }
                    }
                },
            )
        }

        composable<HopRoutes.TripDetailActive> { backStackEntry ->
            val route: HopRoutes.TripDetailActive = backStackEntry.toRoute()
            TripDetailActiveRoute(
                bookingId = route.bookingId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToChat = {
                    // TODO: Replace with ChatScreen navigation (post-MVP)
                },
                onNavigateToCancellationConfirmation = { bookingId ->
                    navController.navigate(HopRoutes.CancellationConfirmation(bookingId = bookingId)) {
                        popUpTo(HopRoutes.TripDetailActive(bookingId = bookingId)) { inclusive = true }
                    }
                },
            )
        }

        composable<HopRoutes.RateDriver> { backStackEntry ->
            val route: HopRoutes.RateDriver = backStackEntry.toRoute()
            RateDriverRoute(
                bookingId = route.bookingId,
                driverName = route.driverName,
                driverInitials = route.driverInitials,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToMyTrips = {
                    navController.navigate(HopRoutes.MyTripsPassenger) {
                        popUpTo(HopRoutes.MyTripsPassenger) { inclusive = true }
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

        composable<HopRoutes.DriverHome> {
            // TODO: Replace with DriverHomeScreen composable (DR-01)
            TodoScreen("Driver Home")
        }

        composable<HopRoutes.EnableDriverStep1> {
            // TODO: Replace with CarDetailsScreen composable (DR-02)
            TodoScreen("Enable Driver — Step 1: Car Details")
        }

        composable<HopRoutes.EnableDriverStep2> {
            // TODO: Replace with LicenceUploadScreen composable (DR-03)
            TodoScreen("Enable Driver — Step 2: Licence Upload")
        }

        composable<HopRoutes.EnableDriverStep3> {
            // TODO: Replace with ReviewPendingScreen composable (DR-04)
            TodoScreen("Enable Driver — Step 3: Review Pending")
        }

        composable<HopRoutes.PostTripModelSelect> {
            // TODO: Replace with PostTripModelSelectScreen composable (DR-05)
            TodoScreen("Post Trip — Select Model")
        }

        composable<HopRoutes.PostTripModelA> {
            // TODO: Replace with PostTripModelAScreen composable (DR-06)
            TodoScreen("Post Trip — Model A (Daily Commute)")
        }

        composable<HopRoutes.PostTripModelB> {
            // TODO: Replace with PostTripModelBScreen composable (DR-07)
            TodoScreen("Post Trip — Model B (One-Off)")
        }

        composable<HopRoutes.PriceReview> {
            // TODO: Replace with PriceReviewScreen composable (DR-08)
            TodoScreen("Price Review & Confirm")
        }

        composable<HopRoutes.MyTripsDriver> {
            // TODO: Replace with MyTripsDriverScreen composable (DR-09)
            TodoScreen("My Trips (Driver)")
        }

        composable<HopRoutes.TripDetailActiveDriver> { backStackEntry ->
            val route: HopRoutes.TripDetailActiveDriver = backStackEntry.toRoute()
            // TODO: Replace with TripDetailActiveDriverScreen composable (DR-10)
            TodoScreen("Active Trip (Driver) — ${route.tripId}")
        }

        composable<HopRoutes.MarkTripComplete> { backStackEntry ->
            val route: HopRoutes.MarkTripComplete = backStackEntry.toRoute()
            // TODO: Replace with MarkTripCompleteScreen composable (DR-11)
            TodoScreen("Mark Trip Complete — ${route.tripId}")
        }

        composable<HopRoutes.RatePassenger> { backStackEntry ->
            val route: HopRoutes.RatePassenger = backStackEntry.toRoute()
            // TODO: Replace with RatePassengerScreen composable (DR-12)
            TodoScreen("Rate Passenger — ${route.bookingId}")
        }

        composable<HopRoutes.TaxDashboard> {
            // TODO: Replace with TaxDashboardScreen composable (DR-13)
            TodoScreen("Tax Dashboard")
        }

        composable<HopRoutes.TaxReportDownload> {
            // TODO: Replace with TaxReportDownloadScreen composable (DR-14)
            TodoScreen("Annual Tax Report Download")
        }

        // ── Shared ────────────────────────────────────────────────────────

        composable<HopRoutes.Profile> { backStackEntry ->
            val route: HopRoutes.Profile = backStackEntry.toRoute()
            // TODO: Replace with ProfileScreen composable (SH-02)
            TodoScreen("Profile — ${route.userId}")
        }

        composable<HopRoutes.OtherProfile> { backStackEntry ->
            val route: HopRoutes.OtherProfile = backStackEntry.toRoute()
            // TODO: Replace with OtherProfileScreen composable (SH-03).
            // Demo-safe: avatar tap from TripDetailScreen navigates here; stub renders a
            // labelled placeholder screen rather than crashing.
            TodoScreen("Other Profile — ${route.userId}")
        }

        composable<HopRoutes.Chat> { backStackEntry ->
            val route: HopRoutes.Chat = backStackEntry.toRoute()
            // TODO: Replace with ChatScreen composable (SH-04)
            TodoScreen("Chat — ${route.bookingId}")
        }

        composable<HopRoutes.Notifications> {
            // TODO: Replace with NotificationsScreen composable (SH-05)
            TodoScreen("Notifications")
        }

        composable<HopRoutes.Settings> {
            // TODO: Replace with SettingsScreen composable (SH-06)
            TodoScreen("Settings")
        }
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

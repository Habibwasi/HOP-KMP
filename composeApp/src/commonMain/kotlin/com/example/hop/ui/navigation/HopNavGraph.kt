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
import com.example.hop.ui.screens.auth.OnboardingScreen

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
            // TODO: Replace with SignUpScreen composable (ON-02)
            TodoScreen("Sign Up")
        }

        composable<HopRoutes.Login> {
            // TODO: Replace with LoginScreen composable (ON-03)
            TodoScreen("Log In")
        }

        composable<HopRoutes.OtpVerification> { backStackEntry ->
            val route: HopRoutes.OtpVerification = backStackEntry.toRoute()
            // TODO: Replace with OtpVerificationScreen composable (ON-04)
            TodoScreen("OTP Verification — ${route.phone}")
        }

        // ── Passenger ─────────────────────────────────────────────────────

        composable<HopRoutes.PassengerHome> {
            // TODO: Replace with PassengerHomeScreen composable (PA-01)
            TodoScreen("Passenger Home")
        }

        composable<HopRoutes.SearchResults> {
            // TODO: Replace with SearchResultsScreen composable (PA-02)
            TodoScreen("Search Results")
        }

        composable<HopRoutes.TripDetail> { backStackEntry ->
            val route: HopRoutes.TripDetail = backStackEntry.toRoute()
            // TODO: Replace with TripDetailScreen composable (PA-03)
            TodoScreen("Trip Detail — ${route.id}")
        }

        composable<HopRoutes.BookingConfirmation> { backStackEntry ->
            val route: HopRoutes.BookingConfirmation = backStackEntry.toRoute()
            // TODO: Replace with BookingConfirmationScreen composable (PA-04)
            TodoScreen("Booking Confirmation — ${route.tripId}")
        }

        composable<HopRoutes.MobilePayHandoff> { backStackEntry ->
            val route: HopRoutes.MobilePayHandoff = backStackEntry.toRoute()
            // TODO: Replace with MobilePayHandoffScreen composable (PA-05)
            TodoScreen("MobilePay Handoff — ${route.bookingId}")
        }

        composable<HopRoutes.BookingSuccess> { backStackEntry ->
            val route: HopRoutes.BookingSuccess = backStackEntry.toRoute()
            // TODO: Replace with BookingSuccessScreen composable (PA-06)
            TodoScreen("Booking Success — ${route.bookingId}")
        }

        composable<HopRoutes.MyTripsPassenger> {
            // TODO: Replace with MyTripsPassengerScreen composable (PA-07)
            TodoScreen("My Trips (Passenger)")
        }

        composable<HopRoutes.TripDetailActive> { backStackEntry ->
            val route: HopRoutes.TripDetailActive = backStackEntry.toRoute()
            // TODO: Replace with TripDetailActiveScreen composable (PA-08)
            TodoScreen("Active Trip — ${route.bookingId}")
        }

        composable<HopRoutes.RateDriver> { backStackEntry ->
            val route: HopRoutes.RateDriver = backStackEntry.toRoute()
            // TODO: Replace with RateDriverScreen composable (PA-09)
            TodoScreen("Rate Driver — ${route.bookingId}")
        }

        composable<HopRoutes.CancellationConfirmation> { backStackEntry ->
            val route: HopRoutes.CancellationConfirmation = backStackEntry.toRoute()
            // TODO: Replace with CancellationConfirmationScreen composable (PA-10)
            TodoScreen("Cancellation Confirmation — ${route.bookingId}")
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
            // TODO: Replace with OtherProfileScreen composable (SH-03)
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

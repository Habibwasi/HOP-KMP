package com.example.hop.ui.navigation

import kotlinx.serialization.Serializable

/**
 * All 33 screen routes in the Hop app.
 *
 * Each leaf is @Serializable so Navigation Compose can build the back-stack
 * entry and restore instances after process death.
 *
 * Route groups match the screen inventory in Agent.md:
 *   Onboarding (4), Passenger (10), Driver (14), Shared (5)
 */
@Serializable
sealed interface HopRoutes {

    // ── Onboarding ─────────────────────────────────────────────────────────

    /** ON-00 — Animated brand splash (3 s) */
    @Serializable
    data object Splash : HopRoutes

    /** ON-01 — Onboarding / marketing screen with Sign-up & Login CTAs */
    @Serializable
    data object Onboarding : HopRoutes

    /** ON-02 */
    @Serializable
    data object SignUp : HopRoutes

    /** ON-03 */
    @Serializable
    data object Login : HopRoutes

    /** ON-03b — Password Reset (stub) */
    @Serializable
    data object ForgotPassword : HopRoutes

    // ── Shared Home ────────────────────────────────────────────────────────

    /** SH-01 — Unified home screen (passenger + driver via role toggle) */
    @Serializable
    data object Home : HopRoutes

    // ── Passenger ──────────────────────────────────────────────────────────

    /** PA-02 */
    @Serializable
    data object SearchResults : HopRoutes

    /** PA-03 */
    @Serializable
    data class TripDetail(val id: String) : HopRoutes

    /** PA-04 */
    @Serializable
    data class BookingConfirmation(val tripId: String) : HopRoutes

    /** PA-05 */
    @Serializable
    data class MobilePayHandoff(val bookingId: String) : HopRoutes

    /** PA-06 */
    @Serializable
    data class BookingSuccess(val bookingId: String) : HopRoutes

    /** PA-07 */
    @Serializable
    data object MyTripsPassenger : HopRoutes

    /** PA-08 */
    @Serializable
    data class TripDetailActive(val bookingId: String) : HopRoutes

    /** PA-09 */
    @Serializable
    data class RateDriver(
        val bookingId: String,
        val driverName: String,
        val driverInitials: String,
    ) : HopRoutes

    /** PA-10 */
    @Serializable
    data class CancellationConfirmation(val bookingId: String) : HopRoutes

    // ── Driver ─────────────────────────────────────────────────────────────

    /** DR-02 Car Details */
    @Serializable
    data object EnableDriverStep1 : HopRoutes

    /** DR-03 Licence Upload */
    @Serializable
    data object EnableDriverStep2 : HopRoutes

    /** DR-04 Review Pending */
    @Serializable
    data object EnableDriverStep3 : HopRoutes

    /** DR-05 */
    @Serializable
    data object PostTripModelSelect : HopRoutes

    /** DR-06 */
    @Serializable
    data object PostTripModelA : HopRoutes

    /** DR-07 */
    @Serializable
    data object PostTripModelB : HopRoutes

    /** DR-08 */
    @Serializable
    data object PriceReview : HopRoutes

    /** DR-09 */
    @Serializable
    data object MyTripsDriver : HopRoutes

    /** DR-10 */
    @Serializable
    data class TripDetailActiveDriver(val tripId: String) : HopRoutes

    /** DR-11 */
    @Serializable
    data class MarkTripComplete(val tripId: String, val driverNetOere: Int) : HopRoutes

    /** DR-12 */
    @Serializable
    data class RatePassenger(
        val bookingId: String,
        val passengerName: String,
        val passengerInitials: String,
    ) : HopRoutes

    /** DR-13 */
    @Serializable
    data object TaxDashboard : HopRoutes

    /** DR-14 */
    @Serializable
    data object TaxReportDownload : HopRoutes

    // ── Shared ─────────────────────────────────────────────────────────────

    /** SH-02 Own Profile */
    @Serializable
    data class Profile(val userId: String) : HopRoutes

    /** SH-03 Other Profile */
    @Serializable
    data class OtherProfile(val userId: String) : HopRoutes

    /** SH-04 In-App Chat */
    @Serializable
    data class Chat(val bookingId: String) : HopRoutes

    /** SH-05 */
    @Serializable
    data object Notifications : HopRoutes

    /** SH-06 */
    @Serializable
    data object Settings : HopRoutes
}

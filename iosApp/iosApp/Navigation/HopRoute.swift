import Foundation

/// Mirror of the Android `HopRoutes` sealed interface.
///
/// All 33 mobile destinations. Admin screens (AD-01…AD-05) are web-only and omitted.
/// Conforms to `Hashable` so it can be used as a `NavigationStack` path element.
///
/// Screen inventory (Agent.md):
///   Onboarding (5) · Passenger (10) · Driver (14) · Shared (5) = 34 screens
///   Note: PA-01/DR-01/SH-01 share the single `.home` case → 33 enum cases.
enum HopRoute: Hashable {

    // MARK: — Onboarding ──────────────────────────────────────────────────────

    /// ON-01 Onboarding
    case onboarding

    /// ON-02 Sign Up
    case signUp

    /// ON-03 Log In
    case login

    /// ON-04 Phone OTP Verification
    case otpVerification(phone: String)

    /// ON-03b Password Reset (stub – post-MVP UI)
    case forgotPassword

    // MARK: — Shared Home ─────────────────────────────────────────────────────

    /// SH-01 / PA-01 / DR-01  Unified home with role-toggle pill
    case home

    // MARK: — Passenger ───────────────────────────────────────────────────────

    /// PA-02 Search Results
    case searchResults

    /// PA-03 Trip Detail
    case tripDetail(id: String)

    /// PA-04 Booking Confirmation
    case bookingConfirmation(tripId: String)

    /// PA-05 MobilePay Handoff
    case mobilePayHandoff(bookingId: String)

    /// PA-06 Booking Success
    case bookingSuccess(bookingId: String)

    /// PA-07 My Trips (Passenger)
    case myTripsPassenger

    /// PA-08 Trip Detail Active (Passenger)
    case tripDetailActive(bookingId: String)

    /// PA-09 Rate Driver
    case rateDriver(bookingId: String)

    /// PA-10 Cancellation Confirmation
    case cancellationConfirmation(bookingId: String)

    // MARK: — Driver ──────────────────────────────────────────────────────────

    /// DR-02 Car Details (Enable Driver) — single-step onboarding.
    /// Licence verification was dropped to match Android: completing this
    /// screen flips the user's role to DRIVER and returns home.
    case enableDriverStep1

    /// DR-03 MobilePay Number (replaces defunct licence-upload step)
    case enableDriverStep2

    /// DR-05 Post Trip – Model Select
    case postTripModelSelect

    /// DR-06 Post Trip – Model A (Daily Commute)
    case postTripModelA

    /// DR-07 Post Trip – Model B (One-Off Long Distance)
    case postTripModelB

    /// DR-08 Price Review & Confirm
    case priceReview

    /// DR-09 My Trips (Driver)
    case myTripsDriver

    /// DR-10 Trip Detail Active (Driver)
    case tripDetailActiveDriver(tripId: String)

    /// DR-11 Mark Trip Complete
    case markTripComplete(tripId: String, driverNetOere: Int)

    /// DR-12 Rate Passenger
    case ratePassenger(
        bookingId: String,
        passengerName: String,
        passengerInitials: String,
        passengerAvatarUrl: String? = nil,
        remainingBookingIds: [String] = [],
        remainingPassengerNames: [String] = [],
        remainingPassengerInitials: [String] = [],
        remainingPassengerAvatarUrls: [String?] = []
    )

    /// DR-13 Tax Dashboard
    case taxDashboard

    /// DR-14 Annual Tax Report Download
    case taxReportDownload

    // MARK: — Shared ──────────────────────────────────────────────────────────

    /// SH-02 Own Profile
    case profile(userId: String)

    /// SH-03 Other User's Profile
    case otherProfile(userId: String)

    /// SH-04 In-App Chat (per booking)
    case chat(bookingId: String)

    /// SH-04b Chat List — all chats the user has had or is having
    case chatList

    /// SH-05 Notifications
    case notifications

    /// SH-06 Settings
    case settings

    // MARK: — Settlement ──────────────────────────────────────────────────────

    /// SE-01 Passenger Settlement (pay driver via MobilePay)
    case passengerSettlement(bookingId: String)

    /// SE-02 Driver Settlement (all passengers for a trip)
    case driverSettlement(tripId: String)

    /// SE-02b Driver Settlement entry via notification deep-link (resolves tripId from bookingId)
    case driverSettlementByBooking(bookingId: String)

    /// DR-09b Past trip detail (driver read-only view)
    case pastTripDetailDriver(tripId: String)
}

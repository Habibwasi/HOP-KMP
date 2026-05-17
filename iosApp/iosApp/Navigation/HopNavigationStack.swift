import SwiftUI
import Shared

// MARK: — HopNavigationStack ──────────────────────────────────────────────────

/// Authenticated app root.
///
/// `HopTabView` sits at the base of the stack.  Detail screens are pushed on
/// top via `navigationDestination(for: HopRoute.self)`, hiding the tab bar for
/// focused flows (trip detail, booking, rating, etc.).
///
/// The tab bar re-appears automatically when the user pops back to the root.
///
/// **How to navigate from a screen:**
/// ```swift
/// // Inside any view that received `navigate`:
/// navigate(.tripDetail(id: trip.id))
/// ```
struct HopNavigationStack: View {

    @State private var path: [HopRoute] = []

    // Search params forwarded from PassengerHomeView.onSearch into SearchResultsView
    @State private var lastSearchOrigin: String = ""
    @State private var lastSearchDest:   String = ""
    @State private var lastSearchDate:   String = ""
    @State private var lastSearchSeats:  Int    = 1

    private func navigate(_ route: HopRoute) { path.append(route) }
    private func popBack() { if !path.isEmpty { path.removeLast() } }
    private func goHome() { path.removeAll() }

    var body: some View {
        NavigationStack(path: $path) {
            HopTabView { route in
                path.append(route)
            } onSearch: { origin, dest, date, seats in
                lastSearchOrigin = origin
                lastSearchDest   = dest
                lastSearchDate   = date   // String ISO date from PassengerHomeView
                lastSearchSeats  = seats
                path.append(.searchResults)
            } onLogout: {
                // Logout flips back to auth via AuthEffectNavigateToLogin in ContentView
            }
            .navigationDestination(for: HopRoute.self) { route in
                destinationView(for: route)
                    // All detail screens render their own custom top bar
                    // (HStack with arrow.left + title). Hide the system
                    // NavigationStack toolbar so we don't get a duplicate
                    // navigation bar stacked above each screen's chrome.
                    .toolbar(.hidden, for: .navigationBar)
            }
        }
    }

    // MARK: — Destination routing ─────────────────────────────────────────────

    /// Returns the SwiftUI view for each `HopRoute`.
    ///
    /// Screens not yet implemented render `HopUnbuiltScreen` so the nav graph
    /// compiles end-to-end today.  Replace each `HopUnbuiltScreen` call with
    /// the real screen view as screens are built.
    @ViewBuilder
    private func destinationView(for route: HopRoute) -> some View {
        switch route {

        // ── Onboarding (post-auth pushes are unexpected; guard for safety) ──
        case .onboarding:
            HopUnbuiltScreen(route: "ON-01 Onboarding")
        case .signUp:
            HopUnbuiltScreen(route: "ON-02 Sign Up")
        case .login:
            HopUnbuiltScreen(route: "ON-03 Log In")
        case .otpVerification(let phone):
            HopUnbuiltScreen(route: "ON-04 OTP Verification (\(phone))")
        case .forgotPassword:
            HopUnbuiltScreen(route: "ON-03b Forgot Password")

        // ── Shared Home (tab root — should not be pushed directly) ──────────
        case .home:
            HopUnbuiltScreen(route: "SH-01 Home")

        // ── Passenger ────────────────────────────────────────────────────────
        case .searchResults:
            // Trigger the search via the shared SearchViewModel before pushing.
            // Pass the captured params so the SearchResultsView's own VM
            // instance can re-issue the search (Koin registers SearchViewModel
            // as factory, so each call site gets a fresh instance).
            SearchResultsView(
                onBack: popBack,
                onNavigateToTripDetail: { id in navigate(.tripDetail(id: id)) },
                origin: lastSearchOrigin,
                dest:   lastSearchDest,
                date:   lastSearchDate,
                seats:  lastSearchSeats
            )

        case .tripDetail(let id):
            TripDetailView(
                tripId: id,
                onBack: popBack,
                onNavigateToOtherProfile: { driverId in navigate(.otherProfile(userId: driverId)) },
                onNavigateToBookingConfirmation: { tripId in navigate(.bookingConfirmation(tripId: tripId)) }
            )

        case .bookingConfirmation(let tripId):
            BookingConfirmationView(
                tripId: tripId,
                onBack: popBack,
                onNavigateToSuccess: { bookingId in
                    navigate(.bookingSuccess(bookingId: bookingId))
                }
            )

        case .mobilePayHandoff(let bookingId):
            PassengerSettlementView(
                bookingId: bookingId,
                onBack: popBack,
                onSettlementComplete: { navigate(.myTripsPassenger) }
            )

        case .bookingSuccess(let bookingId):
            BookingSuccessView(
                bookingId: bookingId,
                onViewMyTrips: { navigate(.myTripsPassenger) },
                onBackToHome: goHome
            )

        case .myTripsPassenger:
            MyTripsPassengerView(
                onNavigateBack: popBack,
                onNavigateToTripDetailActive: { bookingId in navigate(.tripDetailActive(bookingId: bookingId)) },
                onNavigateToTripDetail: { tripId in navigate(.tripDetail(id: tripId)) },
                onNavigateToPassengerSettlement: { bookingId in navigate(.passengerSettlement(bookingId: bookingId)) },
                onNavigateToHome: goHome,
                onNavigateToChat: { /* handled by tab bar */ },
                onNavigateToProfile: { /* tab-bar Profile */ },
                onNavigateToFindRide: goHome
            )

        case .tripDetailActive(let bookingId):
            TripDetailActiveView(
                bookingId: bookingId,
                onBack: popBack,
                onNavigateToChat: { bid in navigate(.chat(bookingId: bid)) },
                onNavigateToCancellationConfirmation: { bid in navigate(.cancellationConfirmation(bookingId: bid)) },
                onNavigateToPassengerSettlement: { bid in navigate(.passengerSettlement(bookingId: bid)) }
            )

        case .rateDriver(let bookingId, let driverName, let driverInitials):
            RateDriverView(
                bookingId:      bookingId,
                driverName:     driverName,
                driverInitials: driverInitials,
                onSubmitted: { navigate(.myTripsPassenger) },
                onBack: popBack
            )

        case .cancellationConfirmation(let bookingId):
            CancellationConfirmationView(
                bookingId: bookingId,
                onBackToMyTrips: { navigate(.myTripsPassenger) }
            )

        // ── Driver ────────────────────────────────────────────────────────────
        case .enableDriverStep1:
            // Single-step onboarding (matches Android): saving car details
            // fires DriverEffect.NavigateToHome, which the view forwards as
            // `onNavigateNext` — navigate to step 2 (MobilePay) so the new
            // driver sets their number before their first ride.
            CarDetailsView(
                onNavigateBack: popBack,
                onNavigateNext: {
                    KoinIOSKt.getAuthViewModel().onEvent(event: AuthEventRefreshProfile.shared)
                    navigate(.enableDriverStep2)
                }
            )

        case .enableDriverStep2:
            MobilepayOnboardingView(
                onNavigateBack: popBack,
                onNavigateNext: goHome
            )

        case .postTripModelSelect:
            PostTripModelSelectView(
                onNavigateToModelA: { navigate(.postTripModelA) },
                onNavigateToModelB: { navigate(.postTripModelB) },
                onBack: popBack
            )

        case .postTripModelA:
            PostTripModelAView(
                onNavigateToReview: { navigate(.priceReview) },
                onBack: popBack
            )

        case .postTripModelB:
            PostTripModelBView(
                onNavigateToReview: { navigate(.priceReview) },
                onBack: popBack
            )

        case .priceReview:
            PriceReviewView(
                onNavigateToMyTrips: { navigate(.myTripsDriver) },
                onBack: popBack
            )

        case .myTripsDriver:
            MyTripsDriverView(
                onTripTapped: { tid in navigate(.tripDetailActiveDriver(tripId: tid)) },
                onSettlementTapped: { tripId in navigate(.driverSettlement(tripId: tripId)) },
                onPastTripTapped: { tripId in navigate(.pastTripDetailDriver(tripId: tripId)) },
                onBack: popBack
            )

        case .tripDetailActiveDriver(let tripId):
            TripDetailActiveDriverView(
                tripId: tripId,
                onMarkComplete: { tid, net in navigate(.markTripComplete(tripId: tid, driverNetOere: net)) },
                onMessagePassenger: { bid in navigate(.chat(bookingId: bid)) },
                onBack: popBack
            )

        case .markTripComplete(let tripId, let driverNetOere):
            MarkTripCompleteView(
                tripId: tripId,
                driverNetOere: driverNetOere,
                onCompleted: { bookingId, name, initials in
                    navigate(.ratePassenger(bookingId: bookingId, passengerName: name, passengerInitials: initials))
                },
                onSettlementRequired: { tripId in
                    // Pop markTripComplete before pushing settlement so the driver
                    // cannot go back and re-complete the trip.
                    if !path.isEmpty { path.removeLast() }
                    navigate(.driverSettlement(tripId: tripId))
                },
                onBack: popBack
            )

        case .ratePassenger(let bookingId, let passengerName, let passengerInitials):
            RatePassengerView(
                bookingId:         bookingId,
                passengerName:     passengerName,
                passengerInitials: passengerInitials,
                onSubmitted:       { navigate(.myTripsDriver) },
                onBack:            popBack
            )

        case .taxDashboard:
            TaxDashboardView(
                onOpenAnnualReport: { navigate(.taxReportDownload) },
                onBack: popBack
            )

        case .taxReportDownload:
            TaxReportDownloadView(onBack: popBack)

        // ── Settlement ────────────────────────────────────────────────────────
        case .passengerSettlement(let bookingId):
            PassengerSettlementView(
                bookingId: bookingId,
                onBack: popBack,
                onSettlementComplete: { navigate(.myTripsPassenger) }
            )

        case .driverSettlement(let tripId):
            DriverSettlementView(
                tripId: tripId,
                onBack: popBack,
                onSettlementComplete: { navigate(.myTripsDriver) }
            )

        case .driverSettlementByBooking(let bookingId):
            DriverSettlementView(
                tripId: "",
                bookingIdForResolution: bookingId,
                onBack: popBack,
                onSettlementComplete: { navigate(.myTripsDriver) }
            )

        case .pastTripDetailDriver(let tripId):
            PastTripDetailDriverView(
                tripId: tripId,
                onBack: popBack
            )

        // ── Shared ────────────────────────────────────────────────────────────
        case .profile(_):
            OwnProfileView(
                navigate: navigate,
                onBack: popBack
            )

        case .otherProfile(let userId):
            OtherProfileView(
                userId: userId,
                onBack: popBack
            )

        case .chat(let bookingId):
            ChatView(
                bookingId: bookingId,
                onBack: popBack
            )

        case .notifications:
            NotificationsView(
                navigate: navigate,
                onBack: popBack
            )

        case .settings:
            SettingsView(
                onBack:      popBack,
                onLoggedOut: goHome
            )
        }
    }
}

// MARK: — Unbuilt screen placeholder ─────────────────────────────────────────

/// Full-screen placeholder for routes whose real views are not yet implemented.
/// Replace each usage with the real `View` as screens are built.
private struct HopUnbuiltScreen: View {
    let route: String

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: HopSpacing.md) {
                Image(systemName: "square.dashed")
                    .font(.system(size: 48, weight: .light))
                    .foregroundColor(Color.hopTextSecondary)

                Text(route)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, HopSpacing.lg)
            }
        }
        .navigationTitle(route)
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: — Previews ────────────────────────────────────────────────────────────

#Preview("Authenticated – tab root") {
    HopNavigationStack()
}

#Preview("Pushed detail screen") {
    // Simulate a pushed route so we can preview the unbuilt placeholder.
    NavigationStack {
        HopUnbuiltScreen(route: "PA-03 Trip Detail — abc123")
            .toolbarBackground(Color.hopSurface, for: .navigationBar)
            .toolbarColorScheme(.dark, for: .navigationBar)
    }
}

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
                    .toolbarBackground(Color.hopSurface, for: .navigationBar)
                    .toolbarColorScheme(.dark, for: .navigationBar)
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
            SearchResultsView(
                origin: lastSearchOrigin,
                destination: lastSearchDest,
                date: lastSearchDate,
                seats: lastSearchSeats,
                onTripTapped: { id in navigate(.tripDetail(id: id)) },
                onBack: popBack
            )

        case .tripDetail(let id):
            TripDetailView(
                tripId: id,
                onBook: { tripId in navigate(.bookingConfirmation(tripId: tripId)) },
                onBack: popBack
            )

        case .bookingConfirmation(let tripId):
            BookingConfirmationView(
                tripId: tripId,
                tripUi: nil,
                onPayWithMobilePay: { bookingId in
                    navigate(.mobilePayHandoff(bookingId: bookingId))
                },
                onBack: popBack
            )

        case .mobilePayHandoff(let bookingId):
            MobilePayHandoffView(
                bookingId: bookingId,
                redirectURL: "",
                onSuccess: { bid in navigate(.bookingSuccess(bookingId: bid)) },
                onBack: popBack
            )

        case .bookingSuccess(let bookingId):
            BookingSuccessView(
                bookingId: bookingId,
                onViewMyTrips: { navigate(.myTripsPassenger) },
                onGoHome: goHome
            )

        case .myTripsPassenger:
            MyTripsPassengerView(
                onTripTapped: { bookingId in navigate(.tripDetailActive(bookingId: bookingId)) },
                navigate: navigate
            )

        case .tripDetailActive(let bookingId):
            TripDetailActiveView(
                bookingId: bookingId,
                onMessageDriver: { bid in navigate(.chat(bookingId: bid)) },
                onCancelBooking: { bid in navigate(.cancellationConfirmation(bookingId: bid)) },
                onRateDriver:    { bid, name, initials in navigate(.rateDriver(bookingId: bid, driverName: name, driverInitials: initials)) },
                onBack: popBack
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
                onGoHome: goHome
            )

        // ── Driver ────────────────────────────────────────────────────────────
        case .enableDriverStep1:
            CarDetailsView(
                onNavigateBack: popBack,
                onNavigateNext: { navigate(.enableDriverStep2) }
            )

        case .enableDriverStep2:
            LicenceUploadView(
                onNavigateBack: popBack,
                onNavigateNext: { navigate(.enableDriverStep3) }
            )

        case .enableDriverStep3:
            ReviewPendingView(onNavigateToHome: goHome)

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
                onCompleted: { bookingId in
                    navigate(.ratePassenger(bookingId: bookingId, passengerName: "Passenger", passengerInitials: "P"))
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

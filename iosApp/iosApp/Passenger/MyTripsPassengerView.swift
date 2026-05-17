import SwiftUI
import Shared

/// PA-07 — My Trips (Passenger). Mirrors composeApp `MyTripsPassengerScreen.kt`.
/// Top bar + Upcoming/Past tabs + TripCardLight list + bottom nav (Home/MyTrips/Chat/Profile).
///
/// `inTab`: when `true`, this view is being rendered as a root tab inside
/// `HopTabView` — the system tab bar already provides bottom navigation and
/// the top-level nav is the tab itself, so we hide our own back arrow and
/// our custom `MyTripsBottomNavBar`. When `false` (pushed via
/// `HopNavigationStack`), the screen owns its full chrome.
struct MyTripsPassengerView: View {
    let onNavigateBack: () -> Void
    let onNavigateToTripDetailActive: (_ bookingId: String) -> Void
    let onNavigateToTripDetail: (_ tripId: String) -> Void
    let onNavigateToPassengerSettlement: (_ bookingId: String) -> Void
    let onNavigateToHome: () -> Void
    let onNavigateToChat: () -> Void
    let onNavigateToProfile: () -> Void
    let onNavigateToFindRide: () -> Void
    var inTab: Bool = false

    @StateObject private var wrapper = MyTripsPassengerViewModelWrapper()
    @State private var selectedTab: Int = 0

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            ZStack {
                Text("My Trips")
                    .font(HopFont.headlineSmall(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                if !inTab {
                    HStack {
                        Button(action: onNavigateBack) {
                            Image(systemName: "arrow.left")
                                .font(.system(size: 18, weight: .medium))
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .frame(width: 44, height: 44)
                        }.buttonStyle(.plain)
                        Spacer()
                    }
                }
            }
            .padding(.horizontal, HopSpacing.xs)

            // Tabs
            HStack(spacing: 0) {
                tabButton(title: "Upcoming", index: 0)
                tabButton(title: "Past", index: 1)
            }

            // Content
            if wrapper.state.isLoading {
                Spacer()
                ProgressView().tint(Color.hopPrimaryGreen)
                Spacer()
            } else if selectedTab == 0 {
                if wrapper.state.upcomingTrips.isEmpty {
                    EmptyStateLight(
                        systemImage: "car",
                        headline: "No upcoming trips",
                        subtitle: "Find a trip and book your first seat.",
                        ctaLabel: "Find a ride",
                        onCta: onNavigateToFindRide
                    )
                } else {
                    TripList(trips: wrapper.state.upcomingTrips, onTap: { trip in
                        let id = trip.bookingId ?? trip.id
                        wrapper.selectUpcoming(bookingId: id)
                    })
                }
            } else {
                if wrapper.state.pastTrips.isEmpty {
                    EmptyStateLight(
                        systemImage: "clock.arrow.circlepath",
                        headline: "No past trips",
                        subtitle: "Trips you've completed will appear here.",
                        ctaLabel: nil, onCta: {}
                    )
                } else {
                    TripList(trips: wrapper.state.pastTrips, onTap: { trip in
                        wrapper.selectPast(tripId: trip.id)
                    })
                }
            }

            // Bottom nav (only when shown as a pushed route — the system
            // TabView already provides bottom navigation when in-tab).
            if !inTab {
                MyTripsBottomNavBar(
                    selected: 1,
                    onHome: onNavigateToHome,
                    onMyTrips: {},
                    onChat: onNavigateToChat,
                    onProfile: onNavigateToProfile
                )
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            wrapper.startObserving { effect in
                if let n = effect as? MyTripsPassengerEffectNavigateToTripDetailActive {
                    onNavigateToTripDetailActive(n.bookingId)
                } else if let n = effect as? MyTripsPassengerEffectNavigateToPassengerSettlement {
                    onNavigateToPassengerSettlement(n.bookingId)
                } else if let n = effect as? MyTripsPassengerEffectNavigateToTripDetail {
                    onNavigateToTripDetail(n.tripId)
                }
            }
            wrapper.load()
        }
    }

    private func tabButton(title: String, index: Int) -> some View {
        let active = selectedTab == index
        return Button(action: { selectedTab = index }) {
            VStack(spacing: 6) {
                Text(title)
                    .font(HopFont.bodyMedium(weight: active ? .semibold : .regular))
                    .foregroundColor(active ? Color.hopPrimaryLime : Color.hopAuthTextSecondary)
                Rectangle()
                    .fill(active ? Color.hopPrimaryLime : Color.clear)
                    .frame(height: 2)
            }
            .padding(.vertical, HopSpacing.sm)
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

private struct TripList: View {
    let trips: [TripUiModel]
    let onTap: (TripUiModel) -> Void
    var body: some View {
        ScrollView {
            VStack(spacing: HopSpacing.md) {
                ForEach(trips, id: \.id) { trip in
                    TripCardLight(
                        driverName: "Driver",
                        driverInitials: "D",
                        driverRating: 4.8,
                        originName: trip.originName,
                        destinationName: trip.destName,
                        departureTime: trip.departsAt,
                        badgeStatus: badgeFor(trip),
                        pricePerSeatOere: Int(trip.priceOerePerSeat)
                    ) { onTap(trip) }
                }
            }
            .padding(HopSpacing.md)
        }
    }
    private func badgeFor(_ trip: TripUiModel) -> HopBadgeStatus {
        switch trip.status {
        case .confirmed: return .confirmed
        case .active: return .confirmed
        case .completed: return .confirmed
        case .cancelled: return .cancelled
        default:
            return trip.model == .a ? .modelA : .modelB
        }
    }
}

private struct EmptyStateLight: View {
    let systemImage: String
    let headline: String
    let subtitle: String?
    let ctaLabel: String?
    let onCta: () -> Void

    var body: some View {
        VStack(spacing: HopSpacing.lg) {
            Spacer()
            ZStack {
                Circle().fill(Color.hopCardSurfaceMuted).frame(width: 120, height: 120)
                Image(systemName: systemImage)
                    .font(.system(size: 44))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            Text(headline)
                .font(HopFont.headlineSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)
            if let subtitle {
                Text(subtitle)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, HopSpacing.xl)
            }
            if let ctaLabel {
                HopButton(text: ctaLabel, variant: .primary, action: onCta)
                    .frame(maxWidth: 240)
            }
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct MyTripsBottomNavBar: View {
    /// 0=Home, 1=MyTrips, 2=Chat, 3=Profile
    let selected: Int
    let onHome: () -> Void
    let onMyTrips: () -> Void
    let onChat: () -> Void
    let onProfile: () -> Void

    var body: some View {
        HStack {
            tab(systemImage: "house.fill", label: "Home", index: 0, action: onHome)
            tab(systemImage: "car.fill", label: "Trips", index: 1, action: onMyTrips)
            tab(systemImage: "message.fill", label: "Chat", index: 2, action: onChat)
            tab(systemImage: "person.fill", label: "Profile", index: 3, action: onProfile)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.top, 6)
        .padding(.bottom, 4)
        .background(Color.hopBackground)
        .overlay(Rectangle().fill(Color.hopCardBorder).frame(height: 1), alignment: .top)
    }
    private func tab(systemImage: String, label: String, index: Int, action: @escaping () -> Void) -> some View {
        let active = selected == index
        return Button(action: action) {
            VStack(spacing: 2) {
                Image(systemName: systemImage)
                    .font(.system(size: 20))
                    .foregroundColor(active ? Color.hopPrimaryLime : Color.hopAuthTextSecondary)
                Text(label)
                    .font(HopFont.labelSmall(weight: active ? .semibold : .medium))
                    .foregroundColor(active ? Color.hopPrimaryLime : Color.hopAuthTextSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
        }
        .buttonStyle(.plain)
    }
}

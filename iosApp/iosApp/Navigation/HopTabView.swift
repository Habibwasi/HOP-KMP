import SwiftUI

// MARK: — Tab identity ────────────────────────────────────────────────────────

/// The four persistent bottom-navigation tabs.
enum HopTab: Int, CaseIterable, Hashable {
    case home
    case myTrips
    case chat
    case profile

    var label: String {
        switch self {
        case .home:    return "Home"
        case .myTrips: return "My Trips"
        case .chat:    return "Chat"
        case .profile: return "Profile"
        }
    }

    /// Lucide-style SF Symbol approximations (stroke weight).
    var icon: String {
        switch self {
        case .home:    return "house"
        case .myTrips: return "car"
        case .chat:    return "bubble.left.and.bubble.right"
        case .profile: return "person.circle"
        }
    }
}

// MARK: — HopTabView ──────────────────────────────────────────────────────────

/// Bottom-nav `TabView` for the authenticated app shell.
///
/// `navigate` is forwarded to each tab root so they can push detail routes onto
/// the outer `HopNavigationStack` path.  Tab-to-tab switching is local state.
///
/// Layout rule (Agent.md):  Never float chrome over a page-style TabView.
/// We use the standard `.tabViewStyle(.automatic)` (sidebar/tab bar), which
/// renders the system tab bar in the safe area — no manual inset required.
struct HopTabView: View {

    /// Push a `HopRoute` onto the enclosing `HopNavigationStack` path.
    var navigate: (HopRoute) -> Void

    /// Called when the passenger submits a search so HopNavigationStack can
    /// capture params before pushing .searchResults.
    var onSearch: (String, String, String, Int) -> Void

    /// Called when the user logs out from the Settings tab.
    var onLogout: () -> Void

    /// Active booking ID bubbled up from HomeView so the Chat tab can go
    /// straight to Chat(bookingId) when the user has a live booking.
    @Binding var activeBookingId: String?

    /// Current role (passenger/driver) from HomeView so the Chat tab routes
    /// drivers to MyTripsDriver instead of a direct chat screen.
    @Binding var selectedRole: HopRole

    @State private var selectedTab: HopTab = .home

    var body: some View {
        TabView(selection: $selectedTab) {

            // ── Home (unified Passenger ↔ Driver) ────────────────────────────
            HomeView(
                navigate: navigate,
                onSearch: onSearch,
                onLogout: onLogout,
                activeBookingId: $activeBookingId,
                selectedRole: $selectedRole
            )
            .tabItem { Label(HopTab.home.label, systemImage: HopTab.home.icon) }
            .tag(HopTab.home)

            // ── My Trips ─────────────────────────────────────────────────────
            MyTripsPassengerView(
                onNavigateBack: { selectedTab = .home },
                onNavigateToTripDetailActive: { bookingId in navigate(.tripDetailActive(bookingId: bookingId)) },
                onNavigateToTripDetail: { tripId in navigate(.tripDetail(id: tripId)) },
                onNavigateToPassengerSettlement: { bookingId in navigate(.passengerSettlement(bookingId: bookingId)) },
                onNavigateToHome: { selectedTab = .home },
                onNavigateToChat: { selectedTab = .chat },
                onNavigateToProfile: { selectedTab = .profile },
                onNavigateToFindRide: { selectedTab = .home },
                inTab: true
            )
            .tabItem { Label(HopTab.myTrips.label, systemImage: HopTab.myTrips.icon) }
            .tag(HopTab.myTrips)

            // ── Chat list (SH-04b) ───────────────────────────────────────────
            ChatListView(
                onBack:           { selectedTab = .home },
                onNavigateToChat: { bookingId in navigate(.chat(bookingId: bookingId)) }
            )
            .tabItem { Label(HopTab.chat.label, systemImage: HopTab.chat.icon) }
            .tag(HopTab.chat)

            // ── Profile (own profile via SH-02) ──────────────────────────────
            OwnProfileView(
                navigate: navigate,
                onBack:   { selectedTab = .home }
            )
            .tabItem { Label(HopTab.profile.label, systemImage: HopTab.profile.icon) }
            .tag(HopTab.profile)
        }
        .tint(Color.hopPrimaryLime)
        // Dark tab-bar background to match the app's surface colour
        .onAppear { applyTabBarAppearance() }
    }

    // MARK: — UITabBar appearance

    private func applyTabBarAppearance() {
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        // Light theme: white background with subtle top border, matching the
        // light auth/passenger surfaces and Android's NavigationBar styling.
        appearance.backgroundColor = UIColor(Color.hopBackground)
        appearance.shadowColor     = UIColor(Color.hopAuthInputBorder)

        // Normal item colour
        appearance.stackedLayoutAppearance.normal.iconColor    = UIColor(Color.hopAuthTextSecondary)
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [
            .foregroundColor: UIColor(Color.hopAuthTextSecondary)
        ]
        // Selected item colour
        appearance.stackedLayoutAppearance.selected.iconColor    = UIColor(Color.hopPrimaryLime)
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [
            .foregroundColor: UIColor(Color.hopPrimaryLime)
        ]

        UITabBar.appearance().standardAppearance   = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
}

// MARK: — Placeholder tab root ────────────────────────────────────────────────

/// Temporary full-screen placeholder rendered inside each tab until the real
/// screen is implemented.  Replaced 1-for-1 as screens are built.
private struct HopTabPlaceholder: View {
    let title: String
    let subtitle: String
    var navigate: (HopRoute) -> Void

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: HopSpacing.md) {
                Text(title)
                    .font(HopFont.headlineLarge())
                    .foregroundColor(Color.hopTextPrimary)

                Text(subtitle)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}

// MARK: — Previews ─────────────────────────────────────────────────────────────

#Preview("Default – Home selected") {
    HopTabView(navigate: { _ in }, onSearch: { _, _, _, _ in }, onLogout: {}, activeBookingId: .constant(nil), selectedRole: .constant(.passenger))
}

#Preview("My Trips selected") {
    // SwiftUI previews cannot drive @State from outside; the tab bar itself
    // controls selection.  Use the live preview to switch tabs interactively.
    HopTabView(navigate: { _ in }, onSearch: { _, _, _, _ in }, onLogout: {}, activeBookingId: .constant(nil), selectedRole: .constant(.passenger))
}

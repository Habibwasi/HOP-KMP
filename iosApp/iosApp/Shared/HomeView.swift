import SwiftUI
import Shared

// MARK: — SH-01 / PA-01 / DR-01 Unified Home ──────────────────────────────────
//
// Mirrors `HomeScreen.kt` 1:1.  Renders a shared top bar (Hop logo +
// RoleTogglePill + bell with unread badge) and an animated content area
// that swaps between the Passenger and Driver content for the selected role.
//
// The bottom navigation bar is supplied by the enclosing `HopTabView` —
// this view intentionally does not draw its own, since iOS uses a
// system-level `TabView` for the four root destinations.

struct HomeView: View {

    var navigate: (HopRoute) -> Void
    var onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void
    var onLogout: () -> Void

    /// Written whenever activeBooking changes so HopTabView can intercept
    /// the Chat tab and push Chat(bookingId) directly.
    @Binding var activeBookingId: String?

    /// Surfaced to HopTabView so the Chat tab can navigate to MyTripsDriver
    /// when the user is in driver role (drivers pick a passenger from their trip).
    @Binding var selectedRole: HopRole

    @StateObject private var statsWrapper = HomeStatsViewModelWrapper()

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                HomeTopBar(
                    selectedRole: $selectedRole,
                    notificationsUnread: Int(statsWrapper.state.unreadCount),
                    onNotifications: { navigate(.notifications) }
                )

                ZStack {
                    if selectedRole == .passenger {
                        PassengerHomeView(
                            onSearch: onSearch,
                            onSwitchToDriver: { withAnimation { selectedRole = .driver } },
                            navigate: navigate
                        )
                        .transition(.opacity)
                    } else {
                        DriverHomeView(navigate: navigate)
                            .transition(.opacity)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .animation(.easeInOut(duration: 0.2), value: selectedRole)
        .onChange(of: statsWrapper.state.activeBooking?.id) { _, newId in
            activeBookingId = newId
        }
        .task {
            statsWrapper.startObserving { _ in }
            statsWrapper.load()
        }
    }
}

// MARK: — HomeTopBar ──────────────────────────────────────────────────────────

private struct HomeTopBar: View {
    @Binding var selectedRole: HopRole
    let notificationsUnread: Int
    let onNotifications: () -> Void

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            HopLogo(height: 30)
                .accessibilityIdentifier("HomeHopLogo")

            Spacer(minLength: 0)

            RoleTogglePill(selectedRole: $selectedRole)
                .accessibilityIdentifier("HomeRoleToggle")

            ZStack(alignment: .topTrailing) {
                Button(action: onNotifications) {
                    Image(systemName: "bell.fill")
                        .font(.system(size: 22, weight: .regular))
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .frame(width: 44, height: 44)
                }
                .accessibilityLabel("Notifications")

                UnreadBadge(count: notificationsUnread)
                    .offset(x: -4, y: 4)
            }
            .frame(width: 44, height: 44)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopBackground)
    }
}

#Preview {
    @Previewable @State var activeBookingId: String? = nil
    @Previewable @State var selectedRole: HopRole = .passenger
    HomeView(navigate: { _ in }, onSearch: { _, _, _, _ in }, onLogout: {}, activeBookingId: $activeBookingId, selectedRole: $selectedRole)
}

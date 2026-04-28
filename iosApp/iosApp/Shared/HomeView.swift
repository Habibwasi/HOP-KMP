import SwiftUI
import Shared

// MARK: — SH-01 / PA-01 / DR-01 Unified Home ──────────────────────────────────
//
// Single home screen with a Passenger ↔ Driver role-toggle pill. Mirrors
// `HomeScreen.kt` on Android. The role toggle swaps between the existing
// `PassengerHomeView` content and the new `DriverHomeView` content.

struct HomeView: View {

    var navigate: (HopRoute) -> Void
    var onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void
    var onLogout: () -> Void

    @State private var selectedRole: HopRole = .passenger

    var body: some View {
        ZStack(alignment: .top) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // ── Role toggle pill (centered, on top of all content) ────────
                RoleTogglePill(selectedRole: $selectedRole)
                    .padding(.top, HopSpacing.sm)
                    .padding(.bottom, HopSpacing.xs)
                    .accessibilityIdentifier("HomeRoleToggle")

                // ── Active role content ───────────────────────────────────────
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
        }
        .navigationBarHidden(true)
        .animation(.easeInOut(duration: 0.2), value: selectedRole)
    }
}

#Preview {
    HomeView(navigate: { _ in }, onSearch: { _, _, _, _ in }, onLogout: {})
        .preferredColorScheme(.dark)
}

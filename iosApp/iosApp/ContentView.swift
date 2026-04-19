import SwiftUI
import Shared

// ── App root ──────────────────────────────────────────────────────────────────
//
// Drives the top-level auth vs. main-app split.
// Start destination: OnboardingView (inside AuthNavigationCoordinator).
// On NavigateToHome effect: `isAuthenticated` flips to true and the root is
// replaced with HopNavigationStack (HopTabView + all screen destinations).

struct ContentView: View {

    @State private var isAuthenticated = false

    var body: some View {
        Group {
            if isAuthenticated {
                HopNavigationStack()
            } else {
                AuthNavigationCoordinator {
                    withAnimation(.easeInOut) {
                        isAuthenticated = true
                    }
                }
            }
        }
        .preferredColorScheme(.dark)
    }
}

#Preview {
    ContentView()
}

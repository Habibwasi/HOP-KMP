import SwiftUI
import Shared

// ── App root ──────────────────────────────────────────────────────────────────
//
// Drives the top-level auth vs. main-app split.
// Once `isAuthenticated` becomes true the auth coordinator is swapped out for
// the main navigation stack (to be built in subsequent screens).

struct ContentView: View {

    @State private var isAuthenticated = false

    var body: some View {
        Group {
            if isAuthenticated {
                // Placeholder: replace with MainNavigationCoordinator when built
                Text("Home — coming soon")
                    .foregroundColor(Color.hopTextPrimary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.hopSurface.ignoresSafeArea())
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

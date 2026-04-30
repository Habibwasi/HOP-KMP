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
    @State private var showSplash = true
    @StateObject private var authWrapper = AuthViewModelWrapper()

    var body: some View {
        Group {
            if showSplash {
                // Brand splash shown on cold start — parity with Android's
                // `SplashRoute` (3-second animated scene before navigating
                // forward to the auth or main-app root).
                SplashView(onComplete: {
                    withAnimation(.easeInOut(duration: 0.35)) {
                        showSplash = false
                    }
                })
                .transition(.opacity)
            } else if isAuthenticated {
                HopNavigationStack()
            } else {
                AuthNavigationCoordinator {
                    withAnimation(.easeInOut) {
                        isAuthenticated = true
                    }
                }
            }
        }
        // Auth + Passenger + Driver screens are light-themed (white background,
        // hopAuth* tokens). Forcing light colour scheme keeps system controls
        // (DatePicker, sheets, alerts) readable on white surfaces.
        .preferredColorScheme(.light)
        .task { authWrapper.startObserving() }
        .task {
            // Listen for NavigateToHome effects emitted by the silent session
            // restore that AuthViewModel fires on init.
            for await effect in authWrapper.viewModel.effect {
                if effect is AuthEffectNavigateToHome {
                    withAnimation(.easeInOut) { isAuthenticated = true }
                } else if effect is AuthEffectNavigateToLogin {
                    // Only NavigateToLogin (explicit logout) flips back to auth screens.
                    // SessionExpired no longer forces the user out automatically.
                    withAnimation(.easeInOut) { isAuthenticated = false }
                }
            }
        }
    }
}

#Preview {
    ContentView()
}

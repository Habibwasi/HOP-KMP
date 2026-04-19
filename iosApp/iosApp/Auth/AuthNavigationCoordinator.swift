import SwiftUI
import Shared

// ── Auth navigation destination ───────────────────────────────────────────────

enum AuthDestination: Hashable {
    case splash
    case signUp
    case login
    case otpVerification(phone: String)
    // Home is handled at the app root level — not an auth destination.
}

// ── AuthNavigationCoordinator ─────────────────────────────────────────────────
//
// Hosts the ON-01 → ON-02 / ON-03 → ON-04 flow using NavigationStack.
// Each screen fires callbacks; this coordinator translates them to path updates.
//
// `onAuthComplete` is fired when OTP verification succeeds and the app should
// move to the main (home) navigation stack.

struct AuthNavigationCoordinator: View {

    var onAuthComplete: () -> Void

    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            SplashView(
                onNavigateToSignUp: {
                    path.append(AuthDestination.signUp)
                },
                onNavigateToHome: {
                    path.append(AuthDestination.login)
                }
            )
            .navigationDestination(for: AuthDestination.self) { destination in
                destinationView(for: destination)
                    .navigationBarBackButtonHidden(false)
                    .toolbarBackground(Color.hopSurface, for: .navigationBar)
                    .toolbarColorScheme(.dark, for: .navigationBar)
            }
        }
    }

    @ViewBuilder
    private func destinationView(for destination: AuthDestination) -> some View {
        switch destination {
        case .signUp:
            SignUpView(
                onNavigateToOtpVerification: { phone in
                    path.append(AuthDestination.otpVerification(phone: phone))
                },
                onNavigateToLogin: {
                    // Replace sign-up with login (pop sign-up, push login)
                    path.removeLast()
                    path.append(AuthDestination.login)
                }
            )

        case .login:
            LoginView(
                onNavigateToHome: {
                    onAuthComplete()
                },
                onNavigateToSignUp: {
                    path.removeLast()
                    path.append(AuthDestination.signUp)
                },
                onNavigateToForgotPassword: {
                    // Stub — post-MVP
                }
            )

        case let .otpVerification(phone):
            OtpVerificationView(
                phone: phone,
                onNavigateToHome: {
                    onAuthComplete()
                }
            )

        case .splash:
            SplashView(
                onNavigateToSignUp: { path.append(AuthDestination.signUp) },
                onNavigateToHome:   { path.append(AuthDestination.login) }
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

#Preview {
    AuthNavigationCoordinator(onAuthComplete: {})
}

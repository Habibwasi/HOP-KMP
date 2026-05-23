import SwiftUI
import Shared

// ── Auth navigation destination ───────────────────────────────────────────────

enum AuthDestination: Hashable {
    case onboarding
    case splash
    case signUp
    case login
    case forgotPassword
    case setNewPassword
    case verifyEmail(email: String)
    case emailVerified
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
            OnboardingView(
                onNavigateToSignUp: {
                    path.append(AuthDestination.signUp)
                },
                onNavigateToLogin: {
                    path.append(AuthDestination.login)
                }
            )
            .navigationDestination(for: AuthDestination.self) { destination in
                destinationView(for: destination)
                    .toolbarBackground(Color.hopSurface, for: .navigationBar)
                    .toolbarColorScheme(.dark, for: .navigationBar)
            }
        }
    }

    @ViewBuilder
    private func destinationView(for destination: AuthDestination) -> some View {
        switch destination {
        case .onboarding:
            OnboardingView(
                onNavigateToSignUp: { path.append(AuthDestination.signUp) },
                onNavigateToLogin:  { path.append(AuthDestination.login) }
            )

        case .signUp:
            SignUpView(
                onNavigateToHome: {
                    onAuthComplete()
                },
                onNavigateToLogin: {
                    // Replace sign-up with login (pop sign-up, push login)
                    path.removeLast()
                    path.append(AuthDestination.login)
                },
                onNavigateToVerifyEmail: { email in
                    path.append(AuthDestination.verifyEmail(email: email))
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
                    path.append(AuthDestination.forgotPassword)
                }
            )

        case .forgotPassword:
            ForgotPasswordView(onBack: {
                path.removeLast()
            })

        case .setNewPassword:
            SetNewPasswordView(
                onPasswordUpdated: {
                    // Pop back to login
                    while path.count > 1 { path.removeLast() }
                },
                onBack: { path.removeLast() }
            )

        case .verifyEmail(let email):
            VerifyEmailView(
                onNavigateToEmailVerified: {
                    // Replace verifyEmail with emailVerified
                    path.removeLast()
                    path.append(AuthDestination.emailVerified)
                },
                onNavigateBack: { path.removeLast() }
            )
            .onAppear { _ = email } // email is captured in VerifyEmailView state via ViewModel

        case .emailVerified:
            EmailVerifiedView(
                onNavigateToHome: { onAuthComplete() }
            )

        case .splash:
            // Legacy — kept for backwards compat; root now uses OnboardingView.
            OnboardingView(
                onNavigateToSignUp: { path.append(AuthDestination.signUp) },
                onNavigateToLogin:  { path.append(AuthDestination.login) }
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

#Preview {
    AuthNavigationCoordinator(onAuthComplete: {})
}

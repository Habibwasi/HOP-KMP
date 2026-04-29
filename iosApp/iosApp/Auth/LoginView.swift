import SwiftUI
import Shared

// MARK: - ON-03 Log In ───────────────────────────────────────────────────────
// Mirrors `LoginScreen.kt`. Light theme, welcome header, email/password with
// Show/Hide toggle, "Forgot password?" link, primary "Log In" button, ghost
// MitID button (disabled — taps emit "Coming soon" toast).

struct LoginView: View {
    var onNavigateToHome:           () -> Void
    var onNavigateToSignUp:         () -> Void
    var onNavigateToForgotPassword: () -> Void

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var email = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var toast: String? = nil

    private var emailValid: Bool {
        email.contains("@") && (email.split(separator: "@").last?.contains(".") ?? false)
    }
    private var canSubmit: Bool {
        emailValid && !password.isEmpty && !wrapper.state.isLoading
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: HopSpacing.xl)

                    Text("Welcome back")
                        .font(HopFont.headlineSmall(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    Spacer().frame(height: HopSpacing.xs)

                    Text("Log in to your Hop account")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)

                    Spacer().frame(height: HopSpacing.xl)

                    HopTextField(
                        label: "Email address",
                        placeholder: "jane@example.com",
                        text: $email,
                        keyboardType: .emailAddress,
                        isEnabled: !wrapper.state.isLoading,
                        lightSurface: true
                    )

                    Spacer().frame(height: HopSpacing.md)

                    HopTextField(
                        label: "Password",
                        placeholder: "••••••••",
                        text: $password,
                        isSecure: !showPassword,
                        isEnabled: !wrapper.state.isLoading,
                        trailingLabel: showPassword ? "Hide" : "Show",
                        trailingAction: { showPassword.toggle() },
                        submitLabel: .done,
                        lightSurface: true
                    )

                    HStack {
                        Spacer()
                        Button(action: onNavigateToForgotPassword) {
                            Text("Forgot password?")
                                .font(HopFont.labelMedium())
                                .foregroundColor(Color.hopAuthAccent)
                                .underline()
                        }
                        .disabled(wrapper.state.isLoading)
                    }
                    .padding(.top, HopSpacing.xs)

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Log In",
                        variant: .primary,
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit,
                        action: {
                            wrapper.login(
                                email: email.trimmingCharacters(in: .whitespaces),
                                password: password
                            )
                        }
                    )

                    Spacer().frame(height: HopSpacing.md)

                    HopButton(
                        text: "Continue with MitID",
                        variant: .ghost,
                        isEnabled: false,
                        lightSurface: true,
                        action: {}
                    )
                    .overlay(
                        Rectangle()
                            .fill(Color.clear)
                            .contentShape(Rectangle())
                            .onTapGesture { showToast("Coming soon") }
                    )

                    Spacer().frame(height: HopSpacing.xl)

                    HStack(spacing: 4) {
                        Spacer()
                        Text("Don't have an account?")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Button(action: onNavigateToSignUp) {
                            Text("Sign up")
                                .font(HopFont.bodyMedium(weight: .semibold))
                                .foregroundColor(Color.hopAuthAccent)
                                .underline()
                        }
                        Spacer()
                    }

                    Spacer().frame(height: HopSpacing.lg)
                }
                .padding(.horizontal, HopSpacing.md)
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .task { wrapper.startObserving() }
        .task {
            for await effect in wrapper.viewModel.effect {
                await handleEffect(effect)
            }
        }
    }

    @MainActor
    private func handleEffect(_ effect: AuthEffect) async {
        switch effect {
        case is AuthEffectNavigateToHome:
            onNavigateToHome()
        case let snack as AuthEffectShowSnackbar:
            showToast(snack.message)
            wrapper.clearError()
        case is AuthEffectSessionExpired:
            showToast("Session expired. Please log in again.")
            wrapper.clearError()
        default:
            break
        }
    }

    private func showToast(_ message: String) {
        withAnimation { toast = message }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            withAnimation { toast = nil }
        }
    }
}

#Preview {
    LoginView(onNavigateToHome: {}, onNavigateToSignUp: {}, onNavigateToForgotPassword: {})
}

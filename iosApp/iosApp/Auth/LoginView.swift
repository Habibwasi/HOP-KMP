import SwiftUI
import Shared

// ── ON-03 Log In ──────────────────────────────────────────────────────────────

struct LoginView: View {

    @StateObject private var wrapper = AuthViewModelWrapper()

    var onNavigateToHome: () -> Void
    var onNavigateToSignUp: () -> Void
    var onNavigateToForgotPassword: () -> Void

    // ── Local form state ──────────────────────────────────────────────────────
    @State private var email        = ""
    @State private var password     = ""
    @State private var showPassword = false

    // ── Toast ─────────────────────────────────────────────────────────────────
    @State private var toastMessage: String? = nil

    // ── Derived validation ────────────────────────────────────────────────────
    private var emailValid:    Bool { email.contains("@") && email.split(separator: "@").last?.contains(".") == true }
    private var passwordValid: Bool { !password.isEmpty }
    private var canSubmit: Bool { emailValid && passwordValid && !wrapper.state.isLoading }

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: HopSpacing.xl)

                    // ── Header ─────────────────────────────────────────────────
                    Text("Welcome back")
                        .font(HopFont.heading)
                        .foregroundColor(Color.hopTextPrimary)

                    Spacer().frame(height: HopSpacing.xs)

                    Text("Log in to your Hop account")
                        .font(HopFont.body)
                        .foregroundColor(Color.hopTextSecondary)

                    Spacer().frame(height: HopSpacing.xl)

                    // ── Email ──────────────────────────────────────────────────
                    HopTextField(
                        label: "Email address",
                        placeholder: "jane@example.com",
                        text: $email,
                        keyboardType: .emailAddress,
                        isEnabled: !wrapper.state.isLoading
                    )

                    Spacer().frame(height: HopSpacing.md)

                    // ── Password ───────────────────────────────────────────────
                    HopTextField(
                        label: "Password",
                        placeholder: "••••••••",
                        text: $password,
                        isSecure: !showPassword,
                        isEnabled: !wrapper.state.isLoading,
                        trailingLabel: showPassword ? "Hide" : "Show",
                        trailingAction: { showPassword.toggle() },
                        submitLabel: .done
                    )

                    // ── Forgot password link ────────────────────────────────────
                    HStack {
                        Spacer()
                        Button(action: onNavigateToForgotPassword) {
                            Text("Forgot password?")
                                .font(HopFont.label)
                                .foregroundColor(Color.hopPrimaryLime)
                                .underline()
                        }
                        .disabled(wrapper.state.isLoading)
                    }
                    .padding(.top, HopSpacing.xs)

                    Spacer().frame(height: HopSpacing.lg)

                    // ── Log In ─────────────────────────────────────────────────
                    HopPrimaryButton(
                        title: "Log In",
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit
                    ) {
                        wrapper.login(
                            email: email.trimmingCharacters(in: .whitespaces),
                            password: password
                        )
                    }

                    Spacer().frame(height: HopSpacing.md)

                    // ── Continue with MitID (disabled) ─────────────────────────
                    HopGhostButton(title: "Continue with MitID", isEnabled: false) {
                        toastMessage = "Coming soon"
                    }
                    .onTapGesture { toastMessage = "Coming soon" }

                    Spacer().frame(height: HopSpacing.xl)

                    // ── Footer link ────────────────────────────────────────────
                    HStack {
                        Spacer()
                        Text("Don't have an account? ")
                            .font(HopFont.body)
                            .foregroundColor(Color.hopTextSecondary)
                        Button(action: onNavigateToSignUp) {
                            Text("Sign up")
                                .font(HopFont.body)
                                .fontWeight(.semibold)
                                .foregroundColor(Color.hopPrimaryLime)
                                .underline()
                        }
                        Spacer()
                    }

                    Spacer().frame(height: HopSpacing.lg)
                }
                .padding(.horizontal, HopSpacing.md)
            }

            // ── Toast overlay ──────────────────────────────────────────────────
            if let msg = toastMessage {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { toastMessage = nil }
                        }
                    }
            }
        }
        .background(Color.hopSurface.ignoresSafeArea())
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
            withAnimation { toastMessage = snack.message }
            wrapper.clearError()
        case is AuthEffectSessionExpired:
            // Token refresh failed while on login screen — surface to user.
            withAnimation { toastMessage = "Session expired. Please log in again." }
            wrapper.clearError()
        default:
            break
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

#Preview("Default") {
    LoginView(
        onNavigateToHome: {},
        onNavigateToSignUp: {},
        onNavigateToForgotPassword: {}
    )
}

#Preview("Loading") {
    LoginView(
        onNavigateToHome: {},
        onNavigateToSignUp: {},
        onNavigateToForgotPassword: {}
    )
}

#Preview("Error state — via snackbar") {
    LoginView(
        onNavigateToHome: {},
        onNavigateToSignUp: {},
        onNavigateToForgotPassword: {}
    )
}

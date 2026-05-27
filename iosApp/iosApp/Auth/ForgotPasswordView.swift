import SwiftUI
import Shared

// MARK: - ON-03b Forgot Password ─────────────────────────────────────────────
// Mirrors `ForgotPasswordScreen.kt`. Two states (form / success) animated via
// AnimatedContent (cross-fade). Light theme, plain top bar with back arrow.

struct ForgotPasswordView: View {
    var onBack: () -> Void

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var email = ""
    @State private var submitted = false
    @State private var toast: String? = nil

    private var emailValid: Bool {
        email.contains("@") && (email.split(separator: "@").last?.contains(".") ?? false)
    }
    private var canSubmit: Bool { emailValid && !wrapper.state.isLoading }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                topBar
                content
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task { wrapper.startObserving() }
        .task {
            for await effect in wrapper.viewModel.effect {
                await handleEffect(effect)
            }
        }
    }

    private var topBar: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 44, height: 44)
            }
            .buttonStyle(.plain)
            Spacer()
        }
        .padding(.horizontal, HopSpacing.xs)
        .frame(height: 56)
    }

    @ViewBuilder
    private var content: some View {
        ZStack {
            if submitted {
                successContent.transition(.opacity)
            } else {
                formContent.transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.25), value: submitted)
    }

    private var formContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: HopSpacing.lg)

                ZStack {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.hopAuthInputSurface)
                        .frame(width: 64, height: 64)
                    Image(systemName: "lock.rotation")
                        .font(.system(size: 32))
                        .foregroundColor(Color.hopAuthAccent)
                }

                Spacer().frame(height: HopSpacing.lg)

                Text("Reset password")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)

                Spacer().frame(height: HopSpacing.xs)

                Text("Enter the email address linked to your account and we'll send you a reset link.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)

                Spacer().frame(height: HopSpacing.xl)

                HopTextField(label: "Email address", placeholder: "jane@example.com",
                             text: $email, keyboardType: .emailAddress,
                             isEnabled: !wrapper.state.isLoading,
                             submitLabel: .done, lightSurface: true)

                Spacer().frame(height: HopSpacing.lg)

                HopButton(
                    text: wrapper.state.isLoading ? "Sending\u{2026}" : "Send reset link",
                    variant: .primary,
                    isLoading: wrapper.state.isLoading,
                    isEnabled: canSubmit,
                    action: {
                        wrapper.requestPasswordReset(email: email.trimmingCharacters(in: .whitespaces))
                    }
                )

                Spacer().frame(height: HopSpacing.md)

                HStack {
                    Spacer()
                    Button(action: onBack) {
                        Text("Back to Log In")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .underline()
                    }
                    .buttonStyle(.plain)
                    Spacer()
                }

                Spacer().frame(height: HopSpacing.xl)
            }
            .padding(.horizontal, HopSpacing.lg)
        }
    }

    private var successContent: some View {
        VStack(spacing: 0) {
            Spacer()

            ZStack {
                RoundedRectangle(cornerRadius: 48)
                    .fill(Color.hopAuthAccent.opacity(0.10))
                    .frame(width: 96, height: 96)
                Image(systemName: "envelope.open.fill")
                    .font(.system(size: 44))
                    .foregroundColor(Color.hopAuthAccent)
            }

            Spacer().frame(height: HopSpacing.lg)

            Text("Check your email")
                .font(HopFont.headlineMedium(weight: .bold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)

            Spacer().frame(height: HopSpacing.xs)

            Text("We've sent a reset link to \(email).\nCheck your inbox and follow the instructions.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)

            Spacer()

            HopButton(text: "Back to Log In", variant: .primary, action: onBack)

            Spacer().frame(height: HopSpacing.md)

            Button(action: { submitted = false }) {
                Text("Didn't receive it? Try again")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .underline()
            }
            .buttonStyle(.plain)

            Spacer().frame(height: HopSpacing.xl)
        }
        .padding(.horizontal, HopSpacing.lg)
    }

    @MainActor
    private func handleEffect(_ effect: AuthEffect) async {
        switch effect {
        case is AuthEffectPasswordResetEmailSent:
            submitted = true
        case let snack as AuthEffectShowSnackbar:
            showToast(snack.message)
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
    ForgotPasswordView(onBack: {})
}

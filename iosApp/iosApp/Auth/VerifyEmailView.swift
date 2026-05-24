import SwiftUI
import Shared

// MARK: - ON-02a Verify Email ─────────────────────────────────────────────────
// Shown after sign-up when Supabase returns VerificationRequired.
// Displays the pending email address, a resend button with 60-second countdown,
// and auto-advances to EmailVerifiedView once the callback is received.

struct VerifyEmailView: View {
    var onNavigateToEmailVerified: () -> Void
    var onNavigateBack: () -> Void

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .center, spacing: 0) {
                // ── Back button ───────────────────────────────────────────
                HStack {
                    Button(action: {
                        wrapper.clearEmailVerification()
                        onNavigateBack()
                    }) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .padding(HopSpacing.sm)
                    }
                    Spacer()
                }

                Spacer().frame(height: HopSpacing.xl)

                // ── Envelope icon ─────────────────────────────────────────
                Image(systemName: "envelope.circle.fill")
                    .font(.system(size: 72))
                    .foregroundColor(Color.hopAuthAccent)

                Spacer().frame(height: HopSpacing.lg)

                // ── Heading ───────────────────────────────────────────────
                Text("Check your email")
                    .font(HopFont.headlineSmall(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: HopSpacing.sm)

                // ── Body ──────────────────────────────────────────────────
                Text("We sent a confirmation link to")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)

                Text(wrapper.state.pendingVerificationEmail ?? "")
                    .font(HopFont.bodyMedium(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: HopSpacing.xs)

                Text("Tap the link in the email to verify your account and continue.")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: HopSpacing.xl)

                // ── Resend button ─────────────────────────────────────────
                let cooldown = wrapper.state.resendCooldownSeconds
                let canResend = cooldown == 0

                HopButton(
                    text: canResend ? "Resend email" : "Resend in \(cooldown)s",
                    variant: .ghost,
                    isEnabled: canResend,
                    lightSurface: true,
                    action: {
                        wrapper.resendVerificationEmail()
                    }
                )
                .frame(maxWidth: .infinity)

                Spacer().frame(height: HopSpacing.md)

                Text("Didn't receive it? Check your spam folder.")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)

                Spacer()
            }
            .padding(.horizontal, HopSpacing.md)

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
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
        case is AuthEffectNavigateToEmailVerified:
            onNavigateToEmailVerified()
        case let snack as AuthEffectShowSnackbar:
            showToast(snack.message)
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
    VerifyEmailView(onNavigateToEmailVerified: {}, onNavigateBack: {})
}

import SwiftUI
import Shared

// MARK: - ON-03c Set New Password ────────────────────────────────────────────
// Mirrors `SetNewPasswordScreen.kt`. Reached via recovery deep-link.

struct SetNewPasswordView: View {
    var onPasswordUpdated: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var password = ""
    @State private var confirm = ""
    @State private var showPassword = false
    @State private var showConfirm = false
    @State private var toast: String? = nil

    private var passwordValid: Bool { password.count >= 8 }
    private var passwordsMatch: Bool { password == confirm }
    private var canSubmit: Bool { passwordValid && passwordsMatch && !wrapper.state.isLoading }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                topBar

                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        Spacer().frame(height: HopSpacing.lg)

                        ZStack {
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.hopAuthInputSurface)
                                .frame(width: 64, height: 64)
                            Image(systemName: "lock.fill")
                                .font(.system(size: 32))
                                .foregroundColor(Color.hopAuthAccent)
                        }

                        Spacer().frame(height: HopSpacing.lg)

                        Text("Set new password")
                            .font(HopFont.headlineMedium(weight: .bold))
                            .foregroundColor(Color.hopAuthTextPrimary)

                        Spacer().frame(height: HopSpacing.xs)

                        Text("Choose a strong password of at least 8 characters.")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)

                        Spacer().frame(height: HopSpacing.xl)

                        HopTextField(
                            label: "New password", placeholder: "••••••••",
                            text: $password, isSecure: !showPassword,
                            isEnabled: !wrapper.state.isLoading,
                            trailingLabel: showPassword ? "Hide" : "Show",
                            trailingAction: { showPassword.toggle() },
                            submitLabel: .next, lightSurface: true
                        )

                        Spacer().frame(height: HopSpacing.md)

                        HopTextField(
                            label: "Confirm password", placeholder: "••••••••",
                            text: $confirm, isSecure: !showConfirm,
                            isEnabled: !wrapper.state.isLoading,
                            errorMessage: (!confirm.isEmpty && !passwordsMatch) ? "Passwords don't match" : nil,
                            trailingLabel: showConfirm ? "Hide" : "Show",
                            trailingAction: { showConfirm.toggle() },
                            submitLabel: .done, lightSurface: true
                        )

                        Spacer().frame(height: HopSpacing.lg)

                        HopButton(
                            text: wrapper.state.isLoading ? "Saving…" : "Save password",
                            variant: .primary,
                            isLoading: wrapper.state.isLoading,
                            isEnabled: canSubmit,
                            action: {
                                wrapper.viewModel.onEvent(event: AuthEventUpdatePassword(newPassword: password))
                            }
                        )

                        Spacer().frame(height: HopSpacing.xl)
                    }
                    .padding(.horizontal, HopSpacing.lg)
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
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

    @MainActor
    private func handleEffect(_ effect: AuthEffect) async {
        switch effect {
        case is AuthEffectPasswordUpdated:
            onPasswordUpdated()
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
    SetNewPasswordView(onPasswordUpdated: {}, onBack: {})
}

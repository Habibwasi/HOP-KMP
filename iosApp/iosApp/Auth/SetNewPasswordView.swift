import SwiftUI
import Shared

// ── ON-03c Set New Password ──────────────────────────────────────────────────
//
// Mirror of `SetNewPasswordScreen.kt`. Reached after user follows the password
// reset email deep-link. Fires AuthEvent.UpdatePassword(newPassword) and pops
// back on success.

struct SetNewPasswordView: View {

    var onPasswordUpdated: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var password:        String = ""
    @State private var confirmPassword: String = ""
    @State private var showPassword:    Bool   = false
    @State private var localError:      String? = nil

    private var passwordValid: Bool { password.count >= 8 }
    private var passwordsMatch: Bool { password == confirmPassword }
    private var canSubmit: Bool {
        passwordValid && passwordsMatch && !wrapper.state.isLoading
    }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Spacer().frame(height: HopSpacing.lg)

                    ZStack {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.hopSurfaceElevated)
                            .frame(width: 64, height: 64)
                        Image(systemName: "key.fill")
                            .font(.system(size: 28, weight: .light))
                            .foregroundColor(Color.hopPrimaryLime)
                    }

                    Text("Set a new password")
                        .font(HopFont.headlineLarge(weight: .bold))
                        .foregroundColor(Color.hopTextPrimary)

                    Text("Choose a strong password with at least 8 characters.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)

                    HopTextField(
                        label: "New password",
                        placeholder: "••••••••",
                        text: $password,
                        isSecure: !showPassword,
                        errorMessage: !password.isEmpty && !passwordValid ? "At least 8 characters" : nil,
                        trailingLabel: showPassword ? "Hide" : "Show",
                        trailingAction: { showPassword.toggle() }
                    )

                    HopTextField(
                        label: "Confirm new password",
                        placeholder: "••••••••",
                        text: $confirmPassword,
                        isSecure: !showPassword,
                        errorMessage: !confirmPassword.isEmpty && !passwordsMatch ? "Passwords don't match" : nil
                    )

                    if let err = localError ?? wrapper.state.error {
                        Text(err)
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopError)
                    }

                    Spacer().frame(height: HopSpacing.md)

                    HopButton(
                        text: "Update password",
                        variant: .primary,
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit
                    ) {
                        localError = nil
                        wrapper.viewModel.onEvent(event: AuthEventUpdatePassword(newPassword: password))
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(Color.hopTextPrimary)
                }
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving()
            for await effect in wrapper.viewModel.effect {
                if effect is AuthEffectPasswordUpdated {
                    onPasswordUpdated()
                } else if let snack = effect as? AuthEffectShowSnackbar {
                    localError = snack.message
                }
            }
        }
    }
}

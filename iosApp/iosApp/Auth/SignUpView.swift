import SwiftUI
import Shared
import AuthenticationServices

// MARK: - ON-02 Sign Up ──────────────────────────────────────────────────────
// Mirrors `SignUpScreen.kt`. Light theme, 5-field form (firstName, lastName,
// email, phone, password), terms-accept checkbox with annotated link text,
// primary "Create account" button, ghost MitID button.

struct SignUpView: View {
    var onNavigateToHome:           () -> Void
    var onNavigateToLogin:          () -> Void
    var onNavigateToVerifyEmail:    (String) -> Void = { _ in }

    @StateObject private var wrapper = AuthViewModelWrapper()

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var termsAccepted = false
    @State private var toast: String? = nil

    private var firstNameValid: Bool { firstName.trimmingCharacters(in: .whitespaces).count >= 2 }
    private var lastNameValid:  Bool { lastName.trimmingCharacters(in: .whitespaces).count >= 2 }
    private var emailValid: Bool {
        email.contains("@") && (email.split(separator: "@").last?.contains(".") ?? false)
    }
    private var phoneValid: Bool {
        let trimmed = phone.trimmingCharacters(in: .whitespaces)
        let pattern = #"^\+[1-9][\d\s\-]{6,14}$"#
        return trimmed.range(of: pattern, options: .regularExpression) != nil
    }
    private var passwordValid: Bool { password.count >= 8 }
    private var canSubmit: Bool {
        firstNameValid && lastNameValid && emailValid && phoneValid && passwordValid && termsAccepted && !wrapper.state.isLoading
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: HopSpacing.xl)

                    Text("Create your account")
                        .font(HopFont.headlineSmall(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    Spacer().frame(height: HopSpacing.xs)

                    Text("Start your Ridly journey")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)

                    Spacer().frame(height: HopSpacing.xl)

                    HopTextField(label: "First name", placeholder: "Jane",
                                 text: $firstName, isEnabled: !wrapper.state.isLoading,
                                 lightSurface: true)
                    Spacer().frame(height: HopSpacing.md)

                    HopTextField(label: "Last name", placeholder: "Doe",
                                 text: $lastName, isEnabled: !wrapper.state.isLoading,
                                 lightSurface: true)
                    Spacer().frame(height: HopSpacing.md)

                    HopTextField(label: "Email address", placeholder: "jane@example.com",
                                 text: $email, keyboardType: .emailAddress,
                                 isEnabled: !wrapper.state.isLoading, lightSurface: true)
                    Spacer().frame(height: HopSpacing.md)

                    HopTextField(label: "Phone number", placeholder: "+45 20 12 34 56",
                                 text: $phone, keyboardType: .phonePad,
                                 isEnabled: !wrapper.state.isLoading, lightSurface: true)
                    Spacer().frame(height: HopSpacing.md)

                    HopTextField(label: "Password", placeholder: "••••••••",
                                 text: $password, isSecure: !showPassword,
                                 isEnabled: !wrapper.state.isLoading,
                                 trailingLabel: showPassword ? "Hide" : "Show",
                                 trailingAction: { showPassword.toggle() },
                                 submitLabel: .done, lightSurface: true)

                    Spacer().frame(height: HopSpacing.md)

                    // Terms checkbox
                    HStack(alignment: .top, spacing: HopSpacing.sm) {
                        Button(action: { termsAccepted.toggle() }) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(termsAccepted ? Color.hopPrimaryLime : Color.hopAuthInputBorder, lineWidth: 1.5)
                                    .background(
                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(termsAccepted ? Color.hopPrimaryLime : Color.clear)
                                    )
                                    .frame(width: 20, height: 20)
                                if termsAccepted {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(Color.hopAuthTextPrimary)
                                }
                            }
                        }
                        .buttonStyle(.plain)

                        termsLabel
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Create account",
                        variant: .primary,
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit,
                        action: {
                            wrapper.register(
                                firstName: firstName.trimmingCharacters(in: .whitespaces),
                                lastName:  lastName.trimmingCharacters(in: .whitespaces),
                                email:     email.trimmingCharacters(in: .whitespaces),
                                phone:     phone.trimmingCharacters(in: .whitespaces),
                                password:  password
                            )
                        }
                    )

                    Spacer().frame(height: HopSpacing.md)

                    GoogleSignInButton(
                        isEnabled: !wrapper.state.isLoading,
                        action: { wrapper.signInWithGoogle() }
                    )

                    Spacer().frame(height: HopSpacing.sm)

                    // Apple Sign-In — uses native ASAuthorizationAppleIDButton appearance
                    // as required by App Store guideline 4.8. Tap triggers Supabase
                    // browser-based OAuth (no native credential exchange needed).
                    SignInWithAppleButton(.signUp, onRequest: { _ in }, onCompletion: { _ in })
                        .signInWithAppleButtonStyle(.black)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .cornerRadius(12)
                        .allowsHitTesting(false)
                        .overlay(
                            Button(action: {
                                if !wrapper.state.isLoading { wrapper.signInWithApple() }
                            }) {
                                Color.clear
                            }
                        )
                        .opacity(wrapper.state.isLoading ? 0.5 : 1.0)

                    Spacer().frame(height: HopSpacing.xl)

                    Text("Already have an account?")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer().frame(height: HopSpacing.xs)

                    HopButton(
                        text: "Log in",
                        variant: .ghost,
                        isEnabled: !wrapper.state.isLoading,
                        lightSurface: true,
                        action: onNavigateToLogin
                    )

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

    @ViewBuilder
    private var termsLabel: some View {
        // Annotated text — "By creating an account, you agree to our Terms of Service and Privacy Policy."
        (Text("By creating an account, you agree to our ")
            .foregroundColor(Color.hopAuthTextSecondary)
         + Text("Terms of Service")
            .foregroundColor(Color.hopAuthAccent).underline()
         + Text(" and ")
            .foregroundColor(Color.hopAuthTextSecondary)
         + Text("Privacy Policy")
            .foregroundColor(Color.hopAuthAccent).underline()
         + Text(".")
            .foregroundColor(Color.hopAuthTextSecondary))
            .font(HopFont.bodySmall())
    }

    @MainActor
    private func handleEffect(_ effect: AuthEffect) async {
        switch effect {
        case is AuthEffectNavigateToHome:
            onNavigateToHome()
        case let navVerify as AuthEffectNavigateToVerifyEmail:
            onNavigateToVerifyEmail(navVerify.email)
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
    SignUpView(onNavigateToHome: {}, onNavigateToLogin: {})
}

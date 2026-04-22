import SwiftUI
import Shared

// ── ON-02 Sign Up ─────────────────────────────────────────────────────────────

struct SignUpView: View {

    @StateObject private var wrapper = AuthViewModelWrapper()

    var onNavigateToHome: () -> Void
    var onNavigateToLogin: () -> Void

    // ── Local form state ──────────────────────────────────────────────────────
    @State private var fullName    = ""
    @State private var email       = ""
    @State private var phone       = ""
    @State private var password    = ""
    @State private var showPassword = false
    @State private var termsAccepted = false

    // ── Toast ─────────────────────────────────────────────────────────────────
    @State private var toastMessage: String? = nil

    // ── Derived validation ────────────────────────────────────────────────────
    private var fullNameValid:  Bool { !fullName.trimmingCharacters(in: .whitespaces).isEmpty }
    private var emailValid:     Bool { email.contains("@") && email.split(separator: "@").last?.contains(".") == true }
    private var phoneValid:     Bool { phone.trimmingCharacters(in: .whitespaces).count >= 8 }
    private var passwordValid:  Bool { password.count >= 8 }
    private var canSubmit: Bool {
        fullNameValid && emailValid && phoneValid && passwordValid && termsAccepted && !wrapper.state.isLoading
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // ── Header ─────────────────────────────────────────────────
                    Spacer().frame(height: HopSpacing.xl)

                    Text("Create your account")
                        .font(HopFont.heading)
                        .foregroundColor(Color.hopTextPrimary)

                    Spacer().frame(height: HopSpacing.xs)

                    Text("Start your Hop journey")
                        .font(HopFont.body)
                        .foregroundColor(Color.hopTextSecondary)

                    Spacer().frame(height: HopSpacing.xl)

                    // ── Full name ──────────────────────────────────────────────
                    HopTextField(
                        label: "Full name",
                        placeholder: "Jane Doe",
                        text: $fullName,
                        keyboardType: .default,
                        isEnabled: !wrapper.state.isLoading
                    )

                    Spacer().frame(height: HopSpacing.md)

                    // ── Email ──────────────────────────────────────────────────
                    HopTextField(
                        label: "Email address",
                        placeholder: "jane@example.com",
                        text: $email,
                        keyboardType: .emailAddress,
                        isEnabled: !wrapper.state.isLoading
                    )

                    Spacer().frame(height: HopSpacing.md)

                    // ── Phone ──────────────────────────────────────────────────
                    HopTextField(
                        label: "Phone number",
                        placeholder: "+45 20 12 34 56",
                        text: $phone,
                        keyboardType: .phonePad,
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

                    // Helper text
                    Text("At least 8 characters")
                        .font(HopFont.caption)
                        .foregroundColor(
                            !password.isEmpty && !passwordValid ? Color.hopError : Color.hopTextSecondary
                        )
                        .padding(.top, HopSpacing.xxs)
                        .padding(.leading, HopSpacing.xs)

                    Spacer().frame(height: HopSpacing.lg)

                    // ── Terms checkbox ─────────────────────────────────────────
                    TermsCheckboxRow(
                        checked: $termsAccepted,
                        isEnabled: !wrapper.state.isLoading
                    )

                    Spacer().frame(height: HopSpacing.xl)

                    // ── Continue ───────────────────────────────────────────────
                    HopPrimaryButton(
                        title: "Continue",
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit
                    ) {
                        let parts = fullName.trimmingCharacters(in: .whitespaces)
                            .split(separator: " ", maxSplits: 1)
                        let firstName = parts.first.map(String.init) ?? fullName
                        let lastName  = parts.count > 1 ? String(parts[1]) : ""
                        wrapper.register(
                            firstName: firstName,
                            lastName: lastName,
                            email: email.trimmingCharacters(in: .whitespaces),
                            phone: phone.trimmingCharacters(in: .whitespaces).isEmpty ? nil
                                   : phone.trimmingCharacters(in: .whitespaces),
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
                        Text("Already have an account? ")
                            .font(HopFont.body)
                            .foregroundColor(Color.hopTextSecondary)
                        Button(action: onNavigateToLogin) {
                            Text("Log in")
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
            // Should not occur during fresh sign-up, but guard anyway.
            withAnimation { toastMessage = "Session expired. Please try again." }
            wrapper.clearError()
        default:
            break
        }
    }
}

// MARK: - TermsCheckboxRow

private struct TermsCheckboxRow: View {
    @Binding var checked: Bool
    var isEnabled: Bool = true

    var body: some View {
        Button {
            if isEnabled { checked.toggle() }
        } label: {
            HStack(alignment: .top, spacing: HopSpacing.sm) {
                ZStack {
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(checked ? Color.hopPrimaryLime : Color.hopTextSecondary, lineWidth: 1.5)
                        .frame(width: 20, height: 20)
                    if checked {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color.hopPrimaryLime)
                    }
                }
                .padding(.top, 1)

                Text("I agree to the ")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                + Text("Terms of Service")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopPrimaryLime)
                + Text(" and ")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                + Text("Privacy Policy")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopPrimaryLime)
            }
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

#Preview("Default") {
    SignUpView(
        onNavigateToHome: {},
        onNavigateToLogin: {}
    )
}

#Preview("Loading") {
    // Shown by injecting state — in real app wrapper drives this
    SignUpView(
        onNavigateToHome: {},
        onNavigateToLogin: {}
    )
}

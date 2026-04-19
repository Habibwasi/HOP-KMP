import SwiftUI

// ── ON-03b Forgot Password (stub) ─────────────────────────────────────────────
//
// Post-MVP: backend /auth/password/reset-request + /auth/password/reset.
// MVP: collects email, shows a generic "Check your email" confirmation so the
// flow is navigable end-to-end.  No ViewModel is wired — server call is a stub.

struct ForgotPasswordView: View {

    var onBack: () -> Void

    // ── Local state ───────────────────────────────────────────────────────────
    @State private var email      = ""
    @State private var submitted  = false
    @State private var isLoading  = false

    // ── Validation ────────────────────────────────────────────────────────────
    private var emailValid: Bool {
        email.contains("@") && email.split(separator: "@").last?.contains(".") == true
    }
    private var canSubmit: Bool { emailValid && !isLoading }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .all)

            if submitted {
                // ── Success state ─────────────────────────────────────────────
                successContent
            } else {
                // ── Form state ────────────────────────────────────────────────
                formContent
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                }
                .accessibilityLabel("Back")
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }

    // MARK: — Form ─────────────────────────────────────────────────────────────

    private var formContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: HopSpacing.xl)

                // ── Icon ──────────────────────────────────────────────────────
                ZStack {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.hopSurfaceElevated)
                        .frame(width: 64, height: 64)
                    Image(systemName: "lock.rotation")
                        .font(.system(size: 28, weight: .light))
                        .foregroundColor(Color.hopPrimaryLime)
                }
                .accessibilityHidden(true)

                Spacer().frame(height: HopSpacing.lg)

                // ── Header ────────────────────────────────────────────────────
                Text("Reset password")
                    .font(HopFont.headlineLarge())
                    .foregroundColor(Color.hopTextPrimary)

                Spacer().frame(height: HopSpacing.xs)

                Text("Enter the email address linked to your account and we'll send you a reset link.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                    .lineSpacing(4)
                    .fixedSize(horizontal: false, vertical: true)

                Spacer().frame(height: HopSpacing.xl)

                // ── Email field ───────────────────────────────────────────────
                HopTextField(
                    label: "Email address",
                    placeholder: "jane@example.com",
                    text: $email,
                    keyboardType: .emailAddress,
                    isEnabled: !isLoading,
                    submitLabel: .done
                )

                Spacer().frame(height: HopSpacing.lg)

                // ── Send button ───────────────────────────────────────────────
                HopPrimaryButton(
                    title: isLoading ? "Sending…" : "Send reset link",
                    isLoading: isLoading,
                    isEnabled: canSubmit
                ) {
                    sendResetLink()
                }

                Spacer().frame(height: HopSpacing.md)

                // ── Back to log in ────────────────────────────────────────────
                HStack {
                    Spacer()
                    Button(action: onBack) {
                        Text("Back to Log In")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopTextSecondary)
                            .underline()
                    }
                    Spacer()
                }

                Spacer().frame(height: HopSpacing.xl)
            }
            .padding(.horizontal, HopSpacing.md)
        }
    }

    // MARK: — Success ──────────────────────────────────────────────────────────

    private var successContent: some View {
        VStack(spacing: 0) {
            Spacer()

            // ── Illustration ──────────────────────────────────────────────────
            ZStack {
                Circle()
                    .fill(Color.hopPrimaryLime.opacity(0.12))
                    .frame(width: 96, height: 96)
                Image(systemName: "envelope.badge.fill")
                    .font(.system(size: 40, weight: .light))
                    .foregroundColor(Color.hopPrimaryLime)
            }
            .accessibilityHidden(true)

            Spacer().frame(height: HopSpacing.lg)

            Text("Check your email")
                .font(HopFont.headlineLarge())
                .foregroundColor(Color.hopTextPrimary)
                .multilineTextAlignment(.center)

            Spacer().frame(height: HopSpacing.xs)

            Text("We've sent a reset link to **\(email)**.\nCheck your inbox and follow the instructions.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, HopSpacing.lg)

            Spacer().frame(height: HopSpacing.xxl)

            // ── Back to log in ────────────────────────────────────────────────
            HopPrimaryButton(title: "Back to Log In", isEnabled: true) {
                onBack()
            }
            .padding(.horizontal, HopSpacing.md)

            Spacer().frame(height: HopSpacing.md)

            // ── Didn't receive – stub ─────────────────────────────────────────
            Button {
                // Re-show form to let user try a different email
                withAnimation(.easeInOut) { submitted = false }
            } label: {
                Text("Didn't receive it? Try again")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
                    .underline()
            }

            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
    }

    // MARK: — Actions ──────────────────────────────────────────────────────────

    private func sendResetLink() {
        guard canSubmit else { return }
        // MVP stub — no network call yet.  Simulate brief loading then success.
        Task { @MainActor in
            withAnimation { isLoading = true }
            try? await Task.sleep(nanoseconds: 800_000_000)
            withAnimation(.easeInOut) {
                isLoading = false
                submitted = true
            }
        }
    }
}

// MARK: — Previews ─────────────────────────────────────────────────────────────

#Preview("Default – empty form") {
    NavigationStack {
        ForgotPasswordView(onBack: {})
    }
}

#Preview("Form – email entered") {
    // Simulate the form with a valid email so the button is enabled.
    NavigationStack {
        ForgotPasswordView(onBack: {})
    }
}

#Preview("Success state") {
    // Drive the success state directly via a thin wrapper.
    NavigationStack {
        _ForgotPasswordSuccessPreview()
    }
}

private struct _ForgotPasswordSuccessPreview: View {
    var body: some View {
        // Reuse the private success path by constructing the public view and
        // using the "Didn't receive it?" path to reach the success screen.
        // For a direct preview we mirror the success layout inline.
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .all)
            VStack(spacing: 0) {
                Spacer()
                ZStack {
                    Circle()
                        .fill(Color.hopPrimaryLime.opacity(0.12))
                        .frame(width: 96, height: 96)
                    Image(systemName: "envelope.badge.fill")
                        .font(.system(size: 40, weight: .light))
                        .foregroundColor(Color.hopPrimaryLime)
                }
                Spacer().frame(height: HopSpacing.lg)
                Text("Check your email")
                    .font(HopFont.headlineLarge())
                    .foregroundColor(Color.hopTextPrimary)
                Spacer().frame(height: HopSpacing.xs)
                Text("We've sent a reset link to **jane@example.com**.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, HopSpacing.lg)
                Spacer()
            }
        }
    }
}

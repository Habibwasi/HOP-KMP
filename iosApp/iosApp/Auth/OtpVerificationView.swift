import SwiftUI
import Shared

private let otpLength = 6
private let resendCountdown = 45

// ── ON-04 OTP Verification ────────────────────────────────────────────────────

struct OtpVerificationView: View {

    @StateObject private var wrapper = AuthViewModelWrapper()

    /// Phone number the code was sent to (from NavigateToOtpVerification effect).
    let phone: String

    var onNavigateToHome: () -> Void

    // ── Local state ───────────────────────────────────────────────────────────
    @State private var digits      = Array(repeating: "", count: otpLength)
    @State private var countdown   = resendCountdown
    @State private var toastMessage: String? = nil

    @FocusState private var focusedIndex: Int?

    private var code: String { digits.joined() }
    private var isComplete: Bool { code.count == otpLength }
    private var canSubmit: Bool { isComplete && !wrapper.state.isLoading }

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: HopSpacing.xl)

                // ── Header ─────────────────────────────────────────────────────
                Text("Verify your number")
                    .font(HopFont.heading)
                    .foregroundColor(Color.hopTextPrimary)

                Spacer().frame(height: HopSpacing.xs)

                Text("We sent a 6-digit code to \(phone)")
                    .font(HopFont.body)
                    .foregroundColor(Color.hopTextSecondary)

                Spacer().frame(height: HopSpacing.xl)

                // ── OTP digit boxes ────────────────────────────────────────────
                HStack(spacing: HopSpacing.sm) {
                    ForEach(0..<otpLength, id: \.self) { index in
                        OtpDigitBox(
                            value: $digits[index],
                            isError: wrapper.state.error != nil,
                            isEnabled: !wrapper.state.isLoading,
                            isFocused: focusedIndex == index,
                            onTap: { focusedIndex = index },
                            onChange: { newVal in
                                handleDigitChange(index: index, newValue: newVal)
                            },
                            onBackspace: {
                                handleBackspace(index: index)
                            }
                        )
                    }
                }

                // ── Inline error ───────────────────────────────────────────────
                if let error = wrapper.state.error {
                    Spacer().frame(height: HopSpacing.sm)
                    Text(error)
                        .font(HopFont.caption)
                        .foregroundColor(Color.hopError)
                }

                Spacer().frame(height: HopSpacing.lg)

                // ── Resend timer / link ────────────────────────────────────────
                if countdown > 0 {
                    Text("Resend code in 0:\(String(format: "%02d", countdown))")
                        .font(HopFont.body)
                        .foregroundColor(Color.hopTextSecondary)
                } else {
                    Button {
                        resetInput()
                        wrapper.sendOtp(phone: phone)
                    } label: {
                        Text("Resend code")
                            .font(HopFont.body)
                            .fontWeight(.semibold)
                            .foregroundColor(Color.hopPrimaryLime)
                    }
                    .disabled(wrapper.state.isLoading)
                }

                Spacer().frame(height: HopSpacing.xl)

                // ── Verify button ──────────────────────────────────────────────
                HopPrimaryButton(
                    title: "Verify",
                    isLoading: wrapper.state.isLoading,
                    isEnabled: canSubmit
                ) {
                    wrapper.verifyOtp(phone: phone, code: code)
                }

                Spacer()
            }
            .padding(.horizontal, HopSpacing.md)

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
        .onTapGesture { focusedIndex = nil }
        .task { wrapper.startObserving() }
        .task { await runCountdown() }
        .task {
            for await effect in wrapper.viewModel.effect {
                await handleEffect(effect)
            }
        }
        // Auto-submit on code complete
        .onChange(of: code) { newCode in
            if newCode.count == otpLength {
                wrapper.verifyOtp(phone: phone, code: newCode)
            }
            if wrapper.state.error != nil && !newCode.isEmpty {
                wrapper.clearError()
            }
        }
        // Focus first box on appear
        .onAppear { focusedIndex = 0 }
    }

    // MARK: - Digit input handling

    private func handleDigitChange(index: Int, newValue: String) {
        let cleaned = newValue.filter { $0.isNumber }
        if cleaned.isEmpty {
            digits[index] = ""
        } else if cleaned.count == 1 {
            digits[index] = cleaned
            if index < otpLength - 1 { focusedIndex = index + 1 }
        } else {
            // Paste: distribute
            let chars = Array(cleaned)
            for (offset, ch) in chars.prefix(otpLength - index).enumerated() {
                digits[index + offset] = String(ch)
            }
            let nextFocus = min(index + chars.count, otpLength - 1)
            focusedIndex = nextFocus
        }
    }

    private func handleBackspace(index: Int) {
        if digits[index].isEmpty && index > 0 {
            digits[index - 1] = ""
            focusedIndex = index - 1
        } else {
            digits[index] = ""
        }
    }

    private func resetInput() {
        digits = Array(repeating: "", count: otpLength)
        countdown = resendCountdown
        focusedIndex = 0
        wrapper.clearError()
    }

    // MARK: - Tasks

    private func runCountdown() async {
        while countdown > 0 {
            try? await Task.sleep(nanoseconds: 1_000_000_000)
            countdown -= 1
        }
    }

    @MainActor
    private func handleEffect(_ effect: AuthEffect) async {
        switch effect {
        case is AuthEffect.NavigateToHome:
            onNavigateToHome()
        case let snack as AuthEffect.ShowSnackbar:
            withAnimation { toastMessage = snack.message }
            wrapper.clearError()
        case is AuthEffect.SessionExpired:
            // Token refreshed failed mid-OTP — surface to user so they can restart.
            withAnimation { toastMessage = "Session expired. Please start again." }
            resetInput()
            wrapper.clearError()
        default:
            break
        }
    }
}

// MARK: - OtpDigitBox

private struct OtpDigitBox: View {
    @Binding var value: String
    let isError: Bool
    let isEnabled: Bool
    let isFocused: Bool
    let onTap: () -> Void
    let onChange: (String) -> Void
    let onBackspace: () -> Void

    private var borderColor: Color {
        if isError && !value.isEmpty { return Color.hopError }
        if isError { return Color.hopError.opacity(0.5) }
        if !value.isEmpty { return Color.hopPrimaryLime }
        return Color.hopTextSecondary.opacity(0.3)
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.hopSurfaceElevated)
            RoundedRectangle(cornerRadius: 12)
                .stroke(borderColor, lineWidth: 1.5)

            if value.isEmpty {
                // Cursor blink indicator when focused
                if isFocused {
                    Rectangle()
                        .fill(Color.hopPrimaryLime)
                        .frame(width: 1.5, height: 24)
                }
            } else {
                Text(value)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(Color.hopTextPrimary)
            }

            // Invisible text field for keyboard input
            OtpHiddenTextField(
                value: $value,
                isEnabled: isEnabled,
                onChange: onChange,
                onBackspace: onBackspace
            )
            .opacity(0.01)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 56)
        .contentShape(Rectangle())
        .onTapGesture { onTap() }
    }
}

// MARK: - OtpHiddenTextField

/// Invisible UITextField wrapper that forwards single-digit input and backspace.
private struct OtpHiddenTextField: UIViewRepresentable {
    @Binding var value: String
    let isEnabled: Bool
    let onChange: (String) -> Void
    let onBackspace: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> BackspaceTextField {
        let field = BackspaceTextField()
        field.delegate = context.coordinator
        field.keyboardType = .numberPad
        field.textContentType = .oneTimeCode
        field.isUserInteractionEnabled = isEnabled
        return field
    }

    func updateUIView(_ uiView: BackspaceTextField, context: Context) {
        uiView.isUserInteractionEnabled = isEnabled
        // Sync value only when different to avoid cursor jump
        if uiView.text != value { uiView.text = value }
    }

    class Coordinator: NSObject, UITextFieldDelegate {
        let parent: OtpHiddenTextField

        init(_ parent: OtpHiddenTextField) { self.parent = parent }

        func textField(
            _ textField: UITextField,
            shouldChangeCharactersIn range: NSRange,
            replacementString string: String
        ) -> Bool {
            let current = textField.text ?? ""
            let updated = (current as NSString).replacingCharacters(in: range, with: string)
            parent.onChange(updated)
            return false
        }
    }
}

/// UITextField subclass that intercepts the delete key when the field is empty.
final class BackspaceTextField: UITextField {
    var onDeleteBackward: (() -> Void)?

    override func deleteBackward() {
        if text?.isEmpty == true { onDeleteBackward?() }
        super.deleteBackward()
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

#Preview("Default") {
    OtpVerificationView(
        phone: "+45 20 12 34 56",
        onNavigateToHome: {}
    )
}

#Preview("With digits") {
    OtpVerificationView(
        phone: "+45 20 12 34 56",
        onNavigateToHome: {}
    )
}

#Preview("Error state") {
    OtpVerificationView(
        phone: "+45 20 12 34 56",
        onNavigateToHome: {}
    )
}

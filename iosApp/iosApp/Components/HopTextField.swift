import SwiftUI

// MARK: - HopTextField

/// Labelled text input — mirrors `HopTextField` in composeApp.
///
/// Features:
/// - Label rendered above the field
/// - Lime focus-ring border
/// - Red error message below with animated entrance
/// - Optional trailing Show/Hide toggle for secure fields
struct HopTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String

    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    var isEnabled: Bool = true
    var errorMessage: String? = nil
    var trailingLabel: String? = nil
    var trailingAction: (() -> Void)? = nil
    var submitLabel: SubmitLabel = .next
    /// Use light theme (white bg + dark text) for auth screens.
    var lightSurface: Bool = false

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xxs) {
            // ── Label ─────────────────────────────────────────────────────────
            Text(label)
                .font(HopFont.labelSmall())
                .foregroundColor(secondaryColor)

            // ── Input row ─────────────────────────────────────────────────────
            HStack(spacing: 0) {
                fieldContent
                    .font(HopFont.bodyMedium())
                    .foregroundColor(primaryColor)
                    .tint(accentColor)
                    .focused($isFocused)
                    .disabled(!isEnabled)
                    .submitLabel(submitLabel)

                if let trailingLabel, let trailingAction {
                    Button(action: trailingAction) {
                        Text(trailingLabel)
                            .font(HopFont.labelSmall())
                            .foregroundColor(accentColor)
                            .padding(.leading, HopSpacing.xs)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .frame(height: 52)
            .background(isEnabled ? inputBg : inputBg.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(borderColor, lineWidth: 1.5)
                    .animation(.easeInOut(duration: 0.15), value: isFocused)
                    .animation(.easeInOut(duration: 0.15), value: errorMessage)
            )

            // ── Error message ─────────────────────────────────────────────────
            if let errorMessage {
                HStack(spacing: 4) {
                    Image(systemName: "exclamationmark.circle")
                        .font(.system(size: 12))
                    Text(errorMessage)
                        .font(HopFont.bodySmall())
                }
                .foregroundColor(Color.hopError)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: errorMessage != nil)
    }

    // MARK: - Helpers

    @ViewBuilder
    private var fieldContent: some View {
        if isSecure {
            SecureField(placeholder, text: $text)
        } else {
            TextField(placeholder, text: $text)
                .keyboardType(keyboardType)
        }
    }

    private var primaryColor:   Color { lightSurface ? Color.hopAuthTextPrimary   : Color.hopTextPrimary }
    private var secondaryColor: Color { lightSurface ? Color.hopAuthTextSecondary : Color.hopTextSecondary }
    private var accentColor:    Color { lightSurface ? Color.hopAuthAccent        : Color.hopPrimaryLime }
    private var inputBg:        Color { lightSurface ? Color.hopAuthInputSurface  : Color.hopSurfaceElevated }

    private var borderColor: Color {
        if errorMessage != nil { return Color.hopError }
        if isFocused { return accentColor }
        return lightSurface ? Color.hopAuthInputBorder : .clear
    }
}

// MARK: - Previews

#Preview("HopTextField — States") {
    @Previewable @State var email = ""
    @Previewable @State var password = "secret"
    @Previewable @State var phone = "abc"
    @Previewable @State var showPassword = false

    VStack(spacing: HopSpacing.md) {
        HopTextField(
            label: "Email address",
            placeholder: "jane@example.com",
            text: $email,
            keyboardType: .emailAddress
        )
        HopTextField(
            label: "Password",
            placeholder: "••••••••",
            text: $password,
            isSecure: !showPassword,
            trailingLabel: showPassword ? "Hide" : "Show",
            trailingAction: { showPassword.toggle() }
        )
        HopTextField(
            label: "Phone",
            placeholder: "+45 12 34 56 78",
            text: $phone,
            keyboardType: .phonePad,
            errorMessage: phone.count < 8 ? "Please enter a valid Danish phone number" : nil
        )
        HopTextField(
            label: "Disabled field",
            placeholder: "Not editable",
            text: .constant(""),
            isEnabled: false
        )
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

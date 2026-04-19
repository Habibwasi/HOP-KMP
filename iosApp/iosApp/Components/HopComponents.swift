import SwiftUI

// ── Shared UI components ──────────────────────────────────────────────────────
// Mirrors HopButton / HopTextField from composeApp.

// MARK: - HopPrimaryButton

struct HopPrimaryButton: View {
    let title: String
    var isLoading: Bool = false
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopSurface))
                        .scaleEffect(0.9)
                } else {
                    Text(title)
                        .font(HopFont.label)
                        .fontWeight(.semibold)
                        .foregroundColor(isEnabled ? Color.hopSurface : Color.hopTextSecondary)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(isEnabled && !isLoading ? Color.hopPrimaryLime : Color.hopSurfaceElevated)
            .cornerRadius(12)
        }
        .disabled(!isEnabled || isLoading)
        .animation(.easeInOut(duration: 0.15), value: isEnabled)
    }
}

// MARK: - HopGhostButton

struct HopGhostButton: View {
    let title: String
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(HopFont.label)
                .fontWeight(.semibold)
                .foregroundColor(isEnabled ? Color.hopTextPrimary : Color.hopTextSecondary)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isEnabled ? Color.hopTextSecondary : Color.hopSurfaceElevated, lineWidth: 1)
                )
        }
        .disabled(!isEnabled)
    }
}

// MARK: - HopTextField

struct HopTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    var isEnabled: Bool = true
    var trailingAction: (() -> Void)? = nil
    var trailingLabel: String? = nil
    var submitLabel: SubmitLabel = .next

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xxs) {
            Text(label)
                .font(HopFont.labelSmall())
                .foregroundColor(Color.hopTextSecondary)

            HStack(spacing: 0) {
                Group {
                    if isSecure {
                        SecureField(placeholder, text: $text)
                    } else {
                        TextField(placeholder, text: $text)
                            .keyboardType(keyboardType)
                    }
                }
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextPrimary)
                .tint(Color.hopPrimaryLime)
                .focused($isFocused)
                .disabled(!isEnabled)
                .submitLabel(submitLabel)
                .accentColor(Color.hopPrimaryLime)

                if let trailingLabel = trailingLabel, let trailingAction = trailingAction {
                    Button(action: trailingAction) {
                        Text(trailingLabel)
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopPrimaryLime)
                            .padding(.leading, HopSpacing.xs)
                    }
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .frame(height: 52)
            .background(Color.hopSurfaceElevated)
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(
                        isFocused ? Color.hopPrimaryLime : Color.clear,
                        lineWidth: 1.5
                    )
            )
        }
    }
}

// MARK: - HopSnackbar

struct HopToast: View {
    let message: String

    var body: some View {
        Text(message)
            .font(HopFont.bodyMedium())
            .foregroundColor(Color.hopTextPrimary)
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.sm)
            .background(Color.hopSurfaceElevated)
            .cornerRadius(10)
            .shadow(color: .black.opacity(0.25), radius: 8, y: 4)
    }
}

// MARK: - Previews

#Preview("Buttons") {
    VStack(spacing: 16) {
        HopPrimaryButton(title: "Get started", action: {})
        HopPrimaryButton(title: "Loading…", isLoading: true, action: {})
        HopPrimaryButton(title: "Disabled", isEnabled: false, action: {})
        HopGhostButton(title: "Continue with MitID", action: {})
        HopGhostButton(title: "Disabled ghost", isEnabled: false, action: {})
    }
    .padding()
    .background(Color.hopSurface)
}

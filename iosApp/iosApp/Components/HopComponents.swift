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

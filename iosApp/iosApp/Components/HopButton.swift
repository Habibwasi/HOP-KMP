import SwiftUI

// MARK: - Variant

enum HopButtonVariant {
    case primary
    case ghost
    case destructive
}

// MARK: - HopButton

/// Unified button component — mirrors `HopButton` + `HopButtonVariant` in composeApp.
///
/// Usage:
/// ```swift
/// HopButton(text: "Log In", variant: .primary, action: signIn)
/// HopButton(text: "Cancel Trip", variant: .destructive, isLoading: isWorking, action: cancel)
/// ```
struct HopButton: View {
    let text: String
    var variant: HopButtonVariant = .primary
    var isLoading: Bool = false
    var isEnabled: Bool = true
    /// Use light theme (dark text on white) for ghost on auth screens.
    var lightSurface: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: spinnerColor))
                        .scaleEffect(0.9)
                } else {
                    Text(text)
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(foregroundColor)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(backgroundColor)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(borderColor, lineWidth: 1.5)
            )
            .opacity((!isEnabled && !isLoading) ? 0.5 : 1.0)
        }
        .disabled(!isEnabled || isLoading)
        .buttonStyle(.plain)
        .animation(.easeInOut(duration: 0.15), value: isEnabled)
        .animation(.easeInOut(duration: 0.15), value: isLoading)
    }

    // MARK: - Colour helpers

    private var backgroundColor: Color {
        switch variant {
        case .primary:
            return (isEnabled && !isLoading) ? Color.hopPrimaryLime : Color.hopSurfaceElevated
        case .ghost, .destructive:
            return .clear
        }
    }

    private var foregroundColor: Color {
        switch variant {
        case .primary:
            return (isEnabled && !isLoading) ? Color.hopSurface : Color.hopTextSecondary
        case .ghost:
            let primary = lightSurface ? Color.hopAuthTextPrimary : Color.hopTextPrimary
            let secondary = lightSurface ? Color.hopAuthTextSecondary : Color.hopTextSecondary
            return isEnabled ? primary : secondary
        case .destructive:
            return isEnabled ? Color.hopError : Color.hopTextSecondary
        }
    }

    private var borderColor: Color {
        switch variant {
        case .primary:
            return .clear
        case .ghost:
            let line = lightSurface ? Color.hopAuthInputBorder : Color.hopTextSecondary
            let dim  = lightSurface ? Color.hopAuthInputBorder.opacity(0.4) : Color.hopSurfaceElevated
            return isEnabled ? line : dim
        case .destructive:
            return isEnabled ? Color.hopError : Color.hopSurfaceElevated
        }
    }

    private var spinnerColor: Color {
        switch variant {
        case .primary:     return Color.hopSurface
        case .ghost:       return lightSurface ? Color.hopAuthTextPrimary : Color.hopTextPrimary
        case .destructive: return Color.hopError
        }
    }
}

// MARK: - Previews

#Preview("HopButton — Primary") {
    VStack(spacing: HopSpacing.md) {
        HopButton(text: "Log In", variant: .primary, action: {})
        HopButton(text: "Loading…", variant: .primary, isLoading: true, action: {})
        HopButton(text: "Disabled", variant: .primary, isEnabled: false, action: {})
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("HopButton — Ghost") {
    VStack(spacing: HopSpacing.md) {
        HopButton(text: "Continue with MitID", variant: .ghost, action: {})
        HopButton(text: "Loading…", variant: .ghost, isLoading: true, action: {})
        HopButton(text: "Disabled", variant: .ghost, isEnabled: false, action: {})
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("HopButton — Destructive") {
    VStack(spacing: HopSpacing.md) {
        HopButton(text: "Cancel Trip", variant: .destructive, action: {})
        HopButton(text: "Loading…", variant: .destructive, isLoading: true, action: {})
        HopButton(text: "Disabled", variant: .destructive, isEnabled: false, action: {})
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

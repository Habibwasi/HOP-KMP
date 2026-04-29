import SwiftUI

/// Design tokens — mirrors `HopColors` in composeApp.
/// All colour literals match Agent.md palette.
extension Color {
    // ── Primary ──────────────────────────────────────────────────────────────
    static let hopPrimaryLime  = Color(hex: 0xC8F135)
    static let hopPrimaryGreen = Color(hex: 0x1DB954)

    // ── Backgrounds ──────────────────────────────────────────────────────────
    static let hopBackground      = Color(hex: 0xFFFFFF)
    static let hopSurface         = Color(hex: 0x1A1A1A)
    static let hopSurfaceElevated = Color(hex: 0x242424)

    // ── Text ─────────────────────────────────────────────────────────────────
    static let hopTextPrimary   = Color(hex: 0xFFFFFF)
    static let hopTextSecondary = Color(hex: 0xB3B3B3)

    // ── Semantic ──────────────────────────────────────────────────────────────
    static let hopSuccess = Color(hex: 0x22C55E)
    static let hopWarning = Color(hex: 0xFBBF24)
    static let hopError   = Color(hex: 0xEF4444)

    // ── Auth (light theme) ───────────────────────────────────────────────────
    static let hopAuthTextPrimary    = Color(hex: 0x0D0D0D)
    static let hopAuthTextSecondary  = Color(hex: 0x5F6368)
    static let hopAuthAccent         = Color(hex: 0x167A30)
    static let hopAuthInputSurface   = Color(hex: 0xF1F3F4)
    static let hopAuthInputBorder    = Color(hex: 0xD1D5DB)

    // ── Helpers ───────────────────────────────────────────────────────────────
    init(hex: UInt, opacity: Double = 1) {
        self.init(
            .sRGB,
            red:   Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >>  8) & 0xFF) / 255,
            blue:  Double( hex        & 0xFF) / 255,
            opacity: opacity
        )
    }
}

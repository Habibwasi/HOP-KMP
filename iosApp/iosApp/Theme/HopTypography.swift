import SwiftUI

/// Typography tokens — Syne for headings, Inter for body.
/// Mirrors the font decisions in Agent.md.
struct HopFont {
    // ── Headings (Syne) ───────────────────────────────────────────────────────
    static func displayLarge  (weight: Font.Weight = .bold)      -> Font { .custom("Syne-Bold",        size: 32) }
    static func headlineLarge (weight: Font.Weight = .bold)      -> Font { .custom("Syne-Bold",        size: 28) }
    static func headlineMedium(weight: Font.Weight = .semibold)  -> Font { .custom("Syne-SemiBold",    size: 24) }
    static func headlineSmall (weight: Font.Weight = .semibold)  -> Font { .custom("Syne-SemiBold",    size: 20) }

    // ── Body (Inter) ──────────────────────────────────────────────────────────
    static func bodyLarge (weight: Font.Weight = .regular) -> Font { .custom("Inter-Regular",    size: 16) }
    static func bodyMedium(weight: Font.Weight = .regular) -> Font { .custom("Inter-Regular",    size: 14) }
    static func bodySmall (weight: Font.Weight = .regular) -> Font { .custom("Inter-Regular",    size: 12) }
    static func labelMedium(weight: Font.Weight = .medium) -> Font { .custom("Inter-Medium",     size: 14) }
    static func labelSmall (weight: Font.Weight = .medium) -> Font { .custom("Inter-Medium",     size: 12) }

    // ── Fallback helpers (system font when custom not embedded) ───────────────
    static let heading    = Font.system(size: 24, weight: .bold,     design: .default)
    static let subheading = Font.system(size: 20, weight: .semibold, design: .default)
    static let body       = Font.system(size: 16, weight: .regular,  design: .default)
    static let caption    = Font.system(size: 12, weight: .regular,  design: .default)
    static let label      = Font.system(size: 14, weight: .medium,   design: .default)
}

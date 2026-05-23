import SwiftUI

/// Typography tokens — Syne for headings, Inter for body.
/// Mirrors the font decisions in Agent.md.
///
/// Pass `weight:` to select the correct font variant.
/// Do NOT chain `.fontWeight()` on top of these — custom font descriptors
/// cannot have their weight remapped at runtime (causes UICTFontDescriptor warnings).
struct HopFont {

    // MARK: — Internal helpers

    /// Maps a SwiftUI weight to an Inter postfix.
    private static func interName(_ weight: Font.Weight) -> String {
        switch weight {
        case .ultraLight, .thin, .light: return "Inter-Light"
        case .regular:                   return "Inter-Regular"
        case .medium:                    return "Inter-Medium"
        case .semibold:                  return "Inter-SemiBold"
        case .bold, .heavy, .black:      return "Inter-Bold"
        default:                         return "Inter-Regular"
        }
    }

    /// Maps a SwiftUI weight to a Syne postfix.
    private static func syneName(_ weight: Font.Weight) -> String {
        switch weight {
        case .regular:                   return "Syne-Regular"
        case .medium:                    return "Syne-Medium"
        case .semibold:                  return "Syne-SemiBold"
        case .bold, .heavy, .black:      return "Syne-Bold"
        default:                         return "Syne-SemiBold"
        }
    }

    // MARK: — Headings (Syne)

    static func displayLarge  (weight: Font.Weight = .bold)     -> Font { .custom(syneName(weight),  size: 32) }
    static func headlineLarge (weight: Font.Weight = .bold)     -> Font { .custom(syneName(weight),  size: 28) }
    static func headlineMedium(weight: Font.Weight = .semibold) -> Font { .custom(syneName(weight),  size: 24) }
    static func headlineSmall (weight: Font.Weight = .semibold) -> Font { .custom(syneName(weight),  size: 20) }

    // MARK: — Body / Label (Inter)

    static func bodyLarge  (weight: Font.Weight = .regular) -> Font { .custom(interName(weight), size: 16) }
    static func bodyMedium (weight: Font.Weight = .regular) -> Font { .custom(interName(weight), size: 14) }
    static func bodySmall  (weight: Font.Weight = .regular) -> Font { .custom(interName(weight), size: 12) }
    static func labelLarge (weight: Font.Weight = .medium)  -> Font { .custom(interName(weight), size: 16) }
    static func labelMedium(weight: Font.Weight = .medium)  -> Font { .custom(interName(weight), size: 14) }
    static func labelSmall (weight: Font.Weight = .medium)  -> Font { .custom(interName(weight), size: 12) }

    // MARK: — Fallback helpers (system font when custom not embedded)

    static let heading    = Font.system(size: 24, weight: .bold,     design: .default)
    static let subheading = Font.system(size: 20, weight: .semibold, design: .default)
    static let body       = Font.system(size: 16, weight: .regular,  design: .default)
    static let caption    = Font.system(size: 12, weight: .regular,  design: .default)
    static let label      = Font.system(size: 14, weight: .medium,   design: .default)

    // MARK: — Mono (for monetary displays)

    /// Monospaced digits used for price / payout displays so digits don't jitter.
    static func mono(size: CGFloat, weight: Font.Weight = .bold) -> Font {
        .system(size: size, weight: weight, design: .monospaced)
    }
}

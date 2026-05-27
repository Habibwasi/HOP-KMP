import SwiftUI

/// PA-01 — Compact greeting banner shown above the search card.
/// Mirrors composeApp `GreetingBanner.kt`. Transparent background — host
/// supplies the lime → background gradient behind it.
struct GreetingBanner: View {
    let firstName: String?
    var subtitle: String? = nil

    var body: some View {
        let hour = Calendar.current.component(.hour, from: Date())
        let greeting: String = {
            switch hour {
            case 5...11:  return "Good morning"
            case 12...16: return "Good afternoon"
            case 17...21: return "Good evening"
            default:      return "Travelling late?"
            }
        }()
        let resolvedName: String? = (firstName?.isEmpty == false) ? firstName : nil
        let resolvedSubtitle = subtitle ?? defaultSubtitle(hour: hour)

        HStack(alignment: .center, spacing: HopSpacing.sm) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.55))
                    .frame(width: 44, height: 44)
                Text(String((resolvedName?.first?.uppercased() ?? "H")))
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
            }

            VStack(alignment: .leading, spacing: 2) {
                if let name = resolvedName {
                    Text("\(greeting), \(name) 👋")
                        .font(HopFont.headlineSmall(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                } else {
                    Text("\(greeting) 👋")
                        .font(HopFont.headlineSmall(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
                Text(resolvedSubtitle)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextPrimary.opacity(0.6))
            }
            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.top, HopSpacing.lg)
        .padding(.bottom, HopSpacing.sm)
    }

    private func defaultSubtitle(hour: Int) -> String {
        switch hour {
        case 5...9:   return "Where are you headed today?"
        case 10...15: return "Find a ride for the rest of your day."
        case 16...20: return "Heading home? See who's going your way."
        default:      return "Late-night ride? We'll find you one."
        }
    }
}

extension HopFont {
    /// Convenience matching Material titleMedium (≈16pt SemiBold by default).
    static func titleMedium(weight: Font.Weight = .semibold) -> Font {
        .custom(weight == .semibold ? "Inter-SemiBold" : "Inter-Medium", size: 16)
    }
    /// Convenience matching Material titleLarge (≈22pt SemiBold by default).
    static func titleLarge(weight: Font.Weight = .semibold) -> Font {
        .custom(weight == .semibold ? "Syne-SemiBold" : "Syne-Medium", size: 22)
    }
    /// Convenience matching Material titleSmall (≈14pt SemiBold by default).
    static func titleSmall(weight: Font.Weight = .semibold) -> Font {
        .custom(weight == .semibold ? "Inter-SemiBold" : "Inter-Medium", size: 14)
    }
}

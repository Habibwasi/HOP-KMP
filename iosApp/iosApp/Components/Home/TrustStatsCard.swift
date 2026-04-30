import SwiftUI

/// PA-01 — Three-stat trust card: rating, completed trips, CO₂ saved.
/// Mirrors composeApp `TrustStatsCard.kt`.
struct TrustStatsCard: View {
    /// Rating × 10 (e.g. 48 = 4.8★). Use 0 for "—".
    let ratingTimes10: Int
    let completedTrips: Int
    let co2SavedKg: Int

    var body: some View {
        HStack(alignment: .center, spacing: 0) {
            StatCell(
                systemImage: "star.fill",
                iconColor: Color.hopWarning,
                value: ratingDisplay,
                label: "Rating"
            )
            StatDivider()
            StatCell(
                systemImage: "car.fill",
                iconColor: Color.hopPrimaryGreen,
                value: "\(completedTrips)",
                label: "Trips"
            )
            StatDivider()
            StatCell(
                systemImage: "leaf.fill",
                iconColor: Color.hopCo2Accent,
                value: "\(co2SavedKg)",
                label: "kg CO₂ saved"
            )
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, HopSpacing.md)
        .padding(.horizontal, HopSpacing.sm)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var ratingDisplay: String {
        guard ratingTimes10 > 0 else { return "—" }
        return "\(ratingTimes10 / 10).\(ratingTimes10 % 10)"
    }
}

private struct StatCell: View {
    let systemImage: String
    let iconColor: Color
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: systemImage)
                .font(.system(size: 18))
                .foregroundColor(iconColor)
            Text(value)
                .font(HopFont.titleMedium())
                .foregroundColor(Color.hopAuthTextPrimary)
            Text(label)
                .font(HopFont.labelSmall())
                .foregroundColor(Color.hopAuthTextSecondary)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct StatDivider: View {
    var body: some View {
        Rectangle()
            .fill(Color.hopCardBorder)
            .frame(width: 1, height: 36)
    }
}

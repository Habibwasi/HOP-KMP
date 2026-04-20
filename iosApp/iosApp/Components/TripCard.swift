import SwiftUI

// MARK: - TripCardData

/// Display model for TripCard — all strings are pre-formatted for display.
/// Monetary amounts follow the project rule: stored as Int in øre, displayed as DKK.
struct TripCardData {
    let driverName: String
    let driverImageURL: URL?
    let driverRating: Double   // 0.0–5.0
    let reviewCount: Int
    let originName: String
    let destName: String
    /// Pre-formatted departure string, e.g. "Mon 07:30" or "12 May, 08:00"
    let departsAt: String
    let badgeStatus: HopBadgeStatus
    /// Price in øre (Int). Displayed as DKK (divided by 100).
    let priceOerePerSeat: Int
}

// MARK: - TripCard

/// Surfaced trip summary card — tappable, used in feed and search results.
///
/// Layout:
/// ```
/// [ Avatar ] Name          [Badge]
///            ★★★★½ (42)
/// ─────────────────────────────────
/// ● Aarhus C               Mon 07:30
/// ● København H            DKK 204
/// ```
struct TripCard: View {
    let data: TripCardData
    var onTap: (() -> Void)? = nil

    var body: some View {
        Button(action: { onTap?() }) {
            VStack(alignment: .leading, spacing: HopSpacing.sm) {
                driverRow
                Divider()
                    .overlay(Color.hopSurface.opacity(0.5))
                routeRow
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.hopSurface.opacity(0.4), lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(accessibilityDescription)
        .accessibilityHint("Double-tap to view trip details")
    }

    // MARK: - Driver row

    private var driverRow: some View {
        HStack(spacing: HopSpacing.sm) {
            HopAvatar(
                name: data.driverName,
                imageURL: data.driverImageURL,
                size: .medium
            )

            VStack(alignment: .leading, spacing: 2) {
                Text(data.driverName)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                    .lineLimit(1)

                StarRatingDisplay(
                    rating: data.driverRating,
                    count: data.reviewCount,
                    starSize: 12
                )
            }

            Spacer(minLength: 0)

            StatusBadge(status: data.badgeStatus)
        }
    }

    // MARK: - Route row

    private var routeRow: some View {
        HStack(alignment: .top, spacing: HopSpacing.xs) {
            // Route stops
            VStack(alignment: .leading, spacing: HopSpacing.xs) {
                routeStop(name: data.originName, isOrigin: true)
                routeStop(name: data.destName,   isOrigin: false)
            }

            Spacer(minLength: HopSpacing.md)

            // Time + price
            VStack(alignment: .trailing, spacing: 2) {
                Text(data.departsAt)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)

                Text(priceFormatted)
                    .font(HopFont.headlineSmall(weight: .bold))
                    .foregroundColor(Color.hopPrimaryLime)
            }
        }
    }

    private func routeStop(name: String, isOrigin: Bool) -> some View {
        HStack(spacing: HopSpacing.xs) {
            Circle()
                .fill(isOrigin ? Color.hopPrimaryLime : Color.hopPrimaryGreen)
                .frame(width: 8, height: 8)
            Text(name)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextPrimary)
                .lineLimit(1)
        }
    }

    // MARK: - Helpers

    private var priceFormatted: String {
        "DKK \(data.priceOerePerSeat / 100)"
    }

    private var accessibilityDescription: String {
        "\(data.driverName), \(String(format: "%.1f", data.driverRating)) stars. " +
        "\(data.originName) to \(data.destName). " +
        "Departs \(data.departsAt). \(priceFormatted) per seat. " +
        "Status: \(data.badgeStatus.label)."
    }
}

// MARK: - Previews

#Preview("TripCard — Confirmed") {
    TripCard(data: TripCardData(
        driverName: "Anders Nielsen",
        driverImageURL: nil,
        driverRating: 4.8,
        reviewCount: 42,
        originName: "Aarhus C",
        destName: "København H",
        departsAt: "Mon 07:30",
        badgeStatus: .confirmed,
        priceOerePerSeat: 20_386
    ))
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("TripCard — Pending") {
    TripCard(data: TripCardData(
        driverName: "Maria Larsen",
        driverImageURL: nil,
        driverRating: 3.5,
        reviewCount: 12,
        originName: "Odense Banegård",
        destName: "Roskilde",
        departsAt: "Fri 08:00",
        badgeStatus: .pending,
        priceOerePerSeat: 8_500
    ))
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("TripCard — Feed") {
    ScrollView {
        VStack(spacing: HopSpacing.md) {
            TripCard(data: TripCardData(
                driverName: "Anders Nielsen",
                driverImageURL: nil,
                driverRating: 4.8,
                reviewCount: 42,
                originName: "Aarhus C",
                destName: "København H",
                departsAt: "Mon 07:30",
                badgeStatus: .confirmed,
                priceOerePerSeat: 20_386
            ))
            TripCard(data: TripCardData(
                driverName: "Sofia Andersen",
                driverImageURL: nil,
                driverRating: 4.2,
                reviewCount: 7,
                originName: "Vejle",
                destName: "Fredericia",
                departsAt: "Tue 09:15",
                badgeStatus: .modelA,
                priceOerePerSeat: 4_200
            ))
            TripCard(data: TripCardData(
                driverName: "Lars Møller",
                driverImageURL: nil,
                driverRating: 5.0,
                reviewCount: 103,
                originName: "Aalborg",
                destName: "Aarhus C",
                departsAt: "Wed 06:45",
                badgeStatus: .cancelled,
                priceOerePerSeat: 11_000
            ))
        }
        .padding(HopSpacing.md)
    }
    .background(Color.hopSurface)
}

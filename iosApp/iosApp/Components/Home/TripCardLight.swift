import SwiftUI

/// PA-01 — Light-theme trip card. Mirrors composeApp `TripCard.kt` (white
/// card / dark text / lime origin dot / rounded 12pt). Used in passenger
/// home + my trips + search results lists.
struct TripCardLight: View {
    let driverName: String
    let driverInitials: String
    let driverRating: Double
    let originName: String
    let destinationName: String
    let departureTime: String
    let badgeStatus: HopBadgeStatus
    /// Price in øre. Displayed as DKK (÷100).
    let pricePerSeatOere: Int
    var isVerified: Bool = false
    var onTap: (() -> Void)? = nil

    var body: some View {
        Button(action: { onTap?() }) {
            VStack(spacing: 0) {
                // Row 1
                HStack(spacing: HopSpacing.sm) {
                    HopAvatar(name: driverName, size: .small, isVerified: isVerified)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(driverName)
                            .font(HopFont.bodyMedium(weight: .semibold))
                            .foregroundColor(Color(hex: 0x1A1A1A))
                            .lineLimit(1)
                        HStack(spacing: 4) {
                            StarRatingDisplay(rating: driverRating, count: nil, starSize: 12)
                            Text(String(format: "%.1f", driverRating))
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color(hex: 0x666666))
                        }
                    }
                    Spacer(minLength: HopSpacing.sm)
                    StatusBadge(status: badgeStatus)
                }
                Spacer().frame(height: HopSpacing.sm)
                Divider().overlay(Color(hex: 0xF0F0F0))
                Spacer().frame(height: HopSpacing.sm)

                // Row 2: Route
                VStack(alignment: .leading, spacing: 0) {
                    HStack(alignment: .center, spacing: HopSpacing.sm) {
                        Circle().fill(Color.hopPrimaryLime).frame(width: 14, height: 14)
                        Text(originName)
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color(hex: 0x1A1A1A))
                            .lineLimit(1)
                        Spacer()
                    }
                    Rectangle()
                        .fill(Color(hex: 0xD0D0D0))
                        .frame(width: 2, height: 12)
                        .padding(.leading, 6)
                    HStack(alignment: .center, spacing: HopSpacing.sm) {
                        Circle().fill(Color(hex: 0x1A1A1A)).frame(width: 14, height: 14)
                        Text(destinationName)
                            .font(HopFont.bodyMedium(weight: .medium))
                            .foregroundColor(Color(hex: 0x1A1A1A))
                            .lineLimit(1)
                        Spacer()
                    }
                }

                Spacer().frame(height: HopSpacing.sm)
                Divider().overlay(Color(hex: 0xF0F0F0))
                Spacer().frame(height: HopSpacing.sm)

                // Row 3: time + price
                HStack {
                    Text("Departs \(departureTime)")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color(hex: 0x666666))
                    Spacer()
                    Text("DKK \(pricePerSeatOere / 100)")
                        .font(HopFont.bodyLarge(weight: .bold))
                        .foregroundColor(Color(hex: 0x1A1A1A))
                }
            }
            .padding(HopSpacing.md)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .shadow(color: Color.black.opacity(0.10), radius: 4, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}

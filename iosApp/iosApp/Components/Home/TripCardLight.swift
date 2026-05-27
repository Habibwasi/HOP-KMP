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
    var isBooked: Bool = false
    var onTap: (() -> Void)? = nil

    @State private var scale: CGFloat = 1.0

    var body: some View {
        Button(action: { onTap?() }) {
            VStack(spacing: 0) {
                // Row 1
                HStack(spacing: HopSpacing.sm) {
                    HopAvatar(name: driverName, size: .small, isVerified: isVerified)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(driverName)
                            .font(HopFont.bodyMedium(weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .lineLimit(1)
                        HStack(spacing: 4) {
                            StarRatingDisplay(rating: driverRating, count: nil, starSize: 12, lightSurface: true)
                            Text(String(format: "%.1f", driverRating))
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                        }
                    }
                    Spacer(minLength: HopSpacing.sm)
                    StatusBadge(status: badgeStatus)

                    // ── Booked ✓ chip ─────────────────────────────────────
                    if isBooked {
                        Spacer().frame(width: 6)
                        HStack(spacing: 3) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                            Text("Booked")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.hopPrimaryLime)
                        .clipShape(Capsule())
                    }
                }
                Spacer().frame(height: HopSpacing.sm)
                Divider().overlay(Color.hopCardBorder)
                Spacer().frame(height: HopSpacing.sm)

                // Row 2: Route
                VStack(alignment: .leading, spacing: 0) {
                    HStack(alignment: .center, spacing: HopSpacing.sm) {
                        Circle().fill(Color.hopPrimaryLime).frame(width: 14, height: 14)
                        Text(originName)
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .lineLimit(1)
                        Spacer()
                    }
                    Rectangle()
                        .fill(Color.hopCardBorder)
                        .frame(width: 2, height: 12)
                        .padding(.leading, 6)
                    HStack(alignment: .center, spacing: HopSpacing.sm) {
                        Circle().fill(Color.hopAuthTextPrimary).frame(width: 14, height: 14)
                        Text(destinationName)
                            .font(HopFont.bodyMedium(weight: .medium))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .lineLimit(1)
                        Spacer()
                    }
                }

                Spacer().frame(height: HopSpacing.sm)
                Divider().overlay(Color.hopCardBorder)
                Spacer().frame(height: HopSpacing.sm)

                // Row 3: time + price
                HStack {
                    Text("Departs \(departureTime)")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                    Spacer()
                    let priceKr = pricePerSeatOere / 100
                    let priceOre = pricePerSeatOere % 100
                    let priceText = priceOre == 0 ? "DKK \(priceKr)" : "DKK \(priceKr),\(String(format: "%02d", priceOre))"
                    Text(priceText)
                        .font(HopFont.bodyLarge(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
            }
            .padding(HopSpacing.md)
            .background(isBooked ? Color.hopPrimaryLime.opacity(0.04) : Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isBooked ? Color.hopPrimaryLime : Color.clear, lineWidth: 2)
            )
            .shadow(
                color: isBooked ? Color.hopPrimaryLime.opacity(0.55) : Color.black.opacity(0.10),
                radius: isBooked ? 12 : 4,
                x: 0, y: isBooked ? 4 : 1
            )
            .scaleEffect(scale)
        }
        .buttonStyle(.plain)
        .onAppear {
            guard isBooked else { return }
            scale = 0.93
            withAnimation(.spring(response: 0.4, dampingFraction: 0.5)) {
                scale = 1.0
            }
        }
    }
}

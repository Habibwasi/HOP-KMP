import SwiftUI
import Shared

// MARK: — Tappable address picker row ─────────────────────────────────────────

/// Mirrors Android's `AddressPickerRow` composable.
/// Displays the selected address (or placeholder) and a "Search" affordance.
/// Tapping the row invokes `onTap` so the parent can present `LocationPickerOverlay`.
struct AddressPickerRowView: View {
    let label: String
    let value: String
    let placeholder: String
    let onTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(HopFont.labelSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextSecondary)

            Button(action: onTap) {
                HStack {
                    Text(value.isEmpty ? placeholder : value)
                        .font(.system(size: 15))
                        .foregroundColor(value.isEmpty
                            ? Color.hopAuthTextSecondary.opacity(0.5)
                            : Color.hopAuthTextPrimary)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Text("Search")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color.hopPrimaryLime)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, 14)
                .background(Color.hopCardSurfaceMuted)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(Color.hopAuthTextSecondary.opacity(0.15), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)
        }
    }
}

// MARK: — Route summary row ────────────────────────────────────────────────────

/// Shows a spinner while calculating, then distance + price/seat once resolved.
/// Mirrors Android's `RouteSummaryRow` composable.
struct RouteSummaryRowView: View {
    let isCalculating: Bool
    let distanceMetres: Int
    let seatsTotal: Int

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            if isCalculating {
                ProgressView()
                    .scaleEffect(0.8)
                    .tint(Color.hopPrimaryLime)
                Text("Calculating route…")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
            } else if distanceMetres > 0 {
                let km = distanceMetres / 1000
                let priceResult = PricingEngine.shared.calculate(
                    distanceMetres: Int32(distanceMetres),
                    seatsTotal: Int32(seatsTotal)
                )
                let pricePerSeat = Int(priceResult.passengerPaysPerSeatOere) / 100
                Text("\(km) km")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Text("·")
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopAuthTextSecondary)
                Text("DKK \(pricePerSeat)/seat")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color.hopPrimaryLime)
            }
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.sm)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopPrimaryLime.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}


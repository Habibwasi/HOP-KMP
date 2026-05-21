import SwiftUI
import Shared

// MARK: — DR-09b Past Trip Detail (Driver) ────────────────────────────────────
//
// Read-only summary for a completed/cancelled trip.
// Shows total earnings and per-passenger payment status.

struct PastTripDetailDriverView: View {

    let tripId: String
    var onBack: () -> Void

    @StateObject private var wrapper = SettlementViewModelWrapper()
    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top bar
                HStack(spacing: 0) {
                    Button(action: onBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 18, weight: .regular))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .frame(width: 40, height: 40)
                    }
                    .accessibilityLabel("Back")

                    Text("Trip Details")
                        .font(HopFont.bodyLarge(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    Spacer()
                }
                .padding(.horizontal, HopSpacing.xs)
                .padding(.vertical, HopSpacing.xs)
                .background(Color.hopBackground)

                if wrapper.state.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                        .scaleEffect(1.3)
                    Spacer()
                } else if wrapper.state.entries.isEmpty {
                    Spacer()
                    Text("No passenger data for this trip.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(HopSpacing.md)
                    Spacer()
                } else {
                    let entries = wrapper.state.entries
                    let totalOere = entries.reduce(0) { $0 + $1.suggestedAmountOere }

                    // Earnings header
                    HStack {
                        Text("Total earnings")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Spacer()
                        Text(formatDkk(totalOere))
                            .font(.system(size: 20, weight: .bold, design: .monospaced))
                            .foregroundColor(Color.hopPrimaryLime)
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)

                    ScrollView(showsIndicators: false) {
                        LazyVStack(spacing: HopSpacing.sm) {
                            ForEach(entries, id: \.bookingId) { entry in
                                PastPassengerRow(entry: entry)
                            }
                        }
                        .padding(HopSpacing.md)
                    }
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let snack as SettlementEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                default: break
                }
            }
            wrapper.loadForTrip(tripId: tripId)
        }
    }
}

// MARK: — Row

private struct PastPassengerRow: View {
    let entry: TripSettlementEntry

    var statusColor: Color {
        switch entry.paymentStatus {
        case .confirmed: return Color.hopPrimaryLime
        case .paid:      return Color(red: 1, green: 0.655, blue: 0.149)
        default:         return Color.hopAuthTextSecondary
        }
    }

    var statusLabel: String {
        switch entry.paymentStatus {
        case .confirmed: return "Confirmed"
        case .paid:      return "Marked paid"
        default:         return "Waiting"
        }
    }

    var body: some View {
        HStack {
            ZStack {
                Circle()
                    .fill(Color.hopPrimaryLime.opacity(0.15))
                    .frame(width: 40, height: 40)
                Text(entry.passengerInitials)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(Color.hopPrimaryLime)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(entry.passengerName)
                    .font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Text(statusLabel)
                    .font(HopFont.labelSmall())
                    .foregroundColor(statusColor)
            }

            Spacer()

            Text(formatDkk(Int(entry.suggestedAmountOere)))
                .font(.system(size: 15, weight: .semibold, design: .monospaced))
                .foregroundColor(Color.hopAuthTextPrimary)
        }
        .padding(HopSpacing.md)
        .background(Color.hopAuthInputSurface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private func formatDkk(_ oere: Int) -> String {
    let kr = oere / 100
    let rem = oere % 100
    return rem == 0 ? "DKK \(kr)" : "DKK \(kr),\(String(format: "%02d", rem))"
}

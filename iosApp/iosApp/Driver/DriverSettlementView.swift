import SwiftUI
import Shared

// MARK: — SE-02 Driver Settlement ─────────────────────────────────────────────
//
// Shows all passengers for the trip. Driver can confirm receipt per passenger.

struct DriverSettlementView: View {

    /// Primary entry: the trip's id.
    let tripId: String
    /// Optional: resolve tripId from a bookingId (notification deep-link entry).
    var bookingIdForResolution: String? = nil
    var onBack: () -> Void
    var onSettlementComplete: () -> Void

    @StateObject private var wrapper = SettlementViewModelWrapper()
    @State private var toast: String? = nil
    @Environment(\.scenePhase) private var scenePhase

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
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("Back")

                    Text("Payment Confirmation")
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
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthAccent))
                        .scaleEffect(1.3)
                    Spacer()
                } else if wrapper.state.entries.isEmpty {
                    Spacer()
                    Text("No passengers found for this trip.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(HopSpacing.md)
                    Spacer()
                } else {
                    let entries = wrapper.state.entries
                    let waitingCount = entries.filter { $0.paymentStatus == .waiting }.count

                    ScrollView(showsIndicators: false) {
                        LazyVStack(spacing: HopSpacing.md) {
                            ForEach(entries, id: \.bookingId) { entry in
                                PassengerSettlementCard(
                                    entry: entry,
                                    isConfirming: wrapper.state.confirmingBookingId == entry.bookingId,
                                    onConfirm: { wrapper.confirmReceivedForBooking(bookingId: entry.bookingId) }
                                )
                            }
                        }
                        .padding(HopSpacing.md)
                        .padding(.bottom, 80)
                    }

                    // Footer summary
                    HStack {
                        Text(waitingCount == 0
                             ? "All passengers have marked as paid"
                             : "\(waitingCount) passenger\(waitingCount > 1 ? "s" : "") still to pay")
                            .font(HopFont.labelSmall())
                            .foregroundColor(waitingCount == 0 ? Color.hopAuthAccent : Color.hopAuthTextSecondary)
                        Spacer()
                    }
                    .padding(HopSpacing.md)
                    .background(Color.hopBackground)
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { wrapper.refresh() }
        }
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is SettlementEffectConfirmReceivedSuccess:
                    onSettlementComplete()
                case is SettlementEffectDisputeSubmittedSuccess:
                    withAnimation { toast = "Dispute submitted" }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { onSettlementComplete() }
                case let snack as SettlementEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                default: break
                }
            }
            if let bookingId = bookingIdForResolution {
                wrapper.loadForTripByBooking(bookingId: bookingId)
            } else {
                wrapper.loadForTrip(tripId: tripId)
            }
        }
    }
}

// MARK: — Per-passenger card

private struct PassengerSettlementCard: View {
    let entry: TripSettlementEntry
    let isConfirming: Bool
    let onConfirm: () -> Void

    var statusColor: Color {
        switch entry.paymentStatus {
        case .confirmed: return Color.hopAuthAccent
        case .paid:      return Color.hopWarning
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

    private func formatDkk(_ oere: Int) -> String {
        let kr = oere / 100
        let rem = oere % 100
        return rem == 0 ? "DKK \(kr)" : "DKK \(kr),\(String(format: "%02d", rem))"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack {
                // Avatar
                ZStack {
                    Circle()
                        .fill(Color.hopAuthAccent.opacity(0.12))
                        .frame(width: 44, height: 44)
                    Text(entry.passengerInitials)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color.hopAuthAccent)
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
                    .font(.system(size: 17, weight: .bold, design: .monospaced))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }

            if entry.paymentStatus == .paid {
                HopButton(
                    text: "Confirm Received",
                    variant: .primary,
                    isLoading: isConfirming
                ) {
                    onConfirm()
                }
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopAuthInputSurface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

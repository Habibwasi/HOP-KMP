import SwiftUI
import Shared

/// PA-04 — Booking Confirmation. Mirrors composeApp `BookingConfirmationScreen.kt`.
struct BookingConfirmationView: View {
    let tripId: String
    let onBack: () -> Void
    let onNavigateToMobilePay: (_ bookingId: String) -> Void

    @StateObject private var tripWrapper = TripDetailViewModelWrapper()
    @StateObject private var bookingWrapper = BookingViewModelWrapper()

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 36, height: 36)
                }.buttonStyle(.plain)
                Text("Confirm Booking")
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            if tripWrapper.state.isLoading {
                Spacer()
                ProgressView().tint(Color.hopPrimaryGreen)
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: HopSpacing.md) {
                        TripSummaryCard(state: tripWrapper.state)
                        PriceSummaryCard(state: tripWrapper.state)
                        if tripWrapper.state.model == .b,
                           let minT = tripWrapper.state.minThreshold?.intValue,
                           minT > 0 {
                            ModelBNoticeCard(
                                seatsBooked: Int(tripWrapper.state.seatsBooked),
                                minThreshold: minT
                            )
                        }
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.top, HopSpacing.md)
                    .padding(.bottom, HopSpacing.md)
                }
            }

            HopButton(
                text: "Pay with MobilePay",
                variant: .primary,
                isLoading: bookingWrapper.state.paymentState == .processing
            ) {
                bookingWrapper.createBooking(tripId: tripId, seats: 1)
            }
            .padding(HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            tripWrapper.startObserving { _ in }
            tripWrapper.loadTrip(tripId: tripId)
            bookingWrapper.startObserving { effect in
                if let nav = effect as? BookingEffectNavigateToMobilePay {
                    onNavigateToMobilePay(nav.bookingId)
                }
            }
        }
    }
}

private struct TripSummaryCard: View {
    let state: TripDetailUiState
    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack {
                Circle().fill(Color.hopPrimaryLime).frame(width: 10, height: 10)
                Text(state.originName).font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack {
                Circle().fill(Color.hopAuthTextPrimary).frame(width: 10, height: 10)
                Text(state.destName).font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            Divider()
            HStack {
                Image(systemName: "calendar").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(state.departsAt).font(HopFont.bodySmall()).foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
                Image(systemName: "person.fill").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(state.driverName).font(HopFont.bodySmall()).foregroundColor(Color.hopAuthTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct PriceSummaryCard: View {
    let state: TripDetailUiState

    var body: some View {
        let perSeat = Int(state.priceOerePerSeat)
        let fee = Int(state.platformFeeOere)
        let seats = 1
        let total = perSeat * seats
        VStack(spacing: HopSpacing.sm) {
            row(label: "Per seat × \(seats)", value: dkk(perSeat * seats))
            row(label: "Platform fee", value: dkk(fee))
            Divider()
            HStack {
                Text("Total").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
                Text(dkk(total))
                    .font(HopFont.mono(size: 18, weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
    private func row(label: String, value: String) -> some View {
        HStack {
            Text(label).font(HopFont.bodyMedium()).foregroundColor(Color.hopAuthTextSecondary)
            Spacer()
            Text(value)
                .font(HopFont.mono(size: 14, weight: .regular))
                .foregroundColor(Color.hopAuthTextPrimary)
        }
    }
    private func dkk(_ oere: Int) -> String {
        let dkk = oere / 100
        let ore = oere % 100
        return String(format: "DKK %d.%02d", dkk, ore)
    }
}

private struct ModelBNoticeCard: View {
    let seatsBooked: Int
    let minThreshold: Int

    var body: some View {
        let progress: Double = {
            guard minThreshold > 0 else { return 0 }
            return min(1.0, Double(seatsBooked) / Double(minThreshold))
        }()
        let remaining = max(0, minThreshold - seatsBooked)
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            HStack(alignment: .top, spacing: 8) {
                Image(systemName: "info.circle.fill")
                    .font(.system(size: 16))
                    .foregroundColor(Color.hopWarning)
                Text("This trip needs \(remaining) more seats to be confirmed")
                    .font(HopFont.bodyMedium(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            ProgressView(value: progress).tint(Color.hopWarning)
        }
        .padding(HopSpacing.md)
        .background(Color.hopWarning.opacity(0.10))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

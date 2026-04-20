import SwiftUI
import Shared

// MARK: — PA-04 Booking Confirmation ──────────────────────────────────────────
//
// Shows order summary before the user pays.
// CTA "Confirm & Pay with MobilePay" dispatches BookingEvent.CreateBooking.
// Effect BookingEffect.NavigateToMobilePay → pushes PA-05.

struct BookingConfirmationView: View {

    let tripId:    String
    let tripUi:    TripUiModel?   // passed from PA-03 (may be nil on deep-link)
    private let seats = 1

    var onPayWithMobilePay: (String) -> Void   // bookingId → PA-05
    var onBack:             () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()

    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    if let trip = tripUi?.trip {
                        Spacer().frame(height: HopSpacing.md)
                        tripSummaryCard(trip: trip)
                        Spacer().frame(height: HopSpacing.md)
                        priceSummaryCard(trip: trip)
                        if trip.model == .b, let threshold = trip.minThreshold {
                            Spacer().frame(height: HopSpacing.md)
                            modelBNoticeCard(
                                booked: Int(trip.seatsBooked) + 1,
                                threshold: Int(truncating: threshold)
                            )
                        }
                        Spacer().frame(height: HopSpacing.md)
                    }
                }
            }

            // ── Sticky CTA ────────────────────────────────────────────────────
            VStack(spacing: 0) {
                Divider().background(Color.hopSurfaceElevated)
                HopPrimaryButton(
                    title: "Pay with MobilePay",
                    isLoading: wrapper.state.isLoading,
                    isEnabled: wrapper.state.paymentState == .idle || wrapper.state.paymentState == .failed
                ) {
                    wrapper.createBooking(tripId: tripId, seats: seats)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.md)
                .background(Color.hopSurface)
            }

            // ── Toast ─────────────────────────────────────────────────────────
            if let msg = toastMessage {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl + 56)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { toastMessage = nil }
                        }
                    }
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopTextPrimary)
                }
                .accessibilityLabel("Back")
                .disabled(wrapper.state.isLoading)
            }
            ToolbarItem(placement: .principal) {
                Text("Confirm Booking")
                    .font(HopFont.bodyLarge()).fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                handleEffect(effect)
            }
        }
        .onChange(of: wrapper.state.error) { error in
            if let error {
                withAnimation { toastMessage = error }
            }
        }
    }

    // MARK: — Effect handler

    private func handleEffect(_ effect: any BookingEffect) {
        if let nav = effect as? BookingEffectNavigateToMobilePay {
            onPayWithMobilePay(nav.bookingId)
        } else if let snack = effect as? BookingEffectShowSnackbar {
            withAnimation { toastMessage = snack.message }
        }
    }

    // MARK: — Trip summary card

    private func tripSummaryCard(trip: Trip) -> some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Trip Summary")
                .font(HopFont.labelMedium()).fontWeight(.semibold)
                .foregroundColor(Color.hopTextSecondary)

            Spacer().frame(height: 2)

            HStack(spacing: HopSpacing.xs) {
                routeDot(color: .hopPrimaryLime)
                Text(trip.originName)
                    .font(HopFont.bodyMedium()).fontWeight(.medium)
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
            }
            HStack(spacing: HopSpacing.xs) {
                routeDot(color: .hopTextSecondary)
                Text(trip.destName)
                    .font(HopFont.bodyMedium()).fontWeight(.medium)
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
            }
            Divider().background(Color.hopSurface)
            HStack(spacing: HopSpacing.lg) {
                summaryMetaItem(label: "Departs", value: HopDateFormatter.shortDisplay(iso: trip.departsAt))
                summaryMetaItem(label: "Driver",  value: trip.driverId)
                summaryMetaItem(label: "Seats",   value: seats == 1 ? "1 seat" : "\(seats) seats")
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
    }

    // MARK: — Price summary card

    private func priceSummaryCard(trip: Trip) -> some View {
        let totalOere       = trip.priceOerePerSeat * seats
        let platformFeeOere = max(0, trip.priceOerePerSeat - trip.driverNetOere)
        let seatCostOere    = trip.priceOerePerSeat - platformFeeOere

        return VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Price Summary")
                .font(HopFont.labelMedium()).fontWeight(.semibold)
                .foregroundColor(Color.hopTextSecondary)

            Spacer().frame(height: 2)

            // Total (prominent)
            HStack {
                Text("Total")
                    .font(HopFont.labelLarge()).fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                Text("DKK \(totalOere / 100)")
                    .font(HopFont.headlineSmall()).fontWeight(.bold)
                    .foregroundColor(Color.hopPrimaryLime)
            }

            Divider().background(Color.hopSurface)

            // Breakdown rows
            HStack {
                Text("\(seats)× seat cost")
                    .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
                Spacer()
                Text("DKK \(seatCostOere * seats / 100)")
                    .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
            }
            HStack {
                Text("Platform fee")
                    .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
                Spacer()
                Text("DKK \(platformFeeOere * seats / 100)")
                    .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
    }

    // MARK: — Payment method row

    private var paymentMethodRow: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: "creditcard.fill")
                .foregroundColor(Color.hopPrimaryLime)
            Text("MobilePay")
                .font(HopFont.bodyMedium()).fontWeight(.medium)
                .foregroundColor(Color.hopTextPrimary)
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(Color.hopSuccess)
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
    }

    // MARK: — Helpers

    private func summaryMetaItem(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
            Text(value)
                .font(HopFont.bodySmall()).fontWeight(.medium)
                .foregroundColor(Color.hopTextPrimary)
                .lineLimit(1)
        }
    }

    private func routeDot(color: Color) -> some View {
        Circle().fill(color).frame(width: 8, height: 8)
            .padding(.leading, HopSpacing.xxs)
    }
}

// MARK: — Previews

#Preview("PA-04 Booking Confirmation — Idle") {
    NavigationStack {
        BookingConfirmationView(
            tripId:          "trip-001",
            tripUi:          nil,
            onPayWithMobilePay: { _ in },
            onBack:           {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-04 Booking Confirmation — Loading") {
    NavigationStack {
        BookingConfirmationView(
            tripId:          "trip-001",
            tripUi:          nil,
            onPayWithMobilePay: { _ in },
            onBack:           {}
        )
    }
    .preferredColorScheme(.dark)
}

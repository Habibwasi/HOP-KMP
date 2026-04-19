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
    let seats:     Int

    var onPayWithMobilePay: (String) -> Void   // bookingId → PA-05
    var onBack:             () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()

    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    sectionLabel("Your booking")

                    // ── Trip summary card ─────────────────────────────────────
                    if let trip = tripUi?.trip {
                        tripSummaryCard(trip: trip)
                    }

                    sectionLabel("Price breakdown")

                    // ── Price breakdown ───────────────────────────────────────
                    if let trip = tripUi?.trip {
                        priceBreakdown(trip: trip)
                    }

                    // ── Payment method ────────────────────────────────────────
                    sectionLabel("Payment")
                    paymentMethodRow

                    // ── Legal note ────────────────────────────────────────────
                    Text("By confirming you agree to Hop's Terms of Service. Payment is processed by Vipps MobilePay.")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.top, HopSpacing.md)
                        .padding(.bottom, HopSpacing.xxl + 56)
                }
            }

            // ── Sticky CTA ────────────────────────────────────────────────────
            VStack(spacing: 0) {
                Divider().background(Color.hopSurfaceElevated)
                HopPrimaryButton(
                    title: "Confirm & Pay with MobilePay",
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
            HStack(spacing: HopSpacing.xs) {
                routeDot(color: .hopPrimaryGreen)
                Text(trip.originName)
                    .font(HopFont.bodyMedium()).fontWeight(.medium)
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
            }
            HStack(spacing: HopSpacing.xs) {
                routeDot(color: .hopPrimaryLime)
                Text(trip.destName)
                    .font(HopFont.bodyMedium()).fontWeight(.medium)
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
            }
            Divider().background(Color.hopSurface)
            HStack {
                Label(HopDateFormatter.dayDate(iso: trip.departsAt), systemImage: "calendar")
                Spacer()
                Label(HopDateFormatter.timeOnly(iso: trip.departsAt), systemImage: "clock")
                Spacer()
                Label("\(seats) seat\(seats == 1 ? "" : "s")", systemImage: "person.2")
            }
            .font(HopFont.bodySmall())
            .foregroundColor(Color.hopTextSecondary)
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Price breakdown

    private func priceBreakdown(trip: Trip) -> some View {
        let perSeat    = Double(trip.priceOerePerSeat) / 100.0
        let total      = perSeat * Double(seats)
        // Display-only fee estimate (actual fee is on the backend)
        let feeEst     = total * 0.15
        let driverEst  = total - feeEst

        return VStack(spacing: HopSpacing.sm) {
            confirmationRow(label: "Fare per seat", value: "DKK \(String(format: "%.0f", perSeat))")
            if seats > 1 {
                confirmationRow(label: "× \(seats) seats", value: "DKK \(String(format: "%.0f", total))")
            }
            Divider().background(Color.hopSurface)
            HStack {
                Text("Total")
                    .font(HopFont.labelMedium()).fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                Text("DKK \(String(format: "%.0f", total))")
                    .font(HopFont.headlineSmall()).fontWeight(.bold)
                    .foregroundColor(Color.hopPrimaryLime)
            }
            Text("Includes platform fee (≈DKK \(String(format: "%.0f", feeEst))). Driver receives ≈DKK \(String(format: "%.0f", driverEst)).")
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Payment method row

    private var paymentMethodRow: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: "creditcard.fill")
                .font(.system(size: 20))
                .foregroundColor(Color.hopPrimaryGreen)
            Text("Vipps MobilePay")
                .font(HopFont.labelMedium()).fontWeight(.medium)
                .foregroundColor(Color.hopTextPrimary)
            Spacer()
            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(Color.hopTextSecondary)
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Helpers

    private func sectionLabel(_ text: String) -> some View {
        Text(text)
            .font(HopFont.labelMedium()).fontWeight(.semibold)
            .foregroundColor(Color.hopTextSecondary)
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.md)
            .padding(.bottom, HopSpacing.xs)
    }

    private func confirmationRow(label: String, value: String) -> some View {
        HStack {
            Text(label).font(HopFont.bodyMedium()).foregroundColor(Color.hopTextSecondary)
            Spacer()
            Text(value).font(HopFont.bodyMedium()).foregroundColor(Color.hopTextPrimary)
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
            seats:           1,
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
            seats:           2,
            onPayWithMobilePay: { _ in },
            onBack:           {}
        )
    }
    .preferredColorScheme(.dark)
}

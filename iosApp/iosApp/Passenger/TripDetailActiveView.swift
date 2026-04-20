import SwiftUI
import Shared

// MARK: — PA-08 Trip Detail Active ────────────────────────────────────────────
//
// Active booking view for the passenger.
// Fetches trip by bookingId via TripViewModel.
// Actions: Message Driver, Cancel Booking (if eligible).
// Effect CancelBooking → BookingEffect.NavigateToCancellationConfirmation.

struct TripDetailActiveView: View {

    let bookingId: String

    var onMessageDriver:  (String) -> Void   // bookingId → SH-04 Chat
    var onCancelBooking:  (String) -> Void   // bookingId → PA-10
    var onRateDriver:     (String, String, String) -> Void // bookingId, driverName, initials → PA-09
    var onBack:           () -> Void

    @StateObject private var tripWrapper    = TripViewModelWrapper()
    @StateObject private var bookingWrapper = BookingViewModelWrapper()

    @State private var showCancelAlert = false
    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            if tripWrapper.state.isLoading {
                loadingView
            } else if let trip = tripWrapper.state.selectedTrip {
                tripContent(trip)
            } else {
                errorView
            }

            if let msg = toastMessage {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl + 52)
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
            }
            ToolbarItem(placement: .principal) {
                Text("My Trip")
                    .font(HopFont.bodyLarge(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .alert("Cancel Booking?", isPresented: $showCancelAlert) {
            Button("Yes, cancel", role: .destructive) {
                bookingWrapper.cancelBooking(id: bookingId)
            }
            Button("Keep booking", role: .cancel) {}
        } message: {
            Text("Your booking will be cancelled and a refund processed where applicable.")
        }
        .task {
            tripWrapper.startObserving { _ in }
            bookingWrapper.startObserving { effect in handleBookingEffect(effect) }
            // Load the trip associated with this booking from the passenger trip list
            tripWrapper.loadMyTripsPassenger()
        }
        .onChange(of: tripWrapper.state.trips) { _, trips in
            // Once loaded, find the trip whose bookingId matches
            if let match = trips.first(where: { $0.bookingId == bookingId }) {
                tripWrapper.selectTrip(id: match.id)
            }
        }
    }

    // MARK: — Trip content

    @ViewBuilder
    private func tripContent(_ tripUi: TripUiModel) -> some View {
        let trip = tripUi.trip
        let isCompleted = trip.status == .completed
        let isCancelled = trip.status == .cancelled

        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {

                // ── Status badge (inline) ──────────────────────────────────────────
                bookingStatusBadge(trip: trip)

                // ── Booking ID ────────────────────────────────────────────────
                HStack {
                    Image(systemName: "ticket")
                        .font(.system(size: 13))
                        .foregroundColor(Color.hopTextSecondary)
                    Text("Booking #\(bookingId.prefix(8).uppercased())")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                    Spacer()
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xs)

                // ── Driver card ───────────────────────────────────────────────
                driverCard(driverId: trip.driverId, isCompleted: isCompleted)

                Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)

                // ── Route & info ──────────────────────────────────────────────
                sectionLabel("Trip")
                routeBlock(trip: trip)

                Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)

                sectionLabel("Details")
                detailGrid(trip: trip)

                // ── Model B info ──────────────────────────────────────────────
                if trip.model == .b, let threshold = trip.minThreshold {
                    Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)
                    sectionLabel("Confirmation")
                    modelBInfo(booked: Int(trip.seatsBooked), threshold: Int(truncating: threshold))
                }

                Spacer().frame(height: HopSpacing.xxl + 52)
            }
        }

        // ── Action bar ────────────────────────────────────────────────────────
        VStack(spacing: 0) {
            Divider().background(Color.hopSurfaceElevated)
            HStack(spacing: HopSpacing.sm) {
                if isCompleted {
                    // Completed: offer rating CTA
                    HopPrimaryButton(title: "Rate Driver") {
                        onRateDriver(bookingId, trip.driverId, String(trip.driverId.prefix(2)).uppercased())
                    }
                } else if isCancelled {
                    HopButton(text: "Booking Cancelled", variant: .ghost, isEnabled: false) {}
                } else {
                    // Active: message + cancel
                    HopPrimaryButton(title: "Message Driver") {
                        onMessageDriver(bookingId)
                    }
                    HopButton(
                        text: "Cancel",
                        variant: .destructive,
                        isLoading: bookingWrapper.state.isLoading
                    ) {
                        showCancelAlert = true
                    }
                    .frame(maxWidth: 100)
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.md)
            .background(Color.hopSurface)
        }
    }

    // MARK: — Booking status badge (inline pill)

    private func bookingStatusBadge(trip: Trip) -> some View {
        let (color, icon, text): (Color, String, String) = {
            switch trip.status {
            case .active:    return (.hopPrimaryGreen, "checkmark.circle.fill", "Booking Confirmed")
            case .confirmed: return (.hopPrimaryGreen, "checkmark.circle.fill", "Trip Confirmed")
            case .completed: return (.hopSuccess,      "flag.checkered.2.crossed", "Trip Completed")
            case .cancelled: return (.hopError,        "xmark.circle.fill", "Booking Cancelled")
            case .unknown:   return (.hopWarning,      "questionmark.circle", "Status Unknown")
            }
        }()

        return HStack(spacing: HopSpacing.xs) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(color)
            Text(text)
                .font(HopFont.labelSmall(weight: .medium))
                .foregroundColor(color)
        }
        .padding(.horizontal, HopSpacing.sm)
        .padding(.vertical, HopSpacing.xxs)
        .background(color.opacity(0.12))
        .clipShape(Capsule())
        .padding(.horizontal, HopSpacing.md)
        .padding(.top, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Driver card

    private func driverCard(driverId: String, isCompleted: Bool) -> some View {
        HStack(spacing: HopSpacing.md) {
            HopAvatar(name: driverId, imageURL: nil, size: .large)
            VStack(alignment: .leading, spacing: HopSpacing.xxs) {
                Text(driverId)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 12)).foregroundColor(Color.hopPrimaryLime)
                    Text("4.9 · Verified")
                        .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
                }
            }
            Spacer()
            if !isCompleted {
                // Phone call button
                Button {
                    // Phone number not yet available via shared ViewModel — falls back to chat
                    onMessageDriver(bookingId)
                } label: {
                    Image(systemName: "phone")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopPrimaryLime)
                        .frame(width: 44, height: 44)
                        .background(Color.hopPrimaryLime.opacity(0.1))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Call driver")

                Button {
                    onMessageDriver(bookingId)
                } label: {
                    Image(systemName: "message")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopPrimaryLime)
                        .frame(width: 44, height: 44)
                        .background(Color.hopPrimaryLime.opacity(0.1))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Message driver")
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Route block

    private func routeBlock(trip: Trip) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: HopSpacing.md) {
                Image(systemName: "circle.fill")
                    .font(.system(size: 12)).foregroundColor(Color.hopPrimaryGreen)
                Text(trip.originName)
                    .font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
                Spacer()
                Text(HopDateFormatter.timeOnly(iso: trip.departsAt))
                    .font(HopFont.labelMedium()).foregroundColor(Color.hopPrimaryLime)
            }
            HStack {
                Rectangle().fill(Color.hopSurface.opacity(0.8))
                    .frame(width: 2, height: 20)
                    .padding(.leading, 5)
                Spacer()
            }
            HStack(spacing: HopSpacing.md) {
                Image(systemName: "mappin.circle.fill")
                    .font(.system(size: 12)).foregroundColor(Color.hopPrimaryLime)
                Text(trip.destName)
                    .font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopTextPrimary).lineLimit(1)
                Spacer()
            }
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Detail grid

    private func detailGrid(trip: Trip) -> some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: HopSpacing.sm) {
            InfoTile(icon: "calendar", label: "Date",    value: HopDateFormatter.dayDate(iso: trip.departsAt))
            InfoTile(icon: "clock",    label: "Time",    value: HopDateFormatter.timeOnly(iso: trip.departsAt))
            InfoTile(icon: "road.lanes", label: "Distance", value: "\(trip.distanceMetres / 1000) km")
            InfoTile(icon: "creditcard", label: "Paid",  value: "DKK \(String(format: "%.0f", Double(trip.priceOerePerSeat) / 100.0))")
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Model B info

    private func modelBInfo(booked: Int, threshold: Int) -> some View {
        let met = booked >= threshold
        return HStack(spacing: HopSpacing.sm) {
            Image(systemName: met ? "checkmark.circle.fill" : "clock")
                .foregroundColor(met ? Color.hopSuccess : Color.hopWarning)
            Text(met ? "Threshold reached — trip is on!" : "Awaiting \(threshold - booked) more booking(s) to confirm.")
                .font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Helpers

    private func sectionLabel(_ text: String) -> some View {
        Text(text)
            .font(HopFont.labelMedium(weight: .semibold))
            .foregroundColor(Color.hopTextSecondary)
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.md).padding(.bottom, HopSpacing.xs)
    }

    private var loadingView: some View {
        VStack {
            Spacer()
            ProgressView().progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime)).scaleEffect(1.2)
            Spacer()
        }.frame(maxWidth: .infinity)
    }

    private var errorView: some View {
        EmptyState(systemImage: "exclamationmark.triangle", headline: "Booking not found",
                   subtitle: "This booking may no longer be available.", ctaLabel: "Go back", ctaAction: onBack)
    }

    private func handleBookingEffect(_ effect: any BookingEffect) {
        if let nav = effect as? BookingEffectNavigateToCancellationConfirmation {
            onCancelBooking(nav.bookingId)
        } else if let snack = effect as? BookingEffectShowSnackbar {
            withAnimation { toastMessage = snack.message }
        }
    }
}

// MARK: — InfoTile

private struct InfoTile: View {
    let icon:  String
    let label: String
    let value: String
    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xxs) {
            HStack(spacing: 4) {
                Image(systemName: icon).font(.system(size: 12)).foregroundColor(Color.hopTextSecondary)
                Text(label).font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
            }
            Text(value).font(HopFont.labelMedium(weight: .medium)).foregroundColor(Color.hopTextPrimary)
        }
    }
}

// MARK: — Previews

#Preview("PA-08 Trip Detail Active — Loading") {
    NavigationStack {
        TripDetailActiveView(
            bookingId:       "bk-001",
            onMessageDriver: { _ in },
            onCancelBooking: { _ in },
            onRateDriver:    { _, _, _ in },
            onBack:          {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-08 Trip Detail Active — Error") {
    NavigationStack {
        TripDetailActiveView(
            bookingId:       "bk-missing",
            onMessageDriver: { _ in },
            onCancelBooking: { _ in },
            onRateDriver:    { _, _, _ in },
            onBack:          {}
        )
    }
    .preferredColorScheme(.dark)
}

import SwiftUI
import Shared

// MARK: — PA-03 Trip Detail ────────────────────────────────────────────────────
//
// Displays full trip info. Driver profile, route map placeholder, price breakdown,
// seat availability, Model A/B badge. CTA: "Book this ride".
// Uses TripViewModel.SelectTrip → state.selectedTrip to get detailed data.

struct TripDetailView: View {

    let tripId: String
    var onBook:    (String) -> Void   // tripId → PA-04 Booking Confirmation
    var onBack:    () -> Void

    @StateObject private var wrapper = TripViewModelWrapper()

    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            if wrapper.state.isLoading {
                loadingView
            } else if let trip = wrapper.state.selectedTrip {
                tripContent(trip)
            } else {
                errorView
            }

            // ── Toast ─────────────────────────────────────────────────────────
            if let msg = toastMessage {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl)
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
                Text("Trip Details")
                    .font(HopFont.bodyLarge(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { _ in /* effects for detail: handled below */ }
            wrapper.selectTrip(id: tripId)
        }
        .onChange(of: wrapper.state.error) { _, error in
            if let error {
                withAnimation { toastMessage = error }
            }
        }
    }

    // MARK: — Trip content

    @ViewBuilder
    private func tripContent(_ tripUi: TripUiModel) -> some View {
        let trip = tripUi.trip
        let availableSeats = Int(trip.seatsTotal) - Int(trip.seatsBooked)

        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {

                // ── Driver row ───────────────────────────────────────────────
                driverRow(trip: trip)

                Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)

                // ── Route section ─────────────────────────────────────────────
                routeBlock(trip: trip)

                Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)

                // ── Trip info section ─────────────────────────────────────────
                tripInfoGrid(trip: trip)

                // ── Model B threshold (if applicable) ─────────────────────────
                if trip.model == .b, let threshold = trip.minThreshold {
                    Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)
                    modelBThreshold(booked: Int(trip.seatsBooked), threshold: Int(truncating: threshold))
                }

                Divider().background(Color.hopSurfaceElevated).padding(.horizontal, HopSpacing.md)

                // ── Price breakdown ───────────────────────────────────────────
                priceSection(trip: trip)

                Spacer().frame(height: HopSpacing.xxl + 56) // clear CTA button
            }
        }

        // ── Sticky Book CTA ───────────────────────────────────────────────────
        VStack(spacing: 0) {
            Divider().background(Color.hopSurfaceElevated)
            HopPrimaryButton(
                title: "Book for DKK \(String(format: "%.0f", Double(Int(trip.priceOerePerSeat)) / 100.0))",
                isEnabled: availableSeats > 0 && !tripUi.isBroken
            ) {
                onBook(tripId)
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.md)
            .background(Color.hopSurface)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: — Driver row

    private func driverRow(trip: Trip) -> some View {
        HStack(spacing: HopSpacing.md) {
            HopAvatar(name: trip.driverId, imageURL: nil, size: .large)
            VStack(alignment: .leading, spacing: HopSpacing.xxs) {
                Text(trip.driverId)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 12))
                        .foregroundColor(Color.hopPrimaryLime)
                    Text("4.9")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                    Text("· Verified driver")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                }
            }
            Spacer()
            StatusBadge(status: trip.model == .b ? .modelB : .modelA)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.md)
    }

    // MARK: — Route block

    private func routeBlock(trip: Trip) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            RouteStopRow(
                icon:   "circle.fill",
                color:  Color.hopPrimaryGreen,
                label:  trip.originName,
                time:   HopDateFormatter.timeOnly(iso: trip.departsAt)
            )

            // Vertical connector line
            HStack(spacing: 0) {
                Rectangle()
                    .fill(Color.hopSurface.opacity(0.8))
                    .frame(width: 2, height: 24)
                    .padding(.leading, HopSpacing.md + 7) // align with icon centre
                Spacer()
            }

            RouteStopRow(
                icon:   "mappin.circle.fill",
                color:  Color.hopPrimaryLime,
                label:  trip.destName,
                time:   ""
            )

            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "road.lanes")
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopTextSecondary)
                Text("\(trip.distanceMetres / 1000) km")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
            .padding(.top, HopSpacing.xs)
        }
        .padding(HopSpacing.md)
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Trip info grid

    private func tripInfoGrid(trip: Trip) -> some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: HopSpacing.sm) {
            InfoCell(icon: "calendar",      label: "Date",       value: HopDateFormatter.dayDate(iso: trip.departsAt))
            InfoCell(icon: "clock",         label: "Departs",    value: HopDateFormatter.timeOnly(iso: trip.departsAt))
            InfoCell(icon: "person.2",      label: "Seats left", value: "\(trip.seatsTotal - trip.seatsBooked) of \(trip.seatsTotal)")
            InfoCell(icon: "tag",           label: "Type",       value: trip.model == .b ? "Long Trip" : "Commute")
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Model B threshold

    private func modelBThreshold(booked: Int, threshold: Int) -> some View {
        let met = booked >= threshold
        return HStack(spacing: HopSpacing.sm) {
            Image(systemName: met ? "checkmark.circle.fill" : "clock.badge.exclamationmark")
                .foregroundColor(met ? Color.hopSuccess : Color.hopWarning)
            VStack(alignment: .leading, spacing: 2) {
                Text(met ? "Trip confirmed" : "Awaiting confirmation")
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(met ? Color.hopSuccess : Color.hopWarning)
                Text("\(booked)/\(threshold) seats needed to confirm · Auto-cancels 6h before departure if threshold not met")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Price section

    private func priceSection(trip: Trip) -> some View {
        VStack(spacing: HopSpacing.sm) {
            PriceRow(label: "Per seat", value: "DKK \(String(format: "%.0f", Double(trip.priceOerePerSeat) / 100.0))", isHighlighted: false)
            Divider().background(Color.hopSurface)
            PriceRow(label: "Total", value: "DKK \(String(format: "%.0f", Double(trip.priceOerePerSeat) / 100.0))", isHighlighted: true)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.xs)
    }

    // MARK: — Helpers

    private var loadingView: some View {
        VStack {
            Spacer()
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                .scaleEffect(1.2)
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }

    private var errorView: some View {
        EmptyState(
            systemImage: "exclamationmark.triangle",
            headline:    "Trip not found",
            subtitle:    "The trip may no longer be available.",
            ctaLabel:    "Go back",
            ctaAction:   onBack
        )
    }
}

// MARK: — RouteStopRow

private struct RouteStopRow: View {
    let icon:  String
    let color: Color
    let label: String
    let time:  String

    var body: some View {
        HStack(spacing: HopSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(color)
                .frame(width: 16)
            Text(label)
                .font(HopFont.bodyMedium(weight: .medium))
                .foregroundColor(Color.hopTextPrimary)
                .lineLimit(1)
            Spacer()
            if !time.isEmpty {
                Text(time)
                    .font(HopFont.labelMedium())
                    .foregroundColor(Color.hopPrimaryLime)
            }
        }
    }
}

// MARK: — InfoCell

private struct InfoCell: View {
    let icon:  String
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xxs) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopTextSecondary)
                Text(label)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
            Text(value)
                .font(HopFont.labelMedium(weight: .medium))
                .foregroundColor(Color.hopTextPrimary)
        }
    }
}

// MARK: — PriceRow

private struct PriceRow: View {
    let label:         String
    let value:         String
    let isHighlighted: Bool

    var body: some View {
        HStack {
            Text(label)
                .font(isHighlighted ? HopFont.labelMedium(weight: .semibold) : HopFont.bodyMedium(weight: .regular))
                .foregroundColor(isHighlighted ? Color.hopTextPrimary : Color.hopTextSecondary)
            Spacer()
            Text(value)
                .font(isHighlighted ? HopFont.headlineSmall(weight: .bold) : HopFont.bodyMedium(weight: .regular))
                .foregroundColor(isHighlighted ? Color.hopPrimaryLime : Color.hopTextPrimary)
        }
    }
}

// MARK: — SeatStepperInline

private struct SeatStepperInline: View {
    @Binding var seats: Int
    let maxSeats: Int

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Button {
                if seats > 1 { seats -= 1 }
            } label: {
                Image(systemName: "minus.circle")
                    .font(.system(size: 22, weight: .light))
                    .foregroundColor(seats > 1 ? Color.hopPrimaryLime : Color.hopTextSecondary)
            }
            .buttonStyle(.plain).disabled(seats <= 1)

            Text("\(seats)")
                .font(HopFont.bodyLarge(weight: .semibold))
                .foregroundColor(Color.hopTextPrimary)
                .frame(minWidth: 24, alignment: .center)

            Button {
                if seats < maxSeats { seats += 1 }
            } label: {
                Image(systemName: "plus.circle")
                    .font(.system(size: 22, weight: .light))
                    .foregroundColor(seats < maxSeats ? Color.hopPrimaryLime : Color.hopTextSecondary)
            }
            .buttonStyle(.plain).disabled(seats >= maxSeats)
        }
    }
}

// MARK: — Previews

#Preview("PA-03 Trip Detail — Loading") {
    NavigationStack {
        TripDetailView(
            tripId: "trip-001",
            onBook: { _ in },
            onBack: {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-03 Trip Detail — Error") {
    NavigationStack {
        TripDetailView(
            tripId: "trip-missing",
            onBook: { _ in },
            onBack: {}
        )
    }
    .preferredColorScheme(.dark)
}

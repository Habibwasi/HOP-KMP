import SwiftUI
import Shared

// MARK: — DR-09 My Trips (Driver) ─────────────────────────────────────────────

struct MyTripsDriverView: View {

    var onTripTapped: (String) -> Void
    var onSettlementTapped: (String) -> Void
    var onPastTripTapped: (String) -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    @State private var selectedFilter: Filter = .upcoming

    private enum Filter: String, CaseIterable {
        case upcoming = "Upcoming"
        case past     = "Past"
    }

    private var displayed: [TripUiModel] {
        let trips = wrapper.state.trips
        switch selectedFilter {
        case .upcoming:
            return trips.filter {
                $0.trip.status == TripStatus.active ||
                $0.trip.status == TripStatus.confirmed ||
                $0.trip.status == TripStatus.thresholdNotMet ||
                $0.trip.awaitingPaymentBookingId != nil
            }
        case .past:
            return trips.filter {
                ($0.trip.status == TripStatus.completed || $0.trip.status == TripStatus.cancelled) &&
                $0.trip.awaitingPaymentBookingId == nil
            }
        }
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // ── Segmented control ────────────────────────────────────────
                HStack(spacing: 0) {
                    ForEach(Filter.allCases, id: \.self) { f in
                        Button { withAnimation { selectedFilter = f } } label: {
                            VStack(spacing: 4) {
                                Text(f.rawValue)
                                    .font(HopFont.labelMedium(weight: selectedFilter == f ? .semibold : .regular))
                                    .foregroundColor(selectedFilter == f ? Color.hopAuthTextPrimary : Color.hopAuthTextSecondary)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, HopSpacing.sm)
                                Rectangle()
                                    .fill(selectedFilter == f ? Color.hopPrimaryLime : Color.clear)
                                    .frame(height: 2)
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }

                if wrapper.state.isLoading && wrapper.state.trips.isEmpty {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                    Spacer()
                } else if displayed.isEmpty {
                    Spacer()
                    EmptyState(
                        systemImage: "car.2",
                        headline: selectedFilter == .upcoming ? "No upcoming trips" : "No past trips",
                        subtitle: selectedFilter == .upcoming ? "Post a trip to start earning." : "Completed trips will appear here.",
                        ctaLabel: nil,
                        ctaAction: nil
                    )
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: HopSpacing.sm) {
                            ForEach(displayed, id: \.id) { trip in
                                Button {
                                    if trip.trip.awaitingPaymentBookingId != nil {
                                        onSettlementTapped(trip.id)
                                    } else if trip.trip.status == TripStatus.completed || trip.trip.status == TripStatus.cancelled {
                                        onPastTripTapped(trip.id)
                                    } else {
                                        onTripTapped(trip.id)
                                    }
                                } label: {
                                    DriverTripDetailRow(trip: trip)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(HopSpacing.md)
                    }
                }
            }
        }
        .task {
            wrapper.loadDriverHome()
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "My Trips", onBack: onBack)
            .background(Color.hopBackground)
    }
    }
}

private struct DriverTripDetailRow: View {
    let trip: TripUiModel

    @State private var scale: CGFloat = 1.0

    var body: some View {
        let isNew = trip.hasRecentBooking
        let isAwaitingPayment = trip.trip.awaitingPaymentBookingId != nil
        let showThreshold = trip.model == .b && trip.trip.minThreshold != nil && trip.trip.seatsTotal > 0
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            // Row 1: route + status
            HStack {
                Text("\(trip.trip.originName) → \(trip.trip.destName)")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
                statusBadge
            }
            // Row 2: time + action badge + seats
            HStack {
                // Model A / B chip
                Text(trip.model == .a ? "Commute" : "Long Trip")
                    .font(HopFont.labelSmall(weight: .medium))
                    .foregroundColor(trip.model == .a ? Color.hopPrimaryLime : Color.hopPrimaryGreen)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background((trip.model == .a ? Color.hopPrimaryLime : Color.hopPrimaryGreen).opacity(0.15))
                    .clipShape(Capsule())
                Text(trip.trip.departsAt)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
                if isAwaitingPayment {
                    Text("Awaiting payment")
                        .font(HopFont.labelSmall(weight: .semibold))
                        .foregroundColor(Color(hex: 0x3B82F6))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(hex: 0x3B82F6).opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                    Spacer().frame(width: 4)
                } else if isNew {
                    Text("New booking")
                        .font(HopFont.labelSmall(weight: .semibold))
                        .foregroundColor(Color.hopPrimaryGreen)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.hopPrimaryGreen.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                    Spacer().frame(width: 4)
                }
                Text("\(trip.trip.seatsBooked)/\(trip.trip.seatsTotal) seats")
                    .font(HopFont.bodySmall(weight: .semibold))
                    .foregroundColor(Color.hopPrimaryLime)
            }
            // Row 3: threshold progress bar (Model B only)
            if showThreshold, let minThreshold = trip.trip.minThreshold {
                let seatsTotal = Int(trip.trip.seatsTotal)
                let seatsBooked = Int(trip.trip.seatsBooked)
                let progress = seatsTotal > 0 ? Double(seatsBooked) / Double(seatsTotal) : 0
                let tickFraction = seatsTotal > 0 ? Double(Int(minThreshold)) / Double(seatsTotal) : 0
                VStack(alignment: .leading, spacing: 4) {
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            // Track
                            RoundedRectangle(cornerRadius: 4)
                                .fill(Color(hex: 0xE8E8E8))
                                .frame(height: 8)
                            // Lime fill
                            RoundedRectangle(cornerRadius: 4)
                                .fill(Color.hopPrimaryLime)
                                .frame(width: geo.size.width * CGFloat(min(progress, 1)), height: 8)
                            // Threshold tick
                            Rectangle()
                                .fill(Color.hopAuthTextPrimary)
                                .frame(width: 2, height: 8)
                                .offset(x: geo.size.width * CGFloat(min(tickFraction, 1)) - 1)
                        }
                    }
                    .frame(height: 8)
                    HStack {
                        Text("\(seatsBooked) booked")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Spacer()
                        Text("Min \(Int(minThreshold)) to confirm")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }
                }
            }
        }
        .padding(HopSpacing.md)
        .background(isNew ? Color.hopPrimaryLime.opacity(0.04) : Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(isNew ? Color.hopPrimaryLime : Color.clear, lineWidth: 2)
        )
        .shadow(
            color: isNew ? Color.hopPrimaryLime.opacity(0.55) : Color.black.opacity(0.04),
            radius: isNew ? 12 : 2, x: 0, y: isNew ? 4 : 1
        )
        .scaleEffect(scale)
        .onAppear {
            guard isNew else { return }
            scale = 0.93
            withAnimation(.spring(response: 0.4, dampingFraction: 0.5)) { scale = 1.0 }
        }
    }

    @ViewBuilder
    private var statusBadge: some View {
        switch trip.trip.status {
        case TripStatus.active:          Badge(text: "Active",            color: Color.hopPrimaryLime)
        case TripStatus.confirmed:       Badge(text: "Confirmed",         color: Color.hopSuccess)
        case TripStatus.completed:       Badge(text: "Completed",         color: Color.hopAuthTextSecondary)
        case TripStatus.cancelled:       Badge(text: "Cancelled",         color: Color.hopError)
        case TripStatus.thresholdNotMet: Badge(text: "Threshold not met", color: Color.hopWarning)
        default: EmptyView()
        }
    }

    private struct Badge: View {
        let text: String
        let color: Color
        var body: some View {
            Text(text)
                .font(HopFont.bodySmall(weight: .semibold))
                .foregroundColor(color)
                .padding(.horizontal, HopSpacing.xs)
                .padding(.vertical, 2)
                .background(color.opacity(0.15))
                .clipShape(Capsule())
        }
    }
}

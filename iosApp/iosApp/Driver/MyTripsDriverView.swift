import SwiftUI
import Shared

// MARK: — DR-09 My Trips (Driver) ─────────────────────────────────────────────

struct MyTripsDriverView: View {

    var onTripTapped: (String) -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = DriverViewModelWrapper()

    @State private var selectedFilter: Filter = .upcoming

    private enum Filter: String, CaseIterable {
        case upcoming = "Upcoming"
        case past     = "Past"
    }

    private var displayed: [TripUiModel] {
        let trips = wrapper.state.trips
        switch selectedFilter {
        case .upcoming:
            return trips.filter { $0.trip.status == TripStatus.active || $0.trip.status == TripStatus.confirmed }
        case .past:
            return trips.filter { $0.trip.status == TripStatus.completed || $0.trip.status == TripStatus.cancelled }
        }
    }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {
                // ── Segmented control ────────────────────────────────────────
                HStack(spacing: 0) {
                    ForEach(Filter.allCases, id: \.self) { f in
                        Button { withAnimation { selectedFilter = f } } label: {
                            VStack(spacing: 4) {
                                Text(f.rawValue)
                                    .font(HopFont.labelMedium(weight: selectedFilter == f ? .semibold : .regular))
                                    .foregroundColor(selectedFilter == f ? Color.hopTextPrimary : Color.hopTextSecondary)
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
                                Button { onTripTapped(trip.id) } label: {
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
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("My Trips").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { _ in }
            wrapper.loadDriverHome()
        }
    }
}

private struct DriverTripDetailRow: View {
    let trip: TripUiModel

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            HStack {
                Text("\(trip.trip.originName) → \(trip.trip.destName)")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                statusBadge
            }
            HStack {
                Text(trip.trip.departsAt)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
                Spacer()
                Text("\(trip.trip.seatsBooked)/\(trip.trip.seatsTotal) seats")
                    .font(HopFont.bodySmall(weight: .semibold))
                    .foregroundColor(Color.hopPrimaryLime)
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private var statusBadge: some View {
        switch trip.trip.status {
        case TripStatus.active:    Badge(text: "Active",    color: Color.hopPrimaryLime)
        case TripStatus.confirmed: Badge(text: "Confirmed", color: Color.hopSuccess)
        case TripStatus.completed: Badge(text: "Completed", color: Color.hopTextSecondary)
        case TripStatus.cancelled: Badge(text: "Cancelled", color: Color.hopError)
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

import SwiftUI
import Shared

// MARK: — PA-07 My Trips (Passenger) ──────────────────────────────────────────
//
// Tab-segmented list: Upcoming / Past.
// Uses TripViewModel.LoadMyTripsPassenger.
// Empty states per tab. Tapping a trip pushes PA-08 Trip Detail Active.

struct MyTripsPassengerView: View {

    var onTripTapped:    (String) -> Void   // bookingId → PA-08
    var navigate:        (HopRoute) -> Void

    @StateObject private var wrapper = TripViewModelWrapper()

    @State private var selectedTab: TripTab = .upcoming
    @State private var toastMessage: String? = nil

    private enum TripTab: String, CaseIterable {
        case upcoming = "Upcoming"
        case past     = "Past"
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: 0) {
                // ── Title bar ─────────────────────────────────────────────────
                HStack {
                    Text("My Trips")
                        .font(HopFont.headlineMedium())
                        .fontWeight(.bold)
                        .foregroundColor(Color.hopTextPrimary)
                    Spacer()
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.md)

                // ── Segment control ───────────────────────────────────────────
                tabSegment

                // ── Content ───────────────────────────────────────────────────
                if wrapper.state.isLoading {
                    loadingView
                } else {
                    tripListView
                }
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
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { effect in
                if let snack = effect as? TripEffectShowSnackbar {
                    withAnimation { toastMessage = snack.message }
                }
            }
            wrapper.loadMyTripsPassenger()
        }
    }

    // MARK: — Segment control

    private var tabSegment: some View {
        HStack(spacing: 0) {
            ForEach(TripTab.allCases, id: \.self) { tab in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) { selectedTab = tab }
                } label: {
                    VStack(spacing: HopSpacing.xxs) {
                        Text(tab.rawValue)
                            .font(HopFont.labelMedium())
                            .fontWeight(selectedTab == tab ? .semibold : .regular)
                            .foregroundColor(selectedTab == tab ? Color.hopTextPrimary : Color.hopTextSecondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, HopSpacing.sm)

                        Rectangle()
                            .fill(selectedTab == tab ? Color.hopPrimaryLime : Color.clear)
                            .frame(height: 2)
                    }
                }
                .buttonStyle(.plain)
            }
        }
        .background(Color.hopSurface)
        .overlay(alignment: .bottom) {
            Divider().background(Color.hopSurfaceElevated)
        }
    }

    // MARK: — Trip list

    private var tripListView: some View {
        let now       = Date()
        let allTrips  = wrapper.state.trips

        let upcoming = allTrips.filter { tripUi in
            let d = HopDateFormatter.parseISO(tripUi.trip.departsAt)
            return d.map { $0 >= now } ?? true
        }
        let past = allTrips.filter { tripUi in
            let d = HopDateFormatter.parseISO(tripUi.trip.departsAt)
            return d.map { $0 < now } ?? false
        }

        let displayed = selectedTab == .upcoming ? upcoming : past

        return Group {
            if displayed.isEmpty {
                emptyStateView(for: selectedTab)
                    .transition(.opacity)
            } else {
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: HopSpacing.sm) {
                        ForEach(displayed, id: \.id) { tripUi in
                            PassengerTripRow(tripUi: tripUi) {
                                // Navigate using bookingId for active trips, tripId otherwise
                                if let bookingId = tripUi.bookingId {
                                    onTripTapped(bookingId)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.top, HopSpacing.md)
                    .padding(.bottom, HopSpacing.xxl)
                }
                .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.2), value: selectedTab)
    }

    // MARK: — Loading

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

    // MARK: — Empty states

    private func emptyStateView(for tab: TripTab) -> some View {
        switch tab {
        case .upcoming:
            return EmptyState(
                systemImage: "car.2",
                headline:    "No upcoming trips",
                subtitle:    "Search for rides and book your next journey.",
                ctaLabel:    "Find a ride",
                ctaAction:   { navigate(.home) }
            )
        case .past:
            return EmptyState(
                systemImage: "clock.arrow.circlepath",
                headline:    "No past trips",
                subtitle:    "Your completed trips will appear here.",
                ctaLabel:    nil,
                ctaAction:   nil
            )
        }
    }
}

// MARK: — PassengerTripRow

private struct PassengerTripRow: View {
    let tripUi: TripUiModel
    let onTap:  () -> Void

    private var badgeStatus: HopBadgeStatus {
        switch tripUi.trip.status {
        case .active:    return .confirmed
        case .confirmed: return .confirmed
        case .cancelled: return .cancelled
        case .completed: return .confirmed
        case .unknown:   return .pending
        }
    }

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: HopSpacing.sm) {
                // Route + badge
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: HopSpacing.xxs) {
                        HStack(spacing: HopSpacing.xs) {
                            Circle().fill(Color.hopPrimaryGreen).frame(width: 8, height: 8)
                            Text(tripUi.trip.originName)
                                .font(HopFont.bodyMedium()).fontWeight(.medium)
                                .foregroundColor(Color.hopTextPrimary).lineLimit(1)
                        }
                        HStack(spacing: HopSpacing.xs) {
                            Circle().fill(Color.hopPrimaryLime).frame(width: 8, height: 8)
                            Text(tripUi.trip.destName)
                                .font(HopFont.bodyMedium()).fontWeight(.medium)
                                .foregroundColor(Color.hopTextPrimary).lineLimit(1)
                        }
                    }
                    Spacer()
                    StatusBadge(status: badgeStatus)
                }

                Divider().background(Color.hopSurface.opacity(0.6))

                // Date + price
                HStack {
                    Label(HopDateFormatter.dayDate(iso: tripUi.trip.departsAt), systemImage: "calendar")
                    Spacer()
                    Text("DKK \(String(format: "%.0f", Double(tripUi.trip.priceOerePerSeat) / 100.0))")
                        .font(HopFont.labelMedium()).fontWeight(.semibold)
                        .foregroundColor(Color.hopPrimaryLime)
                }
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .opacity(tripUi.isBroken ? 0.6 : 1.0)
        }
        .buttonStyle(.plain)
        .disabled(tripUi.isBroken || tripUi.bookingId == nil)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(tripUi.trip.originName) to \(tripUi.trip.destName), \(HopDateFormatter.dayDate(iso: tripUi.trip.departsAt))")
        .accessibilityHint(tripUi.bookingId != nil ? "Double-tap to view details" : "No booking details available")
    }
}

// MARK: — parseISO helper extension

private extension HopDateFormatter {
    static func parseISO(_ iso: String) -> Date? {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        f.timeZone   = TimeZone(identifier: "UTC")
        return f.date(from: iso)
    }
}

// MARK: — Previews

#Preview("PA-07 My Trips — Empty Upcoming") {
    NavigationStack {
        MyTripsPassengerView(
            onTripTapped: { _ in },
            navigate:     { _ in }
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-07 My Trips — Loading") {
    NavigationStack {
        MyTripsPassengerView(
            onTripTapped: { _ in },
            navigate:     { _ in }
        )
    }
    .preferredColorScheme(.dark)
}

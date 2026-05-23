import SwiftUI
import Shared

/// PA-02 — Search Results. Mirrors composeApp `SearchResultsScreen.kt`.
///
/// `SearchViewModel` is registered as `viewModelOf` in Koin (factory), so the
/// instance created by `PassengerHomeView` is **not** the same instance this
/// view sees. The home view captures the typed search params into
/// `HopNavigationStack`'s `lastSearch*` state and forwards them here so we can
/// re-issue the search against this view's own VM instance on appear.
struct SearchResultsView: View {
    let onBack: () -> Void
    let onNavigateToTripDetail: (_ tripId: String) -> Void
    var origin: String = ""
    var dest:   String = ""
    var date:   String = ""
    var seats:  Int    = 1

    @StateObject private var wrapper = SearchViewModelWrapper()
    @State private var toast: String? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 36, height: 36)
                }.buttonStyle(.plain)

                let route = wrapper.state.origin.isEmpty || wrapper.state.dest.isEmpty
                    ? "Search Results"
                    : "\(wrapper.state.origin) → \(wrapper.state.dest)"
                Text(route)
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .lineLimit(1)
                Spacer()
                if !wrapper.state.date.isEmpty {
                    Text(wrapper.state.date)
                        .font(HopFont.labelSmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .padding(.horizontal, HopSpacing.sm)
                        .padding(.vertical, 4)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.hopAuthTextSecondary, lineWidth: 1)
                        )
                }
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            // Filter bar (Earliest / Cheapest / Model A / Model B — TOP_RATED excluded)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: HopSpacing.sm) {
                    filterChip("Earliest", filter: .earliest)
                    filterChip("Cheapest", filter: .cheapest)
                    filterChip("Model A", filter: .dailyCommute)
                    filterChip("Model B", filter: .longDistance)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.xs)
            }

            // Content
            if wrapper.state.isLoading {
                Spacer()
                ProgressView().tint(Color.hopPrimaryGreen)
                Spacer()
            } else if wrapper.state.results.isEmpty {
                emptyStateView
            } else {
                ScrollView {
                    VStack(spacing: HopSpacing.md) {
                        ForEach(wrapper.state.results, id: \.id) { trip in
                            TripCardLight(
                                driverName: "Driver",
                                driverInitials: "D",
                                driverRating: 4.8,
                                originName: trip.originName,
                                destinationName: trip.destName,
                                departureTime: trip.formattedDepartsAt,
                                badgeStatus: trip.model == .a ? .modelA : .modelB,
                                pricePerSeatOere: Int(trip.priceOerePerSeat)
                            ) {
                                wrapper.selectTrip(tripId: trip.id)
                            }
                        }
                    }
                    .padding(HopSpacing.md)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .overlay(alignment: .bottom) {
            if let msg = toast {
                Text(msg)
                    .font(HopFont.labelMedium())
                    .foregroundColor(.white)
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)
                    .background(Color.black.opacity(0.75))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut, value: toast)
        .onAppear {
            wrapper.startObserving { effect in
                if let n = effect as? SearchEffectNavigateToTripDetail {
                    onNavigateToTripDetail(n.tripId)
                } else if effect is SearchEffectAlertCreated {
                    self.toast = "Alert set! We'll notify you when a ride appears."
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) { self.toast = nil }
                } else if let err = effect as? SearchEffectAlertError {
                    self.toast = err.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) { self.toast = nil }
                }
            }
            if !origin.isEmpty || !dest.isEmpty {
                wrapper.search(origin: origin, dest: dest, date: date, seats: seats)
            }
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: HopSpacing.lg) {
            Spacer()
            ZStack {
                Circle().fill(Color.hopCardSurfaceMuted).frame(width: 120, height: 120)
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 44))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            Text("No rides on this route yet")
                .font(HopFont.headlineSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
            HopButton(text: "Alert me when one appears", variant: .primary) {
                wrapper.createAlert()
            }
            .frame(maxWidth: 280)
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }

    private func filterChip(_ label: String, filter: SearchFilter) -> some View {
        let active = wrapper.state.activeFilters.contains(filter)
        return Button(action: { wrapper.applyFilter(filter) }) {
            Text(label)
                .font(HopFont.labelMedium(weight: active ? .semibold : .medium))
                .foregroundColor(active ? Color.hopAuthTextPrimary : Color.hopAuthTextSecondary)
                .padding(.horizontal, HopSpacing.sm)
                .padding(.vertical, 6)
                .background(active ? Color.hopPrimaryLime : Color.clear)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(active ? Color.hopPrimaryLime : Color.hopCardBorder, lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }
}

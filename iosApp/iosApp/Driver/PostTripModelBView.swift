import SwiftUI
import Shared

// MARK: — DR-07 Post Trip — Model B (One-Off Long Distance) ───────────────────

private enum LocationPickerFieldB: Identifiable {
    case from, to
    var id: String { self == .from ? "from" : "to" }
}

struct PostTripModelBView: View {

    var onNavigateToReview: () -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    @State private var origin:       String = ""
    @State private var dest:         String = ""
    @State private var date:         String = ""    // YYYY-MM-DD
    @State private var time:         String = "08:00"
    @State private var seats:        Int    = 3
    @State private var minThreshold: Int    = 2
    @State private var locationPickerField: LocationPickerFieldB? = nil

    private var canSubmit: Bool {
        !origin.isEmpty && !dest.isEmpty && !date.isEmpty && !time.isEmpty &&
        seats >= 1 && minThreshold >= 1 && minThreshold <= seats &&
        wrapper.state.routeDistanceMetres > 0
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Text("One-off Long Distance")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    // From address picker row
                    AddressPickerRowView(label: "From", value: origin, placeholder: "e.g. Aarhus C") {
                        locationPickerField = .from
                    }

                    // To address picker row
                    AddressPickerRowView(label: "To", value: dest, placeholder: "e.g. Copenhagen Central") {
                        locationPickerField = .to
                    }

                    // Route summary
                    if !origin.isEmpty && !dest.isEmpty {
                        RouteSummaryRowView(
                            isCalculating: wrapper.state.isCalculatingRoute,
                            distanceMetres: Int(wrapper.state.routeDistanceMetres),
                            seatsTotal: seats
                        )
                    }

                    HopTextField(label: "Date (YYYY-MM-DD)", placeholder: "2026-05-20", text: $date)
                    HopTextField(label: "Departure time (HH:mm)", placeholder: "08:00", text: $time)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Total seats")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(seats)", value: $seats, in: 1...4)
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                    }

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Minimum confirmed before trip runs")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(minThreshold)", value: $minThreshold, in: 1...max(1, seats))
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Text("If fewer than \(minThreshold) passenger\(minThreshold == 1 ? "" : "s") book, the trip is cancelled and refunded.")
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Next: Review price",
                        variant: .primary,
                        isEnabled: canSubmit && !wrapper.state.isCalculatingRoute
                    ) {
                        let draft = ModelBDraft(
                            originName: origin,
                            originLat: 0.0,
                            originLng: 0.0,
                            destName: dest,
                            destLat: 0.0,
                            destLng: 0.0,
                            date: date,
                            departureTime: time,
                            seatsTotal: Int32(seats),
                            minThreshold: Int32(minThreshold),
                            distanceMetres: 0  // enriched by DriverViewModel.submitModelBDraft
                        )
                        wrapper.submitModelBDraft(draft)
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .onChange(of: origin) { triggerRouteCalcIfReady() }
        .onChange(of: dest)   { triggerRouteCalcIfReady() }
        .fullScreenCover(item: $locationPickerField) { field in
            LocationPickerOverlay(
                title: field == .from ? "Where from?" : "Where to?",
                initialText: field == .from ? origin : dest,
                savedPlaces: [],
                recentSearches: [],
                onDismiss: { locationPickerField = nil },
                onConfirm: { address in
                    if field == .from { origin = address } else { dest = address }
                    locationPickerField = nil
                },
                onRouteConfirm: { o, d in
                    origin = o; dest = d
                    locationPickerField = nil
                },
                onRequestAddPlace: { locationPickerField = nil },
                onDeleteRecentSearch: { _ in }
            )
        }
        .task {
            for await effect in wrapper.effects {
                if effect is DriverEffectNavigateToPriceReview {
                    onNavigateToReview()
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "One-off long trip", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func triggerRouteCalcIfReady() {
        guard !origin.isEmpty, !dest.isEmpty else { return }
        wrapper.calculateRouteDistance(originName: origin, destName: dest)
    }
}

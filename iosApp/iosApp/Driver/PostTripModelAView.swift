import SwiftUI
import Shared

// MARK: — DR-06 Post Trip — Model A (Daily Commute) ──────────────────────────

private enum LocationPickerFieldA: Identifiable {
    case from, to
    var id: String { self == .from ? "from" : "to" }
}

struct PostTripModelAView: View {

    var onNavigateToReview: () -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    @State private var origin:    String = ""
    @State private var dest:      String = ""
    @State private var time:      String = "08:00"
    @State private var seats:     Int    = 3
    @State private var selectedDays: Set<String> = ["MON", "TUE", "WED", "THU", "FRI"]
    @State private var locationPickerField: LocationPickerFieldA? = nil

    private let days = [("MON","M"),("TUE","T"),("WED","W"),("THU","T"),("FRI","F"),("SAT","S"),("SUN","S")]

    private var canSubmit: Bool {
        !origin.isEmpty && !dest.isEmpty && !time.isEmpty && seats >= 1 &&
        !selectedDays.isEmpty && wrapper.state.routeDistanceMetres > 0
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Text("Daily Commute")
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

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Days")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        HStack(spacing: HopSpacing.xs) {
                            ForEach(days, id: \.0) { day in
                                let id = day.0
                                let label = day.1
                                let isOn = selectedDays.contains(id)
                                Button {
                                    if isOn { selectedDays.remove(id) } else { selectedDays.insert(id) }
                                } label: {
                                    Text(label)
                                        .font(HopFont.labelMedium(weight: .semibold))
                                        .foregroundColor(isOn ? Color.hopSurface : Color.hopAuthTextPrimary)
                                        .frame(width: 36, height: 36)
                                        .background(isOn ? Color.hopPrimaryLime : Color.hopCardSurfaceMuted)
                                        .clipShape(Circle())
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    HopTextField(label: "Departure time (HH:mm)", placeholder: "08:00", text: $time)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Seats")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(seats)", value: $seats, in: 1...4)
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Next: Review price",
                        variant: .primary,
                        isEnabled: canSubmit && !wrapper.state.isCalculatingRoute
                    ) {
                        let draft = ModelADraft(
                            originName: origin,
                            originLat: 0.0,
                            originLng: 0.0,
                            destName: dest,
                            destLat: 0.0,
                            destLng: 0.0,
                            recurrenceDays: Array(selectedDays).sorted(),
                            departureTime: time,
                            seatsTotal: Int32(seats),
                            distanceMetres: 0  // enriched by DriverViewModel.submitModelADraft
                        )
                        wrapper.submitModelADraft(draft)
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .onChange(of: origin) { _ in triggerRouteCalcIfReady() }
        .onChange(of: dest)   { _ in triggerRouteCalcIfReady() }
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
        DriverTopBar(title: "Daily commute", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func triggerRouteCalcIfReady() {
        guard !origin.isEmpty, !dest.isEmpty else { return }
        wrapper.calculateRouteDistance(originName: origin, destName: dest)
    }
}

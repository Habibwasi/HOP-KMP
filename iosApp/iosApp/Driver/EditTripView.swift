import SwiftUI
import Shared

// MARK: — DR-09b Edit Trip ────────────────────────────────────────────────────

private enum EditLocationPickerField: String, Identifiable {
    case from, to
    var id: String { rawValue }
}

struct EditTripView: View {

    let tripId: String
    var onNavigateToReview: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = EditTripViewModelWrapper.shared
    @State private var toast: String? = nil

    // Form state — pre-populated when trip loads
    @State private var originName: String = ""
    @State private var destName: String = ""
    @State private var departureTime: String = ""
    @State private var selectedDate: String = ""
    @State private var formPopulated = false

    @State private var locationPickerField: EditLocationPickerField? = nil
    @State private var showTimePicker = false

    // Validation
    @State private var originError: String? = nil
    @State private var destError: String? = nil

    private var state: EditTripUiState { wrapper.state }

    var body: some View {
        let s = state

        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            if s.isLoading || s.trip == nil {
                VStack {
                    Spacer()
                    if s.isLoading {
                        ProgressView().tint(Color.hopAuthAccent)
                    } else {
                        Text("Trip not found.")
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }
                    Spacer()
                }
            } else {
                let trip = s.trip!
                let isModelB = trip.model == TripModel.b

                ScrollView {
                    VStack(alignment: .leading, spacing: HopSpacing.lg) {

                        // Route section
                        FormSectionView(title: "Route") {
                            AddressPickerRowView(
                                label: "From",
                                value: originName,
                                placeholder: "e.g. Aarhus C",
                                onTap: { locationPickerField = .from }
                            )
                            if let err = originError {
                                Text(err).font(HopFont.labelSmall()).foregroundColor(.red)
                            }
                            Spacer().frame(height: HopSpacing.sm)
                            AddressPickerRowView(
                                label: "To",
                                value: destName,
                                placeholder: "e.g. Copenhagen Central",
                                onTap: { locationPickerField = .to }
                            )
                            if let err = destError {
                                Text(err).font(HopFont.labelSmall()).foregroundColor(.red)
                            }
                            if !originName.isEmpty && !destName.isEmpty {
                                Spacer().frame(height: HopSpacing.sm)
                                RouteSummaryRowView(
                                    isCalculating: s.isCalculatingRoute,
                                    distanceMetres: Int(s.routeDistanceMetres),
                                    seatsTotal: Int(trip.seatsTotal)
                                )
                            }
                        }

                        // Departure section
                        FormSectionView(title: "Departure") {
                            AddressPickerRowView(
                                label: "Time",
                                value: departureTime.isEmpty ? "Pick time" : departureTime,
                                placeholder: "HH:mm",
                                onTap: { showTimePicker = true }
                            )
                            if isModelB {
                                Spacer().frame(height: HopSpacing.sm)
                                AddressPickerRowView(
                                    label: "Date",
                                    value: selectedDate.isEmpty ? "Pick a date" : selectedDate,
                                    placeholder: "YYYY-MM-DD",
                                    onTap: {}
                                )
                                // iOS native date picker inline
                                DatePicker(
                                    "",
                                    selection: Binding(
                                        get: {
                                            let fmt = DateFormatter()
                                            fmt.dateFormat = "yyyy-MM-dd"
                                            return fmt.date(from: selectedDate) ?? Date()
                                        },
                                        set: { date in
                                            let fmt = DateFormatter()
                                            fmt.dateFormat = "yyyy-MM-dd"
                                            selectedDate = fmt.string(from: date)
                                        }
                                    ),
                                    displayedComponents: .date
                                )
                                .datePickerStyle(.graphical)
                                .tint(Color.hopAuthAccent)
                            }
                        }

                        Spacer().frame(height: 96)
                    }
                    .padding(HopSpacing.md)
                }

                // Sticky CTA
                VStack(spacing: 0) {
                    Divider().opacity(0.12)
                    HopButton(
                        text: "Review Changes",
                        variant: .primary,
                        isLoading: false,
                        isEnabled: !s.isCalculatingRoute,
                        action: {
                            originError = originName.isEmpty ? "Enter a departure location" : nil
                            destError = destName.isEmpty ? "Enter a destination" : nil
                            guard originError == nil, destError == nil else { return }

                            let departsAt = buildDepartsAt(
                                isModelB: isModelB,
                                originalDepartsAt: trip.departsAt,
                                date: selectedDate,
                                time: departureTime
                            )
                            let draft = EditTripDraft(
                                tripId: trip.id,
                                model: trip.model,
                                originName: originName.trimmingCharacters(in: .whitespaces),
                                originLat: 0,
                                originLng: 0,
                                destName: destName.trimmingCharacters(in: .whitespaces),
                                destLat: 0,
                                destLng: 0,
                                departsAt: departsAt,
                                departureTime: departureTime,
                                date: selectedDate,
                                distanceMetres: Int32(s.routeDistanceMetres > 0
                                    ? s.routeDistanceMetres
                                    : trip.trip.distanceMetres),
                                seatsTotal: trip.seatsTotal
                            )
                            wrapper.submitDraft(draft: draft)
                        }
                    )
                    .padding(HopSpacing.md)
                }
                .background(Color.hopBackground)
            }

            // Toast
            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl + 56)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "chevron.backward")
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Edit Trip")
                    .font(HopFont.title())
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
        }
        .task {
            wrapper.loadTrip(tripId: tripId)
        }
        .onChange(of: state.trip) { trip in
            guard let trip, !formPopulated else { return }
            formPopulated = true
            originName = trip.originName
            destName = trip.destName
            let iso = trip.departsAt
            if let tRange = iso.range(of: "T") {
                let timeStart = tRange.upperBound
                departureTime = String(iso[timeStart...].prefix(5))
                selectedDate = String(iso[..<tRange.lowerBound])
            }
        }
        .onChange(of: originName) { _ in recalculateIfNeeded() }
        .onChange(of: destName) { _ in recalculateIfNeeded() }
        .task {
            for await effect in wrapper.effects {
                switch effect {
                case is EditTripEffectNavigateToPriceReview:
                    onNavigateToReview()
                case let snack as EditTripEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        withAnimation { toast = nil }
                    }
                default:
                    break
                }
            }
        }
        .fullScreenCover(item: $locationPickerField) { field in
            LocationPickerOverlay(
                title: field == .from ? "Where from?" : "Where to?",
                initialText: field == .from ? originName : destName,
                savedPlaces: [],
                recentSearches: [],
                onDismiss: { locationPickerField = nil },
                onConfirm: { address in
                    if field == .from { originName = address; originError = nil }
                    else { destName = address; destError = nil }
                    locationPickerField = nil
                },
                onRouteConfirm: { origin, dest in
                    originName = origin; destName = dest
                    originError = nil; destError = nil
                    locationPickerField = nil
                },
                onRequestAddPlace: { locationPickerField = nil }
            )
        }
        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(time: $departureTime, isPresented: $showTimePicker)
        }
    }

    private func recalculateIfNeeded() {
        guard !originName.isEmpty, !destName.isEmpty else { return }
        wrapper.calculateRoute(originName: originName, destName: destName)
    }

    private func buildDepartsAt(isModelB: Bool, originalDepartsAt: String, date: String, time: String) -> String {
        let useDate: String
        if isModelB {
            useDate = date.isEmpty ? String(originalDepartsAt.prefix(10)) : date
        } else {
            useDate = String(originalDepartsAt.prefix(10))
        }
        let useTime = time.isEmpty ? "08:00" : time
        return "\(useDate)T\(useTime):00.000Z"
    }
}

// MARK: — Time picker sheet ───────────────────────────────────────────────────

private struct TimePickerSheet: View {
    @Binding var time: String
    @Binding var isPresented: Bool

    @State private var selection: Date = Date()

    var body: some View {
        VStack(spacing: HopSpacing.md) {
            DatePicker("", selection: $selection, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .environment(\.locale, Locale(identifier: "en_GB")) // 24h
            HopButton(text: "Set Time", variant: .primary, isLoading: false, isEnabled: true) {
                let cal = Calendar.current
                let h = String(format: "%02d", cal.component(.hour, from: selection))
                let m = String(format: "%02d", cal.component(.minute, from: selection))
                time = "\(h):\(m)"
                isPresented = false
            }
            .padding(.horizontal, HopSpacing.md)
        }
        .padding(.top, HopSpacing.lg)
        .presentationDetents([.medium])
    }
}

// MARK: — DR-09c Edit Trip Price Review ───────────────────────────────────────

struct EditTripPriceReviewView: View {

    var onSaved: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = EditTripViewModelWrapper.shared
    @State private var toast: String? = nil

    private var state: EditTripUiState { wrapper.state }

    var body: some View {
        let s = state
        let draft = s.pendingDraft
        let distanceMetres = Int32(draft?.distanceMetres ?? 0)
        let seatsTotal = Int32(draft?.seatsTotal ?? 1)

        let priceResult = distanceMetres > 0
            ? PricingEngine.shared.calculate(distanceMetres: distanceMetres, seatsTotal: seatsTotal)
            : nil

        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    if let draft {
                        // Updated route card
                        VStack(alignment: .leading, spacing: HopSpacing.sm) {
                            Text("UPDATED ROUTE")
                                .font(HopFont.labelSmall(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextSecondary)
                                .tracking(0.5)
                            Divider().opacity(0.1)
                            EditSummaryRow(label: "From", value: draft.originName.isEmpty ? "—" : draft.originName)
                            EditSummaryRow(label: "To", value: draft.destName.isEmpty ? "—" : draft.destName)
                            EditSummaryRow(label: "Departure", value: draft.departureTime.isEmpty ? "—" : draft.departureTime)
                            if draft.model == TripModel.b {
                                EditSummaryRow(label: "Date", value: draft.date.isEmpty ? "—" : draft.date)
                            }
                        }
                        .padding(HopSpacing.md)
                        .background(Color.hopCardSurfaceMuted)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(Color.hopAuthTextSecondary.opacity(0.12), lineWidth: 1))
                    }

                    // Price breakdown
                    if let price = priceResult {
                        EditPriceBreakdownCard(
                            distanceMetres: Int(distanceMetres),
                            seatsTotal: Int(seatsTotal),
                            pricePerSeatOere: Int(price.pricePerSeatOere)
                        )
                    }

                    // Tax warning
                    SkatWarningCard()

                    Spacer().frame(height: 96)
                }
                .padding(HopSpacing.md)
            }

            VStack(spacing: 0) {
                Divider().opacity(0.12)
                HopButton(
                    text: "Save Changes",
                    variant: .primary,
                    isLoading: s.isSaving,
                    isEnabled: priceResult != nil && !s.isSaving,
                    action: { wrapper.confirmAndUpdate() }
                )
                .padding(HopSpacing.md)
            }
            .background(Color.hopBackground)

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl + 56)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "chevron.backward")
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Review Changes")
                    .font(HopFont.title())
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
        }
        .task {
            for await effect in wrapper.effects {
                switch effect {
                case is EditTripEffectNavigateBack:
                    onSaved()
                case let snack as EditTripEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        withAnimation { toast = nil }
                    }
                default:
                    break
                }
            }
        }
    }
}

// MARK: — Shared form section wrapper ─────────────────────────────────────────

private struct FormSectionView<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text(title.uppercased())
                .font(HopFont.labelSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextSecondary)
                .tracking(0.5)
            content()
        }
    }
}

// MARK: — Helpers ─────────────────────────────────────────────────────────────

private struct EditSummaryRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopAuthTextSecondary)
                .frame(width: 90, alignment: .leading)
            Text(value)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct EditPriceBreakdownCard: View {
    let distanceMetres: Int
    let seatsTotal: Int
    let pricePerSeatOere: Int

    var body: some View {
        let priceOere = pricePerSeatOere
        let priceKr = priceOere / 100
        let priceRem = priceOere % 100
        let priceDisplay = priceRem == 0 ? "DKK \(priceKr)" : "DKK \(priceKr),\(String(format: "%02d", priceRem))"

        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("PRICE BREAKDOWN")
                .font(HopFont.labelSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextSecondary)
                .tracking(0.5)
            Divider().opacity(0.1)
            EditSummaryRow(label: "Per seat", value: priceDisplay)
            EditSummaryRow(label: "Distance", value: "\(distanceMetres / 1000) km")
            EditSummaryRow(label: "Seats", value: "\(seatsTotal)")
        }
        .padding(HopSpacing.md)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(Color.hopAuthTextSecondary.opacity(0.12), lineWidth: 1))
    }
}

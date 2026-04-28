import SwiftUI
import Shared

// MARK: — PA-01 Passenger Home ─────────────────────────────────────────────────
//
// Modern redesign to match Android counterpart:
//   • Lime gradient header band behind greeting
//   • SearchHero: tappable From/To rows with timeline rail + swap button
//   • DatePill + SeatsPill inline below hero, full-width Search CTA
//   • LocationPickerSheet: saved-place chips + recent-search rows at top
//   • ActiveBookingBanner: white card, lime accent stripe, live dot
//   • "Upcoming trips" section with TripCard list / loading / empty state

struct PassengerHomeView: View {

    /// Called when the user taps "Search" — pushes PA-02 Search Results.
    var onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void

    /// Called when the role toggle switches to driver mode.
    var onSwitchToDriver: () -> Void

    var navigate: (HopRoute) -> Void

    // ── Local form state ──────────────────────────────────────────────────────
    @State private var origin           = ""
    @State private var destination      = ""
    @State private var selectedDate     = "Today"
    @State private var seats            = 1
    @State private var selectedRole: HopRole = .passenger

    // Picker sheet state
    @State private var activePickerField: PickerField? = nil
    @State private var showDatePicker                  = false
    @State private var showSeatPicker                  = false

    @StateObject private var tripWrapper = TripViewModelWrapper()

    enum PickerField { case from, to }

    private var canSearch: Bool {
        !origin.trimmingCharacters(in: .whitespaces).isEmpty &&
        !destination.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        ZStack(alignment: .top) {
            Color.hopBackground.ignoresSafeArea()

            // Lime gradient header band
            LinearGradient(
                colors: [Color.hopPrimaryLime.opacity(0.22), Color.hopBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: 260)
            .ignoresSafeArea(edges: .top)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    // ── Greeting ──────────────────────────────────────────────
                    GreetingRow()
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.top, HopSpacing.md)
                        .padding(.bottom, HopSpacing.sm)

                    // ── Search hero ───────────────────────────────────────────
                    SearchHero(
                        origin:       origin,
                        destination:  destination,
                        selectedDate: selectedDate,
                        seats:        seats,
                        canSearch:    canSearch,
                        onFromTap:    { activePickerField = .from },
                        onToTap:      { activePickerField = .to },
                        onSwap: {
                            let tmp = origin
                            origin = destination
                            destination = tmp
                        },
                        onDateTap:   { showDatePicker = true },
                        onSeatsTap:  { showSeatPicker = true },
                        onSearch: {
                            onSearch(
                                origin.trimmingCharacters(in: .whitespaces),
                                destination.trimmingCharacters(in: .whitespaces),
                                selectedDate,
                                seats
                            )
                        }
                    )
                    .padding(.horizontal, HopSpacing.md)

                    // ── Upcoming trips heading ────────────────────────────────
                    Text("Upcoming trips")
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.top, HopSpacing.xl)
                        .padding(.bottom, HopSpacing.sm)

                    // ── Loading / empty / trip list ───────────────────────────
                    if tripWrapper.state.isLoading {
                        HStack {
                            Spacer()
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                                .scaleEffect(1.2)
                            Spacer()
                        }
                        .padding(.vertical, HopSpacing.xl)
                    } else if tripWrapper.state.trips.isEmpty {
                        EmptyState(
                            systemImage: "car.2",
                            headline: "No upcoming trips",
                            subtitle: "Find a ride and book your first trip",
                            ctaLabel: "Find rides",
                            ctaAction: {
                                onSearch(
                                    origin.trimmingCharacters(in: .whitespaces),
                                    destination.trimmingCharacters(in: .whitespaces),
                                    selectedDate,
                                    seats
                                )
                            }
                        )
                        .padding(.horizontal, HopSpacing.md)
                    } else {
                        VStack(spacing: HopSpacing.sm) {
                            ForEach(tripWrapper.state.trips, id: \.id) { tripUi in
                                TripCard(data: tripUi.toHomeCardData()) {
                                    guard !tripUi.isBroken else { return }
                                    navigate(.tripDetail(id: tripUi.id))
                                }
                                .opacity(tripUi.isBroken ? 0.6 : 1.0)
                                .disabled(tripUi.isBroken)
                                .accessibilityHint(tripUi.isBroken ? "This trip is unavailable" : "Double-tap to view details")
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                    }

                    Spacer().frame(height: HopSpacing.xxl)
                }
            }
        }
        .navigationBarHidden(true)
        .task {
            tripWrapper.startObserving { _ in }
            tripWrapper.loadMyTripsPassenger()
        }
        // ── Location picker sheet ─────────────────────────────────────────────
        .sheet(item: $activePickerField) { field in
            LocationPickerSheet(
                title: field == .from ? "Where from?" : "Where to?",
                onConfirm: { address in
                    if field == .from { origin = address }
                    else { destination = address }
                    activePickerField = nil
                },
                onRouteConfirm: { orig, dest in
                    origin = orig
                    destination = dest
                    activePickerField = nil
                }
            )
        }
        // ── Date picker ───────────────────────────────────────────────────────
        .sheet(isPresented: $showDatePicker) {
            DatePickerSheet(selectedDate: $selectedDate, isPresented: $showDatePicker)
        }
        // ── Seat picker ───────────────────────────────────────────────────────
        .sheet(isPresented: $showSeatPicker) {
            SeatPickerSheet(seats: $seats, isPresented: $showSeatPicker)
        }
    }
}

// Make PickerField Identifiable for .sheet(item:)
extension PassengerHomeView.PickerField: Identifiable {
    var id: Int { self == .from ? 0 : 1 }
}

// MARK: — GreetingRow

private struct GreetingRow: View {
    private var greeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<12:  return "Good morning"
        case 12..<17: return "Good afternoon"
        case 17..<22: return "Good evening"
        default:      return "Travelling late?"
        }
    }

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.55))
                    .frame(width: 36, height: 36)
                Text("H")
                    .font(HopFont.bodyLarge(weight: .bold))
                    .foregroundColor(Color.hopTextPrimary)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text("\(greeting) 👋")
                    .font(HopFont.headlineSmall(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Text("Where are you headed today?")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextPrimary.opacity(0.6))
            }
        }
    }
}

// MARK: — SearchHero

private struct SearchHero: View {
    let origin:       String
    let destination:  String
    let selectedDate: String
    let seats:        Int
    let canSearch:    Bool
    let onFromTap:    () -> Void
    let onToTap:      () -> Void
    let onSwap:       () -> Void
    let onDateTap:    () -> Void
    let onSeatsTap:   () -> Void
    let onSearch:     () -> Void

    var body: some View {
        let cardShape = RoundedRectangle(cornerRadius: 24)

        VStack(spacing: 0) {
            // ── From / To with timeline rail + swap ──────────────────────────
            ZStack(alignment: .trailing) {
                HStack(alignment: .center, spacing: 10) {
                    // Timeline rail
                    TimelineRail()
                        .frame(width: 20)

                    // From / To rows
                    VStack(spacing: 0) {
                        Button(action: onFromTap) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("From")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: 0x888888))
                                Text(origin.isEmpty ? "Where from?" : origin)
                                    .font(HopFont.bodyMedium(weight: origin.isEmpty ? .regular : .medium))
                                    .foregroundColor(origin.isEmpty ? Color(hex: 0xB8B8B8) : Color(hex: 0x1A1A1A))
                                    .lineLimit(1)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.trailing, 48)
                            .padding(.vertical, HopSpacing.sm)
                        }
                        .buttonStyle(.plain)

                        Divider()
                            .background(Color(hex: 0xEEEEEE))
                            .padding(.leading, 0)

                        Button(action: onToTap) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("To")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: 0x888888))
                                Text(destination.isEmpty ? "Where to?" : destination)
                                    .font(HopFont.bodyMedium(weight: destination.isEmpty ? .regular : .medium))
                                    .foregroundColor(destination.isEmpty ? Color(hex: 0xB8B8B8) : Color(hex: 0x1A1A1A))
                                    .lineLimit(1)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.trailing, 48)
                            .padding(.vertical, HopSpacing.sm)
                        }
                        .buttonStyle(.plain)
                    }
                }

                // Swap button
                Button(action: onSwap) {
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 36, height: 36)
                            .overlay(Circle().stroke(Color(hex: 0xDDDDDD), lineWidth: 1))
                        Image(systemName: "arrow.up.arrow.down")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(Color(hex: 0x666666))
                    }
                }
                .buttonStyle(.plain)
                .padding(.trailing, HopSpacing.xs)
                .accessibilityLabel("Swap origin and destination")
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.sm)

            Divider()
                .background(Color(hex: 0xEEEEEE))
                .padding(.horizontal, HopSpacing.md)

            // ── Pills ─────────────────────────────────────────────────────────
            HStack(spacing: HopSpacing.xs) {
                DatePill(selectedDate: selectedDate, onTap: onDateTap)
                SeatsPill(seats: seats, onTap: onSeatsTap)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.sm)

            // ── Search CTA ────────────────────────────────────────────────────
            HopPrimaryButton(title: "Search", isEnabled: canSearch, action: onSearch)
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.sm)
        }
        .background(Color.white)
        .clipShape(cardShape)
        .shadow(color: Color.black.opacity(0.1), radius: 6, x: 0, y: 3)
    }
}

// MARK: — TimelineRail

private struct TimelineRail: View {
    var body: some View {
        VStack(spacing: 0) {
            Circle()
                .fill(Color.hopPrimaryGreen)
                .frame(width: 10, height: 10)
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [Color.hopPrimaryGreen.opacity(0.35), Color.hopError.opacity(0.35)],
                        startPoint: .top, endPoint: .bottom
                    )
                )
                .frame(width: 2, height: 34)
            Image(systemName: "location.fill")
                .font(.system(size: 10))
                .foregroundColor(Color.hopError)
        }
    }
}

// MARK: — DatePill

private struct DatePill: View {
    let selectedDate: String
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 4) {
                Image(systemName: "calendar")
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopTextSecondary)
                Text(selectedDate)
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(Color(hex: 0x1A1A1A))
                Image(systemName: "chevron.down")
                    .font(.system(size: 10))
                    .foregroundColor(Color.hopTextSecondary)
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 6)
            .overlay(Capsule().stroke(Color(hex: 0xDDDDDD), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: — SeatsPill

private struct SeatsPill: View {
    let seats: Int
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 4) {
                Image(systemName: "person.fill")
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopTextSecondary)
                Text(seats == 1 ? "1 seat" : "\(seats) seats")
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(Color(hex: 0x1A1A1A))
                Image(systemName: "chevron.down")
                    .font(.system(size: 10))
                    .foregroundColor(Color.hopTextSecondary)
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 6)
            .overlay(Capsule().stroke(Color(hex: 0xDDDDDD), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: — LocationPickerSheet

private struct LocationPickerSheet: View {
    let title: String
    let onConfirm: (String) -> Void
    let onRouteConfirm: (String, String) -> Void

    @State private var searchText = ""
    @Environment(\.dismiss) private var dismiss

    // Synthetic recent routes — in production these would come from HomeStatsViewModel
    private let recentRoutes: [(origin: String, dest: String)] = [
        ("Copenhagen Central", "Aarhus"),
        ("Hellerup", "Odense"),
    ]

    // Synthetic saved places — in production from SavedPlacesViewModel
    private let savedPlaces: [(label: String, icon: String)] = [
        ("Home", "house.fill"),
        ("Work", "briefcase.fill"),
    ]

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 0) {
                // Search field
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(Color.hopTextSecondary)
                    TextField(title, text: $searchText)
                        .font(HopFont.bodyMedium())
                        .autocorrectionDisabled()
                }
                .padding(HopSpacing.sm)
                .background(Color(hex: 0xF1F3F4))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.sm)

                if searchText.isEmpty {
                    // ── Quick chips ───────────────────────────────────────────
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: HopSpacing.xs) {
                            // Current location chip
                            Label("Current", systemImage: "location.fill")
                                .font(HopFont.labelSmall(weight: .medium))
                                .foregroundColor(Color.hopPrimaryGreen)
                                .padding(.horizontal, HopSpacing.sm)
                                .padding(.vertical, 5)
                                .background(Color.hopPrimaryLime.opacity(0.15))
                                .overlay(Capsule().stroke(Color.hopPrimaryLime, lineWidth: 1))
                                .clipShape(Capsule())

                            // Saved place chips
                            ForEach(savedPlaces, id: \.label) { place in
                                Button {
                                    onConfirm(place.label)
                                } label: {
                                    Label(place.label, systemImage: place.icon)
                                        .font(HopFont.labelSmall(weight: .medium))
                                        .foregroundColor(Color.hopTextPrimary)
                                        .padding(.horizontal, HopSpacing.sm)
                                        .padding(.vertical, 5)
                                        .background(Color(hex: 0xF7F8FA))
                                        .overlay(Capsule().stroke(Color(hex: 0xE7EAEE), lineWidth: 1))
                                        .clipShape(Capsule())
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)
                    }

                    Divider().padding(.horizontal, HopSpacing.md)

                    // ── Recent routes ─────────────────────────────────────────
                    if !recentRoutes.isEmpty {
                        Text("Recent")
                            .font(HopFont.labelSmall(weight: .semibold))
                            .foregroundColor(Color.hopTextSecondary)
                            .padding(.horizontal, HopSpacing.md)
                            .padding(.top, HopSpacing.sm)

                        ForEach(recentRoutes.indices, id: \.self) { idx in
                            let route = recentRoutes[idx]
                            Button {
                                onRouteConfirm(route.origin, route.dest)
                            } label: {
                                HStack {
                                    Image(systemName: "clock")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color.hopTextSecondary)
                                    Text("\(route.origin) → \(route.dest)")
                                        .font(HopFont.bodySmall())
                                        .foregroundColor(Color.hopTextPrimary)
                                        .lineLimit(1)
                                    Spacer()
                                }
                                .padding(.horizontal, HopSpacing.md)
                                .padding(.vertical, HopSpacing.sm)
                            }
                            .buttonStyle(.plain)

                            if idx < recentRoutes.count - 1 {
                                Divider().padding(.leading, 44)
                            }
                        }
                    }
                } else {
                    // Placeholder for autocomplete results
                    // (Google Places integration reuses existing iOS Places client)
                    Text("Type to search…")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.top, HopSpacing.md)
                }

                Spacer()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }
}

// MARK: — DatePickerSheet

private struct DatePickerSheet: View {
    @Binding var selectedDate: String
    @Binding var isPresented: Bool

    private let quickOptions = ["Today", "Tomorrow"]

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(quickOptions, id: \.self) { opt in
                    Button {
                        selectedDate = opt
                        isPresented = false
                    } label: {
                        HStack {
                            Text(opt)
                                .font(HopFont.bodyLarge())
                                .foregroundColor(Color.hopTextPrimary)
                            Spacer()
                            if selectedDate == opt {
                                Image(systemName: "checkmark")
                                    .foregroundColor(Color.hopPrimaryGreen)
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, HopSpacing.md)
                }
                Spacer()
            }
            .navigationTitle("Select date")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { isPresented = false }
                }
            }
        }
        .presentationDetents([.fraction(0.35)])
        .presentationDragIndicator(.visible)
    }
}

// MARK: — SeatPickerSheet

private struct SeatPickerSheet: View {
    @Binding var seats: Int
    @Binding var isPresented: Bool

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(1...4, id: \.self) { n in
                    Button {
                        seats = n
                        isPresented = false
                    } label: {
                        HStack(spacing: HopSpacing.md) {
                            ZStack {
                                Circle()
                                    .fill(n == seats ? Color.hopPrimaryLime : Color(hex: 0xF7F8FA))
                                    .frame(width: 32, height: 32)
                                Image(systemName: "person.fill")
                                    .font(.system(size: 14))
                                    .foregroundColor(n == seats ? Color(hex: 0x1A1A1A) : Color.hopTextSecondary)
                            }
                            Text(n == 1 ? "1 seat" : "\(n) seats")
                                .font(HopFont.bodyLarge(weight: n == seats ? .semibold : .regular))
                                .foregroundColor(Color.hopTextPrimary)
                            Spacer()
                            if n == seats {
                                Image(systemName: "checkmark")
                                    .foregroundColor(Color.hopPrimaryGreen)
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)
                    }
                    .buttonStyle(.plain)
                    if n < 4 {
                        Divider().padding(.leading, 56)
                    }
                }
                Spacer()
            }
            .navigationTitle("How many seats?")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { isPresented = false }
                }
            }
        }
        .presentationDetents([.fraction(0.45)])
        .presentationDragIndicator(.visible)
    }
}

// MARK: — TripUiModel → TripCardData

private extension TripUiModel {
    func toHomeCardData() -> TripCardData {
        TripCardData(
            driverName:       trip.driverId,
            driverImageURL:   nil,
            driverRating:     4.5,
            reviewCount:      0,
            originName:       trip.originName,
            destName:         trip.destName,
            departsAt:        homeShortDate(iso: trip.departsAt),
            badgeStatus:      trip.model == .b ? .modelB : .modelA,
            priceOerePerSeat: Int(trip.priceOerePerSeat)
        )
    }

    private func homeShortDate(iso: String) -> String {
        let fmt = DateFormatter()
        fmt.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        fmt.timeZone = TimeZone(identifier: "UTC")
        if let d = fmt.date(from: iso) {
            let out = DateFormatter()
            out.dateFormat = "d MMM, HH:mm"
            return out.string(from: d)
        }
        return iso
    }
}

// MARK: — Previews

#Preview("PA-01 Passenger Home — Default") {
    PassengerHomeView(
        onSearch:         { _, _, _, _ in },
        onSwitchToDriver: {},
        navigate:         { _ in }
    )
}

#Preview("PA-01 Passenger Home — Filled") {
    PassengerHomeView(
        onSearch:         { _, _, _, _ in },
        onSwitchToDriver: {},
        navigate:         { _ in }
    )
}
//
//  PassengerHomeView.swift
//  iosApp
//
//  PA-01 — Passenger Home (light-theme, 1:1 mirror of PassengerHomeScreen.kt)
//

import SwiftUI
import Shared

// MARK: — LocationPickerField (Identifiable wrapper for fullScreenCover) ────

fileprivate enum LocationPickerField: String, Identifiable {
    case from
    case to
    var id: String { rawValue }
}

// MARK: — PassengerHomeView ─────────────────────────────────────────────────

struct PassengerHomeView: View {

    /// Forwarded by HomeView → HopTabView → HopNavigationStack so it can capture
    /// search params before pushing `.searchResults`.
    let onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void
    let onSwitchToDriver: () -> Void
    let navigate: (HopRoute) -> Void

    // ── ViewModels (Koin) ─────────────────────────────────────────────────
    @StateObject private var statsWrapper  = HomeStatsViewModelWrapper()
    @StateObject private var placesWrapper = SavedPlacesViewModelWrapper()
    @StateObject private var searchWrapper = SearchViewModelWrapper()
    @StateObject private var tripsWrapper  = MyTripsPassengerViewModelWrapper()
    @StateObject private var authWrapper   = AuthViewModelWrapper()

    // ── Local ephemeral form state ────────────────────────────────────────
    @State private var fromLocation: String = ""
    @State private var toLocation:   String = ""
    @State private var selectedDate: String = "Today"
    @State private var seats:        Int    = 1
    @State private var showAddPlaceSheet: Bool = false
    @State private var showDatePicker:    Bool = false
    @State private var showSeatPicker:    Bool = false
    @State private var locationPickerField: LocationPickerField? = nil   // .from / .to / nil
    @State private var headerExpanded: Bool = true
    @State private var snackbar: String? = nil

    // ── Derived helpers ───────────────────────────────────────────────────
    private var firstName: String? {
        let full = (authWrapper.state.currentUser?.fullName ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        guard !full.isEmpty else { return nil }
        return full.split(separator: " ").first.map(String.init)
    }

    /// Soonest CONFIRMED upcoming trip from local list — fallback when the
    /// server `activeBooking` hasn't loaded yet.
    private var nextActiveFallback: TripUiModel? {
        let upcoming = tripsWrapper.state.upcomingTrips
        return upcoming.min { lhs, rhs in lhs.departsAt < rhs.departsAt }
    }

    var body: some View {
        ZStack(alignment: .top) {
            Color.hopBackground.ignoresSafeArea()

            // Lime → background gradient band behind the greeting.
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.hopPrimaryLime.opacity(0.22),
                    Color.hopBackground
                ]),
                startPoint: .top,
                endPoint:   .bottom
            )
            .frame(height: 260)
            .frame(maxWidth: .infinity, alignment: .top)
            .ignoresSafeArea(edges: .top)

            VStack(spacing: 0) {
                // ── Greeting (collapses on scroll) ────────────────────────
                if headerExpanded {
                    GreetingBanner(firstName: firstName)
                        .transition(.opacity.combined(with: .move(edge: .top)))
                }

                // ── Scroll body ───────────────────────────────────────────
                ScrollView {
                    // Track scroll offset to collapse the header.
                    GeometryReader { proxy in
                        Color.clear.preference(
                            key: ScrollOffsetKey.self,
                            value: proxy.frame(in: .named("home-scroll")).minY
                        )
                    }
                    .frame(height: 0)

                    VStack(spacing: HopSpacing.md) {
                        // Active-booking banner (server source preferred,
                        // fall back to soonest local upcoming trip).
                        if let active = statsWrapper.state.activeBooking {
                            ActiveBookingBanner(
                                departureIso: active.departsAt,
                                origin: active.originName,
                                destination: active.destName,
                                onTap: { navigate(.tripDetailActive(bookingId: active.id)) }
                            )
                        } else if let fallback = nextActiveFallback {
                            ActiveBookingBanner(
                                departureIso: fallback.departsAt,
                                origin: fallback.originName,
                                destination: fallback.destName,
                                onTap: { navigate(.tripDetailActive(bookingId: fallback.id)) }
                            )
                        }

                        // Search hero
                        SearchHero(
                            fromLocation: fromLocation,
                            toLocation:   toLocation,
                            selectedDate: selectedDate,
                            seats:        seats,
                            onFromClick: { locationPickerField = LocationPickerField.from },
                            onToClick:   { locationPickerField = LocationPickerField.to },
                            onSwap: {
                                let tmp = fromLocation
                                fromLocation = toLocation
                                toLocation = tmp
                                #if canImport(UIKit)
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                #endif
                            },
                            onPickDate:  { showDatePicker = true },
                            onPickSeats: { showSeatPicker = true },
                            onSearch: {
                                #if canImport(UIKit)
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                #endif
                                searchWrapper.search(
                                    origin: fromLocation,
                                    dest:   toLocation,
                                    date:   selectedDate,
                                    seats:  seats
                                )
                                onSearch(fromLocation, toLocation, selectedDate, seats)
                            }
                        )

                        // Tips & announcements pager
                        TipsPager(tips: DefaultHomeTips)

                        // Trust stats (rating × completed × CO₂)
                        let userStats = statsWrapper.state.stats
                        let ratingTimes10: Int = {
                            guard let r = userStats?.averageRating else { return 0 }
                            return Int(Double(truncating: r) * 10.0)
                        }()
                        let completed: Int = Int(userStats?.completedTrips ?? Int32(tripsWrapper.state.upcomingTrips.count))
                        TrustStatsCard(
                            ratingTimes10: ratingTimes10,
                            completedTrips: completed,
                            co2SavedKg: 0
                        )

                        // ── Section heading ──────────────────────────────
                        HStack {
                            Text("Upcoming trips")
                                .font(HopFont.titleMedium())
                                .foregroundColor(Color.hopAuthTextPrimary)
                            Spacer()
                        }
                        .padding(.top, HopSpacing.xs)

                        // Loading / empty / list
                        if tripsWrapper.state.isLoading || tripsWrapper.state.upcomingTrips.isEmpty {
                            AnimatedLoadingCar(
                                caption: tripsWrapper.state.isLoading
                                    ? "Finding rides..."
                                    : "No upcoming trips yet — search above to find a ride."
                            )
                            .padding(.vertical, HopSpacing.lg)
                        } else {
                            VStack(spacing: HopSpacing.sm) {
                                ForEach(tripsWrapper.state.upcomingTrips, id: \.id) { trip in
                                    TripCardLight(
                                        driverName: "Driver",
                                        driverInitials: "D",
                                        driverRating: 0.0,
                                        originName: trip.originName,
                                        destinationName: trip.destName,
                                        departureTime: trip.departsAt,
                                        badgeStatus: trip.model == .a ? .modelA : .modelB,
                                        pricePerSeatOere: Int(trip.priceOerePerSeat),
                                        isBooked: trip.bookingId != nil,
                                        onTap: {
                                            let bookingId = trip.bookingId ?? trip.id
                                            navigate(.tripDetailActive(bookingId: bookingId))
                                        }
                                    )
                                }
                            }
                        }

                        Spacer().frame(height: HopSpacing.md)
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.md)
                }
                .coordinateSpace(name: "home-scroll")
                .onPreferenceChange(ScrollOffsetKey.self) { value in
                    let expanded = value > -200
                    if expanded != headerExpanded {
                        withAnimation(.easeInOut(duration: 0.18)) {
                            headerExpanded = expanded
                        }
                    }
                }
                .refreshable {
                    statsWrapper.load()
                    placesWrapper.load()
                    tripsWrapper.load()
                }
            }
        }
        // ── Snackbar ──────────────────────────────────────────────────────
        .overlay(alignment: .bottom) {
            if let msg = snackbar {
                Text(msg)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.white)
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)
                    .background(Color.black.opacity(0.85))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .padding(.bottom, HopSpacing.lg)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        // ── Sheets / overlays ─────────────────────────────────────────────
        .sheet(isPresented: $showSeatPicker) {
            SeatPickerSheet(currentSeats: seats) { picked in
                seats = picked
                showSeatPicker = false
            }
            .presentationDetents([.medium])
        }
        .sheet(isPresented: $showDatePicker) {
            DatePickerSheet(currentDate: selectedDate) { picked in
                selectedDate = picked
                showDatePicker = false
            }
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showAddPlaceSheet) {
            AddSavedPlaceSheet(
                onSave: { label, address, kind in
                    placesWrapper.add(
                        label: label,
                        address: address,
                        kind: kind
                    )
                    showAddPlaceSheet = false
                },
                onDismiss: { showAddPlaceSheet = false }
            )
            .presentationDetents([.medium, .large])
        }
        .fullScreenCover(item: $locationPickerField) { (field: LocationPickerField) in
            LocationPickerOverlay(
                title: field == LocationPickerField.from ? "Where from?" : "Where to?",
                initialText: field == LocationPickerField.from ? fromLocation : toLocation,
                savedPlaces: placesWrapper.state.places,
                recentSearches: statsWrapper.state.recentSearches,
                onDismiss: { locationPickerField = nil },
                onConfirm: { address in
                    if field == LocationPickerField.from { fromLocation = address }
                    else { toLocation = address }
                    locationPickerField = nil
                },
                onRouteConfirm: { origin, dest in
                    fromLocation = origin
                    toLocation = dest
                    locationPickerField = nil
                },
                onRequestAddPlace: {
                    locationPickerField = nil
                    showAddPlaceSheet = true
                },
                onDeleteRecentSearch: { recentId in
                    statsWrapper.deleteRecentSearch(id: recentId)
                }
            )
        }
        .onAppear {
            authWrapper.startObserving()
            statsWrapper.startObserving { eff in
                if let err = eff as? HomeStatsEffectShowError {
                    showSnack(err.message)
                }
            }
            placesWrapper.startObserving { eff in
                if let err = eff as? SavedPlacesEffectShowError {
                    showSnack(err.message)
                }
            }
            searchWrapper.startObserving { _ in }
            tripsWrapper.startObserving { _ in }

            statsWrapper.load()
            placesWrapper.load()
            tripsWrapper.load()
        }
    }

    private func showSnack(_ msg: String) {
        withAnimation { snackbar = msg }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation { if snackbar == msg { snackbar = nil } }
        }
    }
}

// MARK: — ScrollOffsetKey ───────────────────────────────────────────────────

private struct ScrollOffsetKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}


// MARK: — SearchHero ────────────────────────────────────────────────────────

private struct SearchHero: View {
    let fromLocation: String
    let toLocation:   String
    let selectedDate: String
    let seats:        Int
    let onFromClick:  () -> Void
    let onToClick:    () -> Void
    let onSwap:       () -> Void
    let onPickDate:   () -> Void
    let onPickSeats:  () -> Void
    let onSearch:     () -> Void

    private var canSearch: Bool {
        !fromLocation.trimmingCharacters(in: .whitespaces).isEmpty &&
        !toLocation.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        VStack(spacing: 0) {
            // ── From / To with timeline rail + swap ──────────────────────
            ZStack(alignment: .topLeading) {
                TimelineRail()
                    .padding(.top, 18)
                    .padding(.bottom, 18)

                VStack(spacing: 0) {
                    Button(action: onFromClick) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("From")
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color(hex: "#888888"))
                            Text(fromLocation.isEmpty ? "Where from?" : fromLocation)
                                .font(HopFont.bodyMedium(weight: fromLocation.isEmpty ? .regular : .medium))
                                .foregroundColor(fromLocation.isEmpty
                                    ? Color(hex: "#B8B8B8")
                                    : Color(hex: "#1A1A1A"))
                                .lineLimit(1)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, HopSpacing.sm)
                        .padding(.bottom, HopSpacing.sm)
                        .padding(.trailing, 44)
                    }
                    .buttonStyle(.plain)

                    Divider().background(Color(hex: "#EEEEEE"))

                    Button(action: onToClick) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("To")
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color(hex: "#888888"))
                            Text(toLocation.isEmpty ? "Where to?" : toLocation)
                                .font(HopFont.bodyMedium(weight: toLocation.isEmpty ? .regular : .medium))
                                .foregroundColor(toLocation.isEmpty
                                    ? Color(hex: "#B8B8B8")
                                    : Color(hex: "#1A1A1A"))
                                .lineLimit(1)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, HopSpacing.sm)
                        .padding(.bottom, HopSpacing.sm)
                        .padding(.trailing, 44)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.leading, 28)

                // Swap button (centred vertically on the right)
                HStack {
                    Spacer()
                    Button(action: onSwap) {
                        Image(systemName: "arrow.up.arrow.down")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(hex: "#666666"))
                            .frame(width: 36, height: 36)
                            .background(Color.white)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(Color(hex: "#DDDDDD"), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                    .padding(.trailing, HopSpacing.xs)
                }
                .frame(maxHeight: .infinity)
            }
            .frame(minHeight: 110)

            Divider().background(Color(hex: "#EEEEEE"))
            Spacer().frame(height: HopSpacing.sm)

            // ── Date + Seats pills ───────────────────────────────────────
            HStack(spacing: HopSpacing.xs) {
                DatePill(selectedDate: selectedDate, onClick: onPickDate)
                SeatsPill(seats: seats, onClick: onPickSeats)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Spacer().frame(height: HopSpacing.sm)

            // ── Search CTA ───────────────────────────────────────────────
            HopButton(
                text: "Search",
                variant: .primary,
                isEnabled: canSearch,
                action: onSearch
            )
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.md)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .shadow(color: Color.black.opacity(0.10), radius: 6, x: 0, y: 2)
    }
}

// MARK: — TimelineRail ──────────────────────────────────────────────────────

private struct TimelineRail: View {
    var body: some View {
        VStack(spacing: 0) {
            Circle()
                .fill(Color.hopPrimaryGreen)
                .frame(width: 10, height: 10)
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.hopPrimaryGreen.opacity(0.35),
                    Color.hopError.opacity(0.35)
                ]),
                startPoint: .top,
                endPoint:   .bottom
            )
            .frame(width: 2, height: 34)
            Image(systemName: "mappin.circle.fill")
                .font(.system(size: 14))
                .foregroundColor(Color.hopError)
        }
        .frame(width: 20)
    }
}

// MARK: — DatePill ──────────────────────────────────────────────────────────

private struct DatePill: View {
    let selectedDate: String
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 4) {
                Image(systemName: "calendar")
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopAuthTextSecondary)
                Text(selectedDate)
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(Color(hex: "#1A1A1A"))
                Image(systemName: "chevron.down")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 6)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(hex: "#DDDDDD"), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: — SeatsPill ─────────────────────────────────────────────────────────

private struct SeatsPill: View {
    let seats: Int
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 4) {
                Image(systemName: "person.fill")
                    .font(.system(size: 12))
                    .foregroundColor(Color.hopAuthTextSecondary)
                Text(seats == 1 ? "1 seat" : "\(seats) seats")
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(Color(hex: "#1A1A1A"))
                Image(systemName: "chevron.down")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 6)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(hex: "#DDDDDD"), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: — SeatPickerSheet ──────────────────────────────────────────────────

private struct SeatPickerSheet: View {
    let currentSeats: Int
    let onSeatsSelected: (Int) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("How many seats?")
                .font(HopFont.titleMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .padding(.top, HopSpacing.lg)
                .padding(.horizontal, HopSpacing.lg)

            Spacer().frame(height: HopSpacing.md)

            ForEach(1...4, id: \.self) { n in
                Button(action: { onSeatsSelected(n) }) {
                    HStack(spacing: HopSpacing.md) {
                        ZStack {
                            Circle()
                                .fill(n == currentSeats
                                    ? Color.hopPrimaryLime
                                    : Color.hopCardSurfaceMuted)
                                .frame(width: 32, height: 32)
                            Image(systemName: "person.fill")
                                .font(.system(size: 14))
                                .foregroundColor(n == currentSeats
                                    ? Color(hex: "#1A1A1A")
                                    : Color.hopAuthTextSecondary)
                        }
                        Text(n == 1 ? "1 seat" : "\(n) seats")
                            .font(HopFont.bodyLarge(weight: n == currentSeats ? .semibold : .regular))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Spacer()
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(n == currentSeats
                                ? Color.hopPrimaryLime.opacity(0.18)
                                : Color.clear)
                    )
                    .padding(.horizontal, HopSpacing.lg)
                }
                .buttonStyle(.plain)

                if n < 4 {
                    Divider()
                        .background(Color(hex: "#F0F0F0"))
                        .padding(.leading, HopSpacing.lg + 48)
                }
            }
            Spacer().frame(height: HopSpacing.xl)
        }
        .background(Color.white)
    }
}

// MARK: — DatePickerSheet ──────────────────────────────────────────────────

private struct DatePickerSheet: View {
    let currentDate: String
    let onDateSelected: (String) -> Void

    @State private var pickerDate: Date = Date()

    private static let monthNames = [
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.md) {
            Text("Pick a date")
                .font(HopFont.titleMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .padding(.top, HopSpacing.lg)

            // Quick chips
            HStack(spacing: HopSpacing.sm) {
                quickChip("Today")
                quickChip("Tomorrow")
            }

            DatePicker(
                "Date",
                selection: $pickerDate,
                in: Date()...,
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
            .tint(Color.hopPrimaryGreen)

            HStack {
                Spacer()
                Button("OK") {
                    let cal = Calendar.current
                    let comps = cal.dateComponents([.day, .month], from: pickerDate)
                    if let day = comps.day, let month = comps.month, month >= 1, month <= 12 {
                        onDateSelected("\(day) \(Self.monthNames[month - 1])")
                    }
                }
                .font(HopFont.labelLarge(weight: .semibold))
                .foregroundColor(Color.hopPrimaryGreen)
            }
        }
        .padding(.horizontal, HopSpacing.lg)
        .padding(.bottom, HopSpacing.lg)
        .background(Color.white)
    }

    @ViewBuilder
    private func quickChip(_ label: String) -> some View {
        Button(action: { onDateSelected(label) }) {
            Text(label)
                .font(HopFont.labelMedium(weight: .medium))
                .foregroundColor(currentDate == label
                    ? Color(hex: "#1A1A1A")
                    : Color.hopAuthTextSecondary)
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, 6)
                .background(currentDate == label
                    ? Color.hopPrimaryLime.opacity(0.25)
                    : Color.hopCardSurfaceMuted)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// MARK: — Color hex helper (local) ─────────────────────────────────────────

private extension Color {
    init(hex: String) {
        var clean = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if clean.hasPrefix("#") { clean.removeFirst() }
        var rgb: UInt64 = 0
        Scanner(string: clean).scanHexInt64(&rgb)
        let r = Double((rgb & 0xFF0000) >> 16) / 255.0
        let g = Double((rgb & 0x00FF00) >>  8) / 255.0
        let b = Double( rgb & 0x0000FF       ) / 255.0
        self = Color(red: r, green: g, blue: b)
    }
}
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


import SwiftUI
import Shared

// MARK: — PA-01 Passenger Home ─────────────────────────────────────────────────
//
// Unified home screen for the passenger role.
// Matches Android PassengerHomeScreen layout:
//   • White search card with shadow (from/to fields + swap button + date chips + seats)
//   • "Upcoming trips" section with TripCard list / loading / empty state
// The role toggle switches to DR-01 Driver Home (handled by parent).

struct PassengerHomeView: View {

    /// Called when the user taps "Find rides" — pushes PA-02 Search Results.
    var onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void

    /// Called when the role toggle switches to driver mode.
    var onSwitchToDriver: () -> Void

    var navigate: (HopRoute) -> Void

    // ── Local form state ──────────────────────────────────────────────────────
    @State private var origin       = ""
    @State private var destination  = ""
    @State private var selectedDate = "Today"
    @State private var seats        = 1
    @State private var selectedRole: HopRole = .passenger

    @StateObject private var tripWrapper = TripViewModelWrapper()

    private var canSearch: Bool {
        !origin.trimmingCharacters(in: .whitespaces).isEmpty &&
        !destination.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        ZStack(alignment: .top) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    // ── Top bar: Hop logo + Role toggle ───────────────────────
                    HStack {
                        Text("hop.")
                            .font(HopFont.headlineLarge())
                            .fontWeight(.bold)
                            .foregroundColor(Color.hopPrimaryLime)

                        Spacer()

                        RoleTogglePill(selectedRole: $selectedRole)
                            .onChange(of: selectedRole) { _, newRole in
                                if newRole == .driver { onSwitchToDriver() }
                            }
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.top, HopSpacing.md)
                    .padding(.bottom, HopSpacing.lg)

                    // ── Hero headline ─────────────────────────────────────────
                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Where are you\nheaded?")
                            .font(HopFont.displayLarge())
                            .foregroundColor(Color.hopTextPrimary)
                            .lineSpacing(4)

                        Text("Find affordable rides across Denmark")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopTextSecondary)
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.bottom, HopSpacing.xl)

                    // ── Search card ───────────────────────────────────────────
                    SearchCard(
                        origin: $origin,
                        destination: $destination,
                        selectedDate: $selectedDate,
                        seats: $seats,
                        canSearch: canSearch,
                        onSwap: {
                            let tmp = origin
                            origin = destination
                            destination = tmp
                        },
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
                        .font(HopFont.labelMedium())
                        .fontWeight(.semibold)
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
    }
}

// MARK: — SearchCard

private struct SearchCard: View {
    @Binding var origin: String
    @Binding var destination: String
    @Binding var selectedDate: String
    @Binding var seats: Int
    let canSearch: Bool
    let onSwap: () -> Void
    let onSearch: () -> Void

    var body: some View {
        let cardShape = RoundedRectangle(cornerRadius: 16)

        VStack(spacing: 0) {

            // ── From / To block with swap button ─────────────────────────────
            ZStack(alignment: .trailing) {
                VStack(spacing: 0) {
                    LocationFieldRow(
                        icon: "location.fill",
                        iconColor: Color.hopPrimaryGreen,
                        placeholder: "From — city or address",
                        text: $origin
                    )

                    Divider()
                        .background(Color(hex: 0xEEEEEE))
                        .padding(.leading, 40)

                    LocationFieldRow(
                        icon: "location.fill",
                        iconColor: Color.hopError,
                        placeholder: "To — city or address",
                        text: $destination
                    )
                }

                // Swap button — centred vertically on the divider
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

            Divider().background(Color(hex: 0xEEEEEE))

            // ── Date chips row ────────────────────────────────────────────────
            DateChipsRow(selectedDate: $selectedDate)

            Divider().background(Color(hex: 0xEEEEEE))

            // ── Seats row ─────────────────────────────────────────────────────
            SeatsRow(seats: $seats)

            Spacer().frame(height: HopSpacing.md)

            // ── Find rides CTA ────────────────────────────────────────────────
            HopPrimaryButton(title: "Find rides", isEnabled: canSearch, action: onSearch)
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.md)
        }
        .background(Color.white)
        .clipShape(cardShape)
        .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 4)
    }
}

// MARK: — LocationFieldRow

private struct LocationFieldRow: View {
    let icon: String
    let iconColor: Color
    let placeholder: String
    @Binding var text: String

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(iconColor)
                .frame(width: 24)

            TextField(placeholder, text: $text)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color(hex: 0x1A1A1A))
                .tint(Color.hopPrimaryGreen)
                .submitLabel(.next)
                .autocorrectionDisabled()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.trailing, 44) // leave room for the swap button
        .padding(.vertical, HopSpacing.sm + 2)
    }
}

// MARK: — DateChipsRow

private struct DateChipsRow: View {
    @Binding var selectedDate: String

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            HStack(spacing: 4) {
                Image(systemName: "calendar")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(hex: 0x888888))
                Text("Date")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color(hex: 0x888888))
            }

            Spacer()

            HStack(spacing: HopSpacing.xs) {
                DateChip(label: "Today",    isSelected: selectedDate == "Today")    { selectedDate = "Today" }
                DateChip(label: "Tomorrow", isSelected: selectedDate == "Tomorrow") { selectedDate = "Tomorrow" }

                // Custom date trigger (post-MVP)
                Button {
                    // Platform date picker — post-MVP
                } label: {
                    HStack(spacing: 2) {
                        Text(selectedDate != "Today" && selectedDate != "Tomorrow" ? selectedDate : "Pick")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color(hex: 0x444444))
                        Image(systemName: "chevron.down")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(Color(hex: 0x888888))
                    }
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, 4)
                    .overlay(Capsule().stroke(Color(hex: 0xDDDDDD), lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.sm)
    }
}

private struct DateChip: View {
    let label: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(HopFont.labelSmall())
                .fontWeight(isSelected ? .semibold : .regular)
                .foregroundColor(isSelected ? Color(hex: 0x1A1A1A) : Color(hex: 0x444444))
                .padding(.horizontal, HopSpacing.sm)
                .padding(.vertical, 4)
                .background(isSelected ? Color.hopPrimaryLime : Color.clear)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(isSelected ? Color.hopPrimaryLime : Color(hex: 0xDDDDDD), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: — SeatsRow

private struct SeatsRow: View {
    @Binding var seats: Int
    private let range = 1...4

    var body: some View {
        HStack {
            Text("Seats")
                .font(HopFont.bodyMedium())
                .fontWeight(.medium)
                .foregroundColor(Color(hex: 0x444444))

            Spacer()

            HStack(spacing: HopSpacing.sm) {
                // Decrease button
                Button {
                    if seats > range.lowerBound { seats -= 1 }
                } label: {
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 32, height: 32)
                            .overlay(
                                Circle().stroke(
                                    seats > range.lowerBound ? Color(hex: 0xCCCCCC) : Color(hex: 0xEEEEEE),
                                    lineWidth: 1
                                )
                            )
                        Image(systemName: "minus")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(seats > range.lowerBound ? Color(hex: 0x1A1A1A) : Color(hex: 0xCCCCCC))
                    }
                }
                .buttonStyle(.plain)
                .disabled(seats <= range.lowerBound)

                Text("\(seats)")
                    .font(HopFont.bodyLarge())
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: 0x1A1A1A))
                    .frame(width: 20, alignment: .center)

                // Increase button
                Button {
                    if seats < range.upperBound { seats += 1 }
                } label: {
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 32, height: 32)
                            .overlay(
                                Circle().stroke(
                                    seats < range.upperBound ? Color(hex: 0xCCCCCC) : Color(hex: 0xEEEEEE),
                                    lineWidth: 1
                                )
                            )
                        Image(systemName: "plus")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(seats < range.upperBound ? Color(hex: 0x1A1A1A) : Color(hex: 0xCCCCCC))
                    }
                }
                .buttonStyle(.plain)
                .disabled(seats >= range.upperBound)
            }
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
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
    .background(Color.hopSurface)
}

#Preview("PA-01 Passenger Home — Filled") {
    PassengerHomeView(
        onSearch:         { _, _, _, _ in },
        onSwitchToDriver: {},
        navigate:         { _ in }
    )
    .background(Color.hopSurface)
}

import SwiftUI
import Shared

// MARK: — PA-01 Passenger Home ─────────────────────────────────────────────────
//
// Unified home screen for the passenger role.
// Contains: role toggle pill, search form (origin, destination, date, seats).
// The role toggle switches to DR-01 Driver Home (handled by parent).

struct PassengerHomeView: View {

    /// Called when the user taps Search — pushes PA-02 Search Results.
    var onSearch: (_ origin: String, _ dest: String, _ date: String, _ seats: Int) -> Void

    /// Called when the role toggle switches to driver mode.
    var onSwitchToDriver: () -> Void

    var navigate: (HopRoute) -> Void

    // ── Local form state ──────────────────────────────────────────────────────
    @State private var origin      = ""
    @State private var destination = ""
    @State private var date        = Date()
    @State private var seats       = 1
    @State private var selectedRole: HopRole = .passenger

    private var canSearch: Bool {
        !origin.trimmingCharacters(in: .whitespaces).isEmpty &&
        !destination.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private static let isoFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        return f
    }()

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
                    VStack(spacing: HopSpacing.sm) {

                        // Origin
                        SearchFieldRow(
                            icon: "circle",
                            iconColor: Color.hopPrimaryGreen,
                            label: "From",
                            placeholder: "e.g. Aarhus C",
                            text: $origin
                        )

                        searchDivider

                        // Destination
                        SearchFieldRow(
                            icon: "mappin",
                            iconColor: Color.hopPrimaryLime,
                            label: "To",
                            placeholder: "e.g. København H",
                            text: $destination
                        )

                        searchDivider

                        // Date
                        HStack(spacing: HopSpacing.sm) {
                            Image(systemName: "calendar")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color.hopTextSecondary)
                                .frame(width: 20)

                            Text("Date")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopTextSecondary)
                                .frame(width: 48, alignment: .leading)

                            DatePicker(
                                "",
                                selection: $date,
                                in: Date()...,
                                displayedComponents: .date
                            )
                            .labelsHidden()
                            .colorScheme(.dark)
                            .tint(Color.hopPrimaryLime)
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)

                        searchDivider

                        // Seats
                        HStack(spacing: HopSpacing.sm) {
                            Image(systemName: "person.2")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color.hopTextSecondary)
                                .frame(width: 20)

                            Text("Seats")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopTextSecondary)
                                .frame(width: 48, alignment: .leading)

                            Spacer()

                            SeatsStepper(seats: $seats)
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)

                        // Search CTA
                        HopPrimaryButton(title: "Find Rides", isEnabled: canSearch) {
                            onSearch(
                                origin.trimmingCharacters(in: .whitespaces),
                                destination.trimmingCharacters(in: .whitespaces),
                                Self.isoFormatter.string(from: date),
                                seats
                            )
                        }
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.bottom, HopSpacing.md)
                    }
                    .background(Color.hopSurfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .padding(.horizontal, HopSpacing.md)

                    // ── Popular routes label ───────────────────────────────────
                    Text("Popular routes")
                        .font(HopFont.labelMedium())
                        .fontWeight(.semibold)
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.top, HopSpacing.xl)
                        .padding(.bottom, HopSpacing.sm)

                    // ── Popular route chips ───────────────────────────────────
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: HopSpacing.sm) {
                            ForEach(PopularRoute.all) { route in
                                PopularRouteChip(route: route) {
                                    origin      = route.from
                                    destination = route.to
                                }
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                    }

                    Spacer().frame(height: HopSpacing.xxl)
                }
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: — Helpers

    private var searchDivider: some View {
        Divider()
            .background(Color.hopSurface.opacity(0.6))
            .padding(.horizontal, HopSpacing.md)
    }
}

// MARK: — SearchFieldRow

private struct SearchFieldRow: View {
    let icon: String
    let iconColor: Color
    let label: String
    let placeholder: String
    @Binding var text: String

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(iconColor)
                .frame(width: 20)

            Text(label)
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
                .frame(width: 48, alignment: .leading)

            TextField(placeholder, text: $text)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextPrimary)
                .tint(Color.hopPrimaryLime)
                .submitLabel(.next)
                .autocorrectionDisabled()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.sm + 2)
    }
}

// MARK: — SeatsStepper

private struct SeatsStepper: View {
    @Binding var seats: Int
    private let range = 1...4

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Button {
                if seats > range.lowerBound { seats -= 1 }
            } label: {
                Image(systemName: "minus.circle")
                    .font(.system(size: 22, weight: .light))
                    .foregroundColor(seats > range.lowerBound ? Color.hopPrimaryLime : Color.hopTextSecondary)
            }
            .buttonStyle(.plain)
            .disabled(seats <= range.lowerBound)

            Text("\(seats)")
                .font(HopFont.bodyLarge())
                .fontWeight(.semibold)
                .foregroundColor(Color.hopTextPrimary)
                .frame(minWidth: 24, alignment: .center)

            Button {
                if seats < range.upperBound { seats += 1 }
            } label: {
                Image(systemName: "plus.circle")
                    .font(.system(size: 22, weight: .light))
                    .foregroundColor(seats < range.upperBound ? Color.hopPrimaryLime : Color.hopTextSecondary)
            }
            .buttonStyle(.plain)
            .disabled(seats >= range.upperBound)
        }
    }
}

// MARK: — Popular routes data

private struct PopularRoute: Identifiable {
    let id = UUID()
    let from: String
    let to: String

    var label: String { "\(from) → \(to)" }

    static let all: [PopularRoute] = [
        PopularRoute(from: "Aarhus C", to: "København H"),
        PopularRoute(from: "Odense C", to: "København H"),
        PopularRoute(from: "Aalborg", to: "Aarhus C"),
        PopularRoute(from: "Esbjerg", to: "Aarhus C"),
        PopularRoute(from: "Randers", to: "Aarhus C"),
    ]
}

private struct PopularRouteChip: View {
    let route: PopularRoute
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: "arrow.right")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(Color.hopPrimaryLime)
                Text(route.label)
                    .font(HopFont.labelSmall())
                    .foregroundColor(Color.hopTextPrimary)
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)
            .background(Color.hopSurfaceElevated)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(Color.hopSurface.opacity(0.5), lineWidth: 0.5))
        }
        .buttonStyle(.plain)
    }
}

// MARK: — Previews

#Preview("PA-01 Passenger Home — Default") {
    PassengerHomeView(
        onSearch:        { _, _, _, _ in },
        onSwitchToDriver: {},
        navigate:        { _ in }
    )
    .background(Color.hopSurface)
}

#Preview("PA-01 Passenger Home — Filled") {
    PassengerHomeView(
        onSearch:        { _, _, _, _ in },
        onSwitchToDriver: {},
        navigate:        { _ in }
    )
    .background(Color.hopSurface)
}

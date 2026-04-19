import SwiftUI
import Shared

// MARK: — HopDateFormatter

enum HopDateFormatter {
    private static let iso: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        f.timeZone = TimeZone(identifier: "UTC")
        return f
    }()
    private static let isoDate: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        return f
    }()

    /// Short display: "Mon 07:30" or "12 May, 08:00" depending on proximity.
    static func shortDisplay(iso isoString: String) -> String {
        if let d = iso.date(from: isoString) {
            let f = DateFormatter()
            f.dateFormat = "d MMM, HH:mm"
            return f.string(from: d)
        }
        return isoString
    }

    /// Just the time, e.g. "07:30"
    static func timeOnly(iso isoString: String) -> String {
        if let d = iso.date(from: isoString) {
            let f = DateFormatter()
            f.dateFormat = "HH:mm"
            return f.string(from: d)
        }
        return ""
    }

    /// Date only, e.g. "Mon 12 May"
    static func dayDate(iso isoString: String) -> String {
        if let d = iso.date(from: isoString) {
            let f = DateFormatter()
            f.dateFormat = "EEE d MMM"
            return f.string(from: d)
        }
        return isoString
    }
}

// MARK: — PA-02 Search Results ─────────────────────────────────────────────────
//
// Displays TripCard list for a search query.
// Empty state: illustration + "No rides found" + "Try different dates" CTA.
// Loading state: shimmer skeleton rows.
// Broken trips rendered at 60% opacity and non-tappable.

struct SearchResultsView: View {

    let origin:      String
    let destination: String
    let date:        String
    let seats:       Int

    var onTripTapped: (String) -> Void
    var onBack:       () -> Void

    @StateObject private var wrapper = SearchTripsViewModelWrapper()

    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: 0) {

                // ── Search summary header ─────────────────────────────────────
                searchSummaryHeader

                // ── Content ───────────────────────────────────────────────────
                ZStack {
                    if wrapper.state.isLoading {
                        skeletonList
                    } else if wrapper.state.trips.isEmpty {
                        emptyState
                    } else {
                        resultsList
                    }
                }
                .animation(.easeInOut(duration: 0.25), value: wrapper.state.isLoading)
            }

            // ── Toast ─────────────────────────────────────────────────────────
            if let msg = toastMessage {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { toastMessage = nil }
                        }
                    }
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopTextPrimary)
                }
                .accessibilityLabel("Back")
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving()
            wrapper.search(origin: origin, dest: destination, date: date, seats: seats)
        }
        .onChange(of: wrapper.state.error) { error in
            if let error {
                withAnimation { toastMessage = error }
                wrapper.clearError()
            }
        }
    }

    // MARK: — Sub-views

    private var searchSummaryHeader: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            HStack(spacing: HopSpacing.xs) {
                Text(origin)
                    .font(HopFont.headlineSmall())
                    .fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
                Image(systemName: "arrow.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color.hopPrimaryLime)
                Text(destination)
                    .font(HopFont.headlineSmall())
                    .fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
            }
            HStack(spacing: HopSpacing.md) {
                Label(formattedDate, systemImage: "calendar")
                Label("\(seats) seat\(seats == 1 ? "" : "s")", systemImage: "person.2")
            }
            .font(HopFont.bodySmall())
            .foregroundColor(Color.hopTextSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.md)
        .background(Color.hopSurfaceElevated)
    }

    private var resultsList: some View {
        ScrollView(showsIndicators: false) {
            LazyVStack(spacing: HopSpacing.sm) {
                ForEach(wrapper.state.trips, id: \.id) { tripUi in
                    TripCard(data: tripUi.toCardData()) {
                        guard !tripUi.isBroken else { return }
                        onTripTapped(tripUi.id)
                    }
                    .opacity(tripUi.isBroken ? 0.6 : 1.0)
                    .disabled(tripUi.isBroken)
                    .accessibilityHint(tripUi.isBroken ? "This trip is unavailable" : "Double-tap to view details")
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.md)
            .padding(.bottom, HopSpacing.xxl)
        }
    }

    private var emptyState: some View {
        EmptyState(
            systemImage: "car.2",
            headline: "No rides found",
            subtitle: "Try different dates or a nearby city.",
            ctaLabel: "Edit search",
            ctaAction: onBack
        )
    }

    private var skeletonList: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: HopSpacing.sm) {
                ForEach(0..<5, id: \.self) { _ in
                    TripCardSkeleton()
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.md)
        }
    }

    // MARK: — Helpers

    private var formattedDate: String {
        let inputFmt = DateFormatter()
        inputFmt.dateFormat = "yyyy-MM-dd"
        let displayFmt = DateFormatter()
        displayFmt.dateFormat = "d MMM"
        if let d = inputFmt.date(from: date) {
            return displayFmt.string(from: d)
        }
        return date
    }
}

// MARK: — TripCardSkeleton

private struct TripCardSkeleton: View {
    @State private var shimmer = false

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack(spacing: HopSpacing.sm) {
                Circle().frame(width: 40, height: 40)
                VStack(alignment: .leading, spacing: 4) {
                    RoundedRectangle(cornerRadius: 4).frame(width: 120, height: 14)
                    RoundedRectangle(cornerRadius: 4).frame(width: 80, height: 12)
                }
                Spacer()
                RoundedRectangle(cornerRadius: 8).frame(width: 60, height: 24)
            }
            Divider()
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    RoundedRectangle(cornerRadius: 4).frame(width: 100, height: 12)
                    RoundedRectangle(cornerRadius: 4).frame(width: 90, height: 12)
                }
                Spacer()
                RoundedRectangle(cornerRadius: 4).frame(width: 70, height: 12)
            }
        }
        .padding(HopSpacing.md)
        .foregroundColor(Color.hopSurfaceElevated)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shimmer(active: shimmer)
        .onAppear { shimmer = true }
    }
}

// MARK: — Shimmer modifier

private extension View {
    func shimmer(active: Bool) -> some View {
        self.overlay(
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [.clear, Color.white.opacity(0.06), .clear],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .rotationEffect(.degrees(15))
                .offset(x: active ? 300 : -300)
                .animation(.linear(duration: 1.2).repeatForever(autoreverses: false), value: active)
                .clipped()
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: — TripUiModel → TripCardData mapper

private extension TripUiModel {
    func toCardData() -> TripCardData {
        TripCardData(
            driverName:       trip.driverId,   // real name loaded when trip detail fetched
            driverImageURL:   nil,
            driverRating:     4.5,             // placeholder until user profile available
            reviewCount:      0,
            originName:       trip.originName,
            destName:         trip.destName,
            departsAt:        HopDateFormatter.shortDisplay(iso: trip.departsAt),
            badgeStatus:      trip.model == .b ? .modelB : .modelA,
            priceOerePerSeat: trip.priceOerePerSeat
        )
    }
}

// MARK: — Previews

#Preview("PA-02 Search Results — Loading") {
    NavigationStack {
        SearchResultsView(
            origin:      "Aarhus C",
            destination: "København H",
            date:        "2026-05-10",
            seats:       1,
            onTripTapped: { _ in },
            onBack:       {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-02 Search Results — Empty") {
    NavigationStack {
        SearchResultsView(
            origin:      "Silkeborg",
            destination: "Viborg",
            date:        "2026-05-10",
            seats:       2,
            onTripTapped: { _ in },
            onBack:       {}
        )
    }
    .preferredColorScheme(.dark)
}

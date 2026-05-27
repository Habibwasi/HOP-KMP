import SwiftUI
import MapKit
import Combine
import CoreLocation
import Shared

/// PA-01 — Full-screen location picker overlay.
///
/// Mirrors composeApp `LocationPickerOverlay` (PassengerHomeScreen.kt):
/// a full-screen MapKit map with a centered pin, a floating search bar with
/// autocomplete predictions, quick-pick saved-place chips and recent searches
/// when the field is empty, and a "Confirm pin" button at the bottom that
/// reverse-geocodes the map crosshair if the field is empty.
struct LocationPickerOverlay: View {
    let title: String                // "Where from?" or "Where to?"
    let initialText: String
    let savedPlaces: [SavedPlace]
    let recentSearches: [RecentSearch]
    let onDismiss: () -> Void
    let onConfirm: (_ address: String) -> Void
    let onRouteConfirm: (_ origin: String, _ dest: String) -> Void
    let onRequestAddPlace: () -> Void
    let onDeleteRecentSearch: (_ id: String) -> Void

    // ── Map state ────────────────────────────────────────────────────────
    // Default to Copenhagen — same as Android (LatLng(55.6761, 12.5683), z 13).
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 55.6761, longitude: 12.5683),
            span: MKCoordinateSpan(latitudeDelta: 0.08, longitudeDelta: 0.08)
        )
    )
    @State private var pinnedCoordinate: CLLocationCoordinate2D = CLLocationCoordinate2D(
        latitude: 55.6761, longitude: 12.5683
    )
    @State private var pinnedAddress: String = ""

    // ── Search / autocomplete state ──────────────────────────────────────
    @State private var query: String = ""
    @StateObject private var completer = LocalSearchCompleter()

    // ── Geocoding ────────────────────────────────────────────────────────
    private let geocoder = CLGeocoder()

    var body: some View {
        ZStack(alignment: .top) {
            // ── Map layer ────────────────────────────────────────────────
            Map(position: $cameraPosition, interactionModes: .all)
                .mapStyle(.standard)
                .ignoresSafeArea()
                .onMapCameraChange(frequency: .onEnd) { ctx in
                    pinnedCoordinate = ctx.camera.centerCoordinate
                    reverseGeocode(pinnedCoordinate)
                }

            // ── Centered pin (tip points at map centre) ─────────────────
            Image(systemName: "mappin.and.ellipse")
                .font(.system(size: 32, weight: .semibold))
                .foregroundColor(Color.hopPrimaryGreen)
                .shadow(color: .black.opacity(0.25), radius: 3, y: 2)
                .offset(y: -16)
                .allowsHitTesting(false)

            // ── Top floating search column ──────────────────────────────
            VStack(spacing: HopSpacing.xs) {
                searchBar

                if query.isEmpty {
                    quickChipsRow
                    if !recentSearches.isEmpty {
                        recentSearchesCard
                    }
                } else if !completer.results.isEmpty {
                    suggestionsCard
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.sm)

            // ── Bottom confirm button ───────────────────────────────────
            VStack {
                Spacer()
                HopButton(
                    text: pinnedAddress.isEmpty ? "Confirm pin location"
                                                : "Use: \(pinnedAddress)",
                    variant: .primary
                ) {
                    let q = query.trimmingCharacters(in: .whitespaces)
                    if !q.isEmpty {
                        onConfirm(q)
                    } else if !pinnedAddress.isEmpty {
                        onConfirm(pinnedAddress)
                    }
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.md)
            }
        }
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            query = initialText
            reverseGeocode(pinnedCoordinate)
        }
        .onChange(of: query) { _, newValue in
            completer.update(query: newValue)
        }
    }

    // MARK: — Search bar ────────────────────────────────────────────────

    private var searchBar: some View {
        HStack(spacing: HopSpacing.sm) {
            Button(action: onDismiss) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 44, height: 44)
            }.buttonStyle(.plain)

            TextField("", text: $query, prompt:
                Text(title).foregroundColor(Color(white: 0.69))
            )
            .font(HopFont.bodyMedium())
            .foregroundColor(Color.hopAuthTextPrimary)
            .submitLabel(.search)
            .onSubmit {
                let q = query.trimmingCharacters(in: .whitespaces)
                if !q.isEmpty { onConfirm(q) }
            }

            if !query.isEmpty {
                Button(action: { query = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color.hopAuthTextSecondary)
                }.buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.18), radius: 6, y: 2)
    }

    // MARK: — Quick chips (current + saved + add) ───────────────────────

    private var quickChipsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: HopSpacing.xs) {
                // Current location chip — uses the reverse-geocoded crosshair
                Button(action: {
                    if !pinnedAddress.isEmpty { onConfirm(pinnedAddress) }
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 11))
                        Text("Current")
                            .font(HopFont.labelSmall(weight: .medium))
                    }
                    .foregroundColor(Color.hopPrimaryGreen)
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, 5)
                    .background(Color.hopPrimaryLime.opacity(0.18))
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.hopPrimaryLime, lineWidth: 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                }.buttonStyle(.plain)

                ForEach(savedPlaces.prefix(3), id: \.id) { place in
                    Button(action: { onConfirm(place.address) }) {
                        HStack(spacing: 4) {
                            Image(systemName: iconName(for: place.kind))
                                .font(.system(size: 11))
                                .foregroundColor(Color.hopPrimaryGreen)
                            Text(place.label)
                                .font(HopFont.labelSmall(weight: .medium))
                                .foregroundColor(Color.hopAuthTextPrimary)
                        }
                        .padding(.horizontal, HopSpacing.sm)
                        .padding(.vertical, 5)
                        .background(Color.hopCardSurfaceMuted)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.hopCardBorder, lineWidth: 1)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                    }.buttonStyle(.plain)
                }

                Button(action: onRequestAddPlace) {
                    HStack(spacing: 4) {
                        Image(systemName: "plus").font(.system(size: 11))
                        Text("Add").font(HopFont.labelSmall(weight: .medium))
                    }
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, 5)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.hopAuthInputBorder, lineWidth: 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                }.buttonStyle(.plain)
            }
            .padding(.horizontal, 4)
        }
        .padding(.vertical, 6)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.18), radius: 6, y: 2)
    }

    // MARK: — Recent searches card ──────────────────────────────────────

    private var recentSearchesCard: some View {
        VStack(spacing: 0) {
            ForEach(Array(recentSearches.prefix(5).enumerated()), id: \.element.id) { index, rs in
                HStack(spacing: HopSpacing.sm) {
                    Button(action: { onRouteConfirm(rs.originLabel, rs.destLabel) }) {
                        HStack(spacing: HopSpacing.sm) {
                            Image(systemName: "clock.arrow.circlepath")
                                .font(.system(size: 14))
                                .foregroundColor(Color.hopAuthTextSecondary)
                            Text("\(rs.originLabel) → \(rs.destLabel)")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .lineLimit(1)
                            Spacer()
                        }
                        .contentShape(Rectangle())
                    }.buttonStyle(.plain)

                    Button(action: { onDeleteRecentSearch(rs.id) }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .frame(width: 24, height: 24)
                    }.buttonStyle(.plain)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, 10)

                if index < min(recentSearches.count, 5) - 1 {
                    Divider().padding(.leading, 40)
                }
            }
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.18), radius: 6, y: 2)
    }

    // MARK: — Autocomplete suggestions card ─────────────────────────────

    private var suggestionsCard: some View {
        VStack(spacing: 0) {
            ForEach(Array(completer.results.prefix(5).enumerated()), id: \.offset) { index, result in
                Button(action: {
                    let label = [result.title, result.subtitle]
                        .filter { !$0.isEmpty }
                        .joined(separator: ", ")
                    onConfirm(label)
                }) {
                    HStack(spacing: HopSpacing.sm) {
                        Image(systemName: "mappin")
                            .font(.system(size: 14))
                            .foregroundColor(Color(white: 0.69))
                        VStack(alignment: .leading, spacing: 2) {
                            Text(result.title)
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .lineLimit(1)
                            if !result.subtitle.isEmpty {
                                Text(result.subtitle)
                                    .font(HopFont.labelSmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                    .lineLimit(1)
                            }
                        }
                        Spacer()
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, 10)
                    .contentShape(Rectangle())
                }.buttonStyle(.plain)

                if index < min(completer.results.count, 5) - 1 {
                    Divider()
                }
            }
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.18), radius: 6, y: 2)
    }

    // MARK: — Helpers ───────────────────────────────────────────────────

    private func iconName(for kind: SavedPlaceKind) -> String {
        switch kind {
        case .home: return "house"
        case .work: return "briefcase"
        default:    return "mappin"
        }
    }

    private func reverseGeocode(_ coord: CLLocationCoordinate2D) {
        let location = CLLocation(latitude: coord.latitude, longitude: coord.longitude)
        // Cancel any in-flight request to avoid throttling.
        geocoder.cancelGeocode()
        geocoder.reverseGeocodeLocation(location) { placemarks, _ in
            guard let p = placemarks?.first else { return }
            let parts = [p.thoroughfare, p.locality, p.country].compactMap { $0 }
            let address = parts.joined(separator: ", ")
            if !address.isEmpty {
                DispatchQueue.main.async { pinnedAddress = address }
            }
        }
    }
}

// MARK: — MKLocalSearchCompleter wrapper ────────────────────────────────

/// Debounced wrapper around `MKLocalSearchCompleter` that publishes the
/// current `results` array for SwiftUI consumption. Mirrors the 350 ms
/// debounce and 2-character minimum used on Android (`PassengerHomeScreen`).
private final class LocalSearchCompleter: NSObject, ObservableObject,
                                          MKLocalSearchCompleterDelegate {
    @Published var results: [MKLocalSearchCompletion] = []

    private let completer = MKLocalSearchCompleter()
    private var debounce: DispatchWorkItem?

    override init() {
        super.init()
        completer.delegate = self
        completer.resultTypes = [.address, .pointOfInterest]
    }

    func update(query: String) {
        debounce?.cancel()
        let trimmed = query.trimmingCharacters(in: .whitespaces)
        guard trimmed.count >= 2 else {
            results = []
            return
        }
        let work = DispatchWorkItem { [weak self] in
            self?.completer.queryFragment = trimmed
        }
        debounce = work
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35, execute: work)
    }

    func completerDidUpdateResults(_ completer: MKLocalSearchCompleter) {
        results = completer.results
    }

    func completer(_ completer: MKLocalSearchCompleter, didFailWithError error: Error) {
        results = []
    }
}

import SwiftUI
import Shared

/// PA-01 — Full-screen location picker overlay (light theme).
/// Mirrors composeApp `LocationPickerOverlay`. Combines a search field,
/// quick-pick saved-place chips, and recent searches. Returns either a
/// single picked location (`onConfirm`) or an origin/dest pair when the
/// user taps a recent search row (`onRouteConfirm`).
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

    @State private var query: String = ""

    var body: some View {
        VStack(spacing: 0) {
            // ── Top bar ──────────────────────────────────────────────────
            HStack(spacing: HopSpacing.sm) {
                Button(action: onDismiss) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 36, height: 36)
                }
                .buttonStyle(.plain)
                Text(title)
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            // ── Search field ─────────────────────────────────────────────
            HStack(spacing: HopSpacing.sm) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopAuthTextSecondary)
                TextField("", text: $query, prompt:
                    Text("Search a place").foregroundColor(Color(white: 0.69))
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
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, 12)
            .background(Color.hopCardSurfaceMuted)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.hopCardBorder, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.xs)

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    // Saved places + Add chip
                    LocationSectionHeader(text: "Saved places")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: HopSpacing.sm) {
                            ForEach(savedPlaces, id: \.id) { place in
                                SavedPlaceChip(
                                    label: place.label,
                                    kind: place.kind
                                ) { onConfirm(place.address) }
                            }
                            AddPlaceChip(onTap: onRequestAddPlace)
                        }
                        .padding(.horizontal, HopSpacing.md)
                    }

                    if !recentSearches.isEmpty {
                        LocationSectionHeader(text: "Recent")
                        VStack(spacing: 0) {
                            ForEach(recentSearches, id: \.id) { rs in
                                RecentRow(
                                    origin: rs.originLabel,
                                    dest: rs.destLabel,
                                    onTap: { onRouteConfirm(rs.originLabel, rs.destLabel) },
                                    onDelete: { onDeleteRecentSearch(rs.id) }
                                )
                            }
                        }
                        .padding(.horizontal, HopSpacing.md)
                    }

                    Spacer(minLength: HopSpacing.xl)
                }
                .padding(.top, HopSpacing.md)
            }
        }
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear { query = initialText }
    }
}

private struct LocationSectionHeader: View {
    let text: String
    var body: some View {
        Text(text)
            .font(HopFont.labelMedium(weight: .semibold))
            .foregroundColor(Color.hopAuthTextSecondary)
            .padding(.horizontal, HopSpacing.md)
    }
}

private struct SavedPlaceChip: View {
    let label: String
    let kind: SavedPlaceKind
    let onTap: () -> Void

    private var iconName: String {
        switch kind {
        case .home: return "house"
        case .work: return "briefcase"
        default:    return "mappin"
        }
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: iconName).font(.system(size: 12))
                Text(label).font(HopFont.labelMedium())
            }
            .foregroundColor(Color.hopAuthTextPrimary)
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 8)
            .background(Color.hopCardSurfaceMuted)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.hopCardBorder, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
    }
}

private struct AddPlaceChip: View {
    let onTap: () -> Void
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: "plus").font(.system(size: 12, weight: .semibold))
                Text("Add").font(HopFont.labelMedium(weight: .semibold))
            }
            .foregroundColor(Color.hopAuthTextPrimary)
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 8)
            .background(Color.hopPrimaryLime)
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
    }
}

private struct RecentRow: View {
    let origin: String
    let dest: String
    let onTap: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Button(action: onTap) {
                HStack(spacing: HopSpacing.sm) {
                    Image(systemName: "clock.arrow.circlepath")
                        .font(.system(size: 14))
                        .foregroundColor(Color.hopAuthTextSecondary)
                    Text("\(origin) → \(dest)")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .lineLimit(1)
                    Spacer()
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            Button(action: onDelete) {
                Image(systemName: "xmark")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .frame(width: 28, height: 28)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 12)
    }
}

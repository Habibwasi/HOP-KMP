import SwiftUI
import Shared

// MARK: — DR-01 Driver Home (rendered inside unified HomeView) ────────────────
//
// Displays driver dashboard: licence status, monthly earnings hero card,
// list of upcoming trips, FAB to post a new trip.

struct DriverHomeView: View {

    var navigate: (HopRoute) -> Void

    @StateObject private var wrapper = DriverViewModelWrapper()
    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    // ── Licence status banner ────────────────────────────────
                    if let status = wrapper.state.licenceStatus, status != LicenceStatus.approved {
                        LicenceStatusBanner(status: status, onTap: { navigate(.enableDriverStep1) })
                    }

                    // ── Earnings hero ────────────────────────────────────────
                    Button(action: wrapper.tapEarningsBanner) {
                        EarningsHero(
                            monthlyOere: Int(wrapper.state.monthlyEarningsOere),
                            estimatedTaxOere: Int(wrapper.state.estimatedTaxOere)
                        )
                    }
                    .buttonStyle(.plain)

                    // ── Upcoming trips ───────────────────────────────────────
                    Text("Your trips")
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                        .padding(.top, HopSpacing.md)

                    if wrapper.state.isLoading && wrapper.state.trips.isEmpty {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, HopSpacing.xl)
                    } else if wrapper.state.trips.isEmpty {
                        EmptyState(
                            systemImage: "car.2",
                            headline: "No active trips",
                            subtitle: "Post your first trip to start earning.",
                            ctaLabel: "Post a trip",
                            ctaAction: { navigate(.postTripModelSelect) }
                        )
                    } else {
                        VStack(spacing: HopSpacing.sm) {
                            ForEach(wrapper.state.trips, id: \.id) { trip in
                                Button {
                                    navigate(.tripDetailActiveDriver(tripId: trip.id))
                                } label: {
                                    DriverTripRow(trip: trip)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    Spacer().frame(height: HopSpacing.xxl + 56)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.md)
            }

            // ── FAB ─────────────────────────────────────────────────────────
            Button(action: { navigate(.postTripModelSelect) }) {
                HStack(spacing: HopSpacing.xs) {
                    Image(systemName: "plus")
                    Text("Post a trip")
                }
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopSurface)
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.sm)
                .background(Color.hopPrimaryLime)
                .clipShape(Capsule())
                .shadow(color: .black.opacity(0.25), radius: 8, y: 4)
            }
            .padding(.trailing, HopSpacing.md)
            .padding(.bottom, HopSpacing.xl)

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xxl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is DriverEffectNavigateToPostTrip:
                    navigate(.postTripModelSelect)
                case is DriverEffectNavigateToTaxDashboard:
                    navigate(.taxDashboard)
                case is DriverEffectNavigateToLicenceUpload:
                    navigate(.enableDriverStep2)
                case let snack as DriverEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                default: break
                }
            }
            wrapper.loadDriverHome()
            wrapper.loadLicenceStatus()
        }
    }
}

// MARK: — Sub-components

private struct LicenceStatusBanner: View {
    let status: LicenceStatus
    let onTap: () -> Void

    var body: some View {
        let (text, color, action): (String, Color, String?) = {
            switch status {
            case LicenceStatus.none:     return ("Add your licence to start driving", Color.hopWarning, "Add now")
            case LicenceStatus.pending:  return ("Licence under review", Color.hopWarning, nil)
            case LicenceStatus.rejected: return ("Licence rejected — please re-upload", Color.hopError, "Re-upload")
            default: return ("", .clear, nil)
            }
        }()

        return Button(action: onTap) {
            HStack {
                Image(systemName: "exclamationmark.triangle.fill").foregroundColor(color)
                Text(text)
                    .font(HopFont.bodyMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                if let action {
                    Text(action)
                        .font(HopFont.bodySmall(weight: .semibold))
                        .foregroundColor(color)
                }
            }
            .padding(HopSpacing.md)
            .background(color.opacity(0.15))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(status == LicenceStatus.pending)
    }
}

private struct EarningsHero: View {
    let monthlyOere: Int
    let estimatedTaxOere: Int

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("This month")
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopSurface.opacity(0.7))
            Text("DKK \(monthlyOere / 100)")
                .font(HopFont.displayLarge())
                .foregroundColor(Color.hopSurface)
            HStack {
                Text("Estimated tax")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopSurface.opacity(0.7))
                Spacer()
                Text("DKK \(estimatedTaxOere / 100)")
                    .font(HopFont.bodySmall(weight: .semibold))
                    .foregroundColor(Color.hopSurface)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopPrimaryLime)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct DriverTripRow: View {
    let trip: TripUiModel

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            VStack(alignment: .leading, spacing: 2) {
                Text("\(trip.trip.originName) → \(trip.trip.destName)")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                    .lineLimit(1)
                Text(trip.trip.departsAt)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
            Spacer()
            Text("\(trip.trip.seatsBooked)/\(trip.trip.seatsTotal) seats")
                .font(HopFont.bodySmall(weight: .semibold))
                .foregroundColor(Color.hopPrimaryLime)
        }
        .padding(HopSpacing.md)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

import SwiftUI
import Shared

/// PA-03 — Trip Detail. Mirrors composeApp `TripDetailScreen.kt`.
struct TripDetailView: View {
    let tripId: String
    let onBack: () -> Void
    let onNavigateToOtherProfile: (_ driverId: String) -> Void
    let onNavigateToBookingConfirmation: (_ tripId: String) -> Void

    @StateObject private var wrapper = TripDetailViewModelWrapper()

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 36, height: 36)
                }.buttonStyle(.plain)
                Text("Trip Details")
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            if wrapper.state.isLoading {
                Spacer()
                ProgressView().tint(Color.hopPrimaryGreen)
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 0) {
                        DriverHeader(
                            driverName: wrapper.state.driverName,
                            driverInitials: wrapper.state.driverInitials,
                            driverAvatarURL: wrapper.state.driverAvatarUrl.flatMap { URL(string: $0) },
                            driverRating: Double(wrapper.state.driverRating),
                            isVerified: wrapper.state.isDriverVerified,
                            model: wrapper.state.model,
                            onAvatarTap: { wrapper.viewDriverProfile() }
                        )
                        SectionDivider()
                        RouteSection(
                            originName: wrapper.state.originName,
                            destName: wrapper.state.destName,
                            departsAt: wrapper.state.departsAt
                        )
                        SectionDivider()
                        MetaRow(
                            distanceMetres: Int(wrapper.state.distanceMetres),
                            estimatedDurationMinutes: Int(wrapper.state.estimatedDurationMinutes),
                            seatsAvailable: Int(wrapper.state.seatsAvailable)
                        )
                        SectionDivider()
                        PriceBreakdownCard(state: wrapper.state)
                            .padding(HopSpacing.md)
                        Spacer().frame(height: HopSpacing.md)
                    }
                }
            }

            HopButton(text: "Book Seat", variant: .primary) {
                wrapper.bookSeat()
            }
            .padding(HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            wrapper.startObserving { effect in
                if let n = effect as? TripDetailEffectNavigateToBookingConfirmation {
                    onNavigateToBookingConfirmation(n.tripId)
                } else if let p = effect as? TripDetailEffectNavigateToOtherProfile {
                    onNavigateToOtherProfile(p.driverId)
                }
            }
            wrapper.loadTrip(tripId: tripId)
        }
    }
}

private struct DriverHeader: View {
    let driverName: String
    let driverInitials: String
    let driverAvatarURL: URL?
    let driverRating: Double
    let isVerified: Bool
    let model: TripModel
    let onAvatarTap: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: HopSpacing.md) {
            Button(action: onAvatarTap) {
                HopAvatar(name: driverName, imageURL: driverAvatarURL, size: .xlarge, isVerified: isVerified)
            }
            .buttonStyle(.plain)
            VStack(alignment: .leading, spacing: 4) {
                Text(driverName)
                    .font(HopFont.headlineSmall(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                StarRatingDisplay(rating: driverRating, count: nil, starSize: 14)
                ModelBadge(model: model)
            }
            Spacer()
        }
        .padding(HopSpacing.md)
    }
}

private struct ModelBadge: View {
    let model: TripModel
    var body: some View {
        let isB = model == .b
        let bg: Color = isB ? Color.hopPrimaryGreen.opacity(0.15) : Color(hex: 0x1976D2).opacity(0.15)
        let fg: Color = isB ? Color.hopPrimaryGreen : Color(hex: 0x1976D2)
        let text: String = isB ? "Long Trip · Model B" : "Commute · Model A"
        Text(text)
            .font(HopFont.labelSmall(weight: .medium))
            .foregroundColor(fg)
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 4)
            .background(bg)
            .clipShape(Capsule())
    }
}

private struct RouteSection: View {
    let originName: String
    let destName: String
    let departsAt: String
    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack {
                Circle().fill(Color.hopPrimaryLime).frame(width: 12, height: 12)
                Text(originName).font(HopFont.bodyLarge()).foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack {
                Circle().fill(Color.hopAuthTextPrimary).frame(width: 12, height: 12)
                Text(destName).font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack(spacing: 6) {
                Image(systemName: "calendar").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(departsAt).font(HopFont.bodyMedium()).foregroundColor(Color.hopAuthTextSecondary)
            }
        }
        .padding(HopSpacing.md)
    }
}

private struct MetaRow: View {
    let distanceMetres: Int
    let estimatedDurationMinutes: Int
    let seatsAvailable: Int

    var body: some View {
        HStack {
            chip(systemImage: "ruler", text: "\(distanceMetres / 1000) km")
            chip(systemImage: "clock", text: "\(estimatedDurationMinutes) min")
            chip(systemImage: "person.2", text: "\(seatsAvailable) seat\(seatsAvailable == 1 ? "" : "s")")
            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.sm)
    }
    private func chip(systemImage: String, text: String) -> some View {
        HStack(spacing: 4) {
            Image(systemName: systemImage).font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
            Text(text).font(HopFont.labelMedium()).foregroundColor(Color.hopAuthTextPrimary)
        }
        .padding(.horizontal, HopSpacing.sm)
        .padding(.vertical, 6)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(Capsule())
    }
}

private struct PriceBreakdownCard: View {
    let state: TripDetailUiState

    var body: some View {
        let perSeat = Int(state.priceOerePerSeat)
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Price")
                .font(HopFont.titleSmall())
                .foregroundColor(Color.hopAuthTextPrimary)
            HStack {
                Text("Per seat").font(HopFont.bodyMedium()).foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
                Text(dkk(perSeat))
                    .font(HopFont.mono(size: 18, weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            Text("SKAT-suggested rate · pay driver via MobilePay after the ride")
                .font(HopFont.labelSmall())
                .foregroundColor(Color.hopAuthTextSecondary)

            if state.model == .b, let minT = state.minThreshold?.intValue, minT > 0 {
                Divider()
                Text("\(state.seatsBooked) of \(minT) seats confirmed")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                ProgressView(value: Double(state.thresholdProgress))
                    .tint(Color.hopPrimaryLime)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
    private func dkk(_ oere: Int) -> String {
        let d = oere / 100, o = oere % 100
        return String(format: "DKK %d,%02d", d, o)
    }
}

private struct SectionDivider: View {
    var body: some View {
        Rectangle().fill(Color.hopCardBorder).frame(height: 1).padding(.horizontal, HopSpacing.md)
    }
}

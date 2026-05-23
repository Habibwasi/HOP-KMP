import SwiftUI
import Shared

// MARK: — DR-08 Price Review & Confirm ────────────────────────────────────────

struct PriceReviewView: View {

    var onNavigateToMyTrips: () -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared
    @State private var toast: String? = nil

    var body: some View {
        let s = wrapper.state
        let summary = computeSummary(s)

        return ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    if let summary {
                        TripSummaryCard(summary: summary)
                        PriceBreakdownCard(summary: summary)
                        SkatWarningCard()
                    } else {
                        Text("No trip draft to review.")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }
                    Spacer().frame(height: 96)
                }
                .padding(HopSpacing.md)
            }

            VStack(spacing: HopSpacing.xs) {
                HopButton(
                    text: "Confirm & post trip",
                    variant: .primary,
                    isLoading: s.isPostingTrip,
                    isEnabled: summary != nil,
                    action: { wrapper.confirmAndPostTrip() }
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
        .task {
            for await effect in wrapper.effects {
                switch effect {
                case is DriverEffectNavigateToMyTrips:
                    onNavigateToMyTrips()
                case let snack as DriverEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                default: break
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Review price", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func computeSummary(_ state: DriverUiState) -> TripSummary? {
        if let a = state.pendingModelADraft {
            let dist = Int(a.distanceMetres)
            let seats = Int(a.seatsTotal)
            guard dist > 0, seats > 0 else { return nil }
            let price = PricingEngine.shared.calculate(distanceMetres: Int32(dist), seatsTotal: Int32(seats))
            return TripSummary(
                origin: a.originName,
                dest:   a.destName,
                schedule: "\(a.recurrenceDays.joined(separator: ", ")) · \(a.departureTime)",
                seats:    seats,
                distanceKm: dist / 1000,
                threshold: nil,
                pricePerSeatOere: Int(price.pricePerSeatOere)
            )
        }
        if let b = state.pendingModelBDraft {
            let dist = Int(b.distanceMetres)
            let seats = Int(b.seatsTotal)
            guard dist > 0, seats > 0 else { return nil }
            let price = PricingEngine.shared.calculate(distanceMetres: Int32(dist), seatsTotal: Int32(seats))
            return TripSummary(
                origin: b.originName,
                dest:   b.destName,
                schedule: "\(b.date) · \(b.departureTime)",
                seats:    seats,
                distanceKm: dist / 1000,
                threshold: Int(b.minThreshold),
                pricePerSeatOere: Int(price.pricePerSeatOere)
            )
        }
        return nil
    }
}

private struct TripSummary {
    let origin: String
    let dest:   String
    let schedule: String
    let seats:    Int
    let distanceKm: Int
    let threshold: Int?
    let pricePerSeatOere: Int
}

private struct TripSummaryCard: View {
    let summary: TripSummary

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Trip summary")
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextSecondary)

            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "mappin.circle.fill").foregroundColor(Color.hopPrimaryLime)
                Text("\(summary.origin) → \(summary.dest)")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "calendar").foregroundColor(Color.hopAuthTextSecondary)
                Text(summary.schedule)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "person.3").foregroundColor(Color.hopAuthTextSecondary)
                Text("\(summary.seats) seat\(summary.seats == 1 ? "" : "s")")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                if let t = summary.threshold {
                    Text("· min \(t) to run")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
                }
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "ruler").foregroundColor(Color.hopAuthTextSecondary)
                Text("\(summary.distanceKm) km")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct PriceBreakdownCard: View {
    let summary: TripSummary

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Price breakdown (per seat)")
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextSecondary)

            row(label: "Passengers pay / seat (SKAT rate)", oere: summary.pricePerSeatOere, highlight: false)
            Divider().background(Color.hopAuthTextSecondary.opacity(0.2))
            row(label: "You receive / seat", oere: summary.pricePerSeatOere, highlight: true)
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func row(label: String, oere: Int, highlight: Bool) -> some View {
        let kr = oere / 100
        let rem = oere % 100
        let priceText = rem == 0 ? "DKK \(kr)" : "DKK \(kr),\(String(format: "%02d", rem))"
        return HStack {
            Text(label)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
            Spacer()
            Text(priceText)
                .font(highlight ? HopFont.headlineSmall(weight: .bold) : HopFont.bodyMedium(weight: .semibold))
                .foregroundColor(highlight ? Color.hopPrimaryGreen : Color.hopAuthTextPrimary)
        }
    }
}

// MARK: — SKAT warning ────────────────────────────────────────────────────────

private struct SkatWarningCard: View {
    var body: some View {
        HStack(alignment: .top, spacing: HopSpacing.sm) {
            Image(systemName: "info.circle.fill")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(Color(hex: 0xB45309))
            VStack(alignment: .leading, spacing: 4) {
                Text("Tax reminder")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color(hex: 0x78350F))
                Text("Hop is not your tax authority. You're responsible for reporting earnings to SKAT. We'll show monthly estimates and an annual report to help — verify before filing.")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color(hex: 0x78350F))
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: 0xFEF3C7))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(hex: 0xFCD34D), lineWidth: 1)
        )
    }
}

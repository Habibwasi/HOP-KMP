import SwiftUI
import Shared

// MARK: — DR-08 Price Review & Confirm ────────────────────────────────────────

struct PriceReviewView: View {

    var onNavigateToMyTrips: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = DriverViewModelWrapper()
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
                    } else {
                        Text("No trip draft to review.")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopTextSecondary)
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
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Review price").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
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
                passengerPaysPerSeatOere: Int(price.passengerPaysPerSeatOere),
                platformFeeOere:          Int(price.platformFeeOere),
                driverNetPerSeatOere:     Int(price.driverNetPerSeatOere)
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
                passengerPaysPerSeatOere: Int(price.passengerPaysPerSeatOere),
                platformFeeOere:          Int(price.platformFeeOere),
                driverNetPerSeatOere:     Int(price.driverNetPerSeatOere)
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
    let passengerPaysPerSeatOere: Int
    let platformFeeOere: Int
    let driverNetPerSeatOere: Int
}

private struct TripSummaryCard: View {
    let summary: TripSummary

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Trip summary")
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopTextSecondary)

            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "mappin.circle.fill").foregroundColor(Color.hopPrimaryLime)
                Text("\(summary.origin) → \(summary.dest)")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "calendar").foregroundColor(Color.hopTextSecondary)
                Text(summary.schedule)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "person.3").foregroundColor(Color.hopTextSecondary)
                Text("\(summary.seats) seat\(summary.seats == 1 ? "" : "s")")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                if let t = summary.threshold {
                    Text("· min \(t) to run")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)
                }
            }
            HStack(spacing: HopSpacing.xs) {
                Image(systemName: "ruler").foregroundColor(Color.hopTextSecondary)
                Text("\(summary.distanceKm) km")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct PriceBreakdownCard: View {
    let summary: TripSummary

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text("Price breakdown (per seat)")
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopTextSecondary)

            row(label: "Passenger pays", oere: summary.passengerPaysPerSeatOere, highlight: false)
            row(label: "Platform fee (15%)", oere: summary.platformFeeOere, highlight: false)
            Divider().background(Color.hopTextSecondary.opacity(0.2))
            row(label: "You receive", oere: summary.driverNetPerSeatOere, highlight: true)
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func row(label: String, oere: Int, highlight: Bool) -> some View {
        HStack {
            Text(label)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextSecondary)
            Spacer()
            Text("DKK \(oere / 100)")
                .font(highlight ? HopFont.headlineSmall(weight: .bold) : HopFont.bodyMedium(weight: .semibold))
                .foregroundColor(highlight ? Color.hopPrimaryLime : Color.hopTextPrimary)
        }
    }
}

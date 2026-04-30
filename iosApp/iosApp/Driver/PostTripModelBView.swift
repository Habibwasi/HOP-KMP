import SwiftUI
import Shared

// MARK: — DR-07 Post Trip — Model B (One-Off Long Distance) ───────────────────

struct PostTripModelBView: View {

    var onNavigateToReview: () -> Void
    var onBack: () -> Void

    @ObservedObject private var wrapper = DriverViewModelWrapper.shared

    @State private var origin:       String = ""
    @State private var dest:         String = ""
    @State private var date:         String = ""    // YYYY-MM-DD
    @State private var time:         String = "08:00"
    @State private var seats:        Int    = 3
    @State private var minThreshold: Int    = 2
    @State private var distanceKm:   String = "100"

    private var canSubmit: Bool {
        !origin.isEmpty && !dest.isEmpty && !date.isEmpty && !time.isEmpty &&
        seats >= 1 && minThreshold >= 1 && minThreshold <= seats &&
        (Int(distanceKm) ?? 0) > 0
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Text("One-off Long Distance")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    HopTextField(label: "From", placeholder: "Origin", text: $origin)
                    HopTextField(label: "To",   placeholder: "Destination", text: $dest)

                    HopTextField(label: "Date (YYYY-MM-DD)", placeholder: "2026-05-20", text: $date)
                    HopTextField(label: "Departure time (HH:mm)", placeholder: "08:00", text: $time)
                    HopTextField(label: "Distance (km)", placeholder: "100", text: $distanceKm, keyboardType: .numberPad)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Total seats")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(seats)", value: $seats, in: 1...4)
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                    }

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Minimum confirmed before trip runs")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(minThreshold)", value: $minThreshold, in: 1...max(1, seats))
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Text("If fewer than \(minThreshold) passenger\(minThreshold == 1 ? "" : "s") book, the trip is cancelled and refunded.")
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Next: Review price",
                        variant: .primary,
                        isEnabled: canSubmit
                    ) {
                        let draft = ModelBDraft(
                            originName: origin,
                            destName: dest,
                            date: date,
                            departureTime: time,
                            seatsTotal: Int32(seats),
                            minThreshold: Int32(minThreshold),
                            distanceMetres: Int32((Int(distanceKm) ?? 0) * 1000)
                        )
                        wrapper.submitModelBDraft(draft)
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .task {
            for await effect in wrapper.effects {
                if effect is DriverEffectNavigateToPriceReview {
                    onNavigateToReview()
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "One-off long trip", onBack: onBack)
            .background(Color.hopBackground)
    }
    }
}

import SwiftUI
import Shared

// MARK: — DR-06 Post Trip — Model A (Daily Commute) ──────────────────────────

struct PostTripModelAView: View {

    var onNavigateToReview: () -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    @State private var origin:    String = ""
    @State private var dest:      String = ""
    @State private var time:      String = "08:00"
    @State private var seats:     Int    = 3
    @State private var distanceKm: String = "20"
    @State private var selectedDays: Set<String> = ["MON", "TUE", "WED", "THU", "FRI"]

    private let days = [("MON","M"),("TUE","T"),("WED","W"),("THU","T"),("FRI","F"),("SAT","S"),("SUN","S")]

    private var canSubmit: Bool {
        !origin.isEmpty && !dest.isEmpty && !time.isEmpty && seats >= 1 &&
        !selectedDays.isEmpty && (Int(distanceKm) ?? 0) > 0
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Text("Daily Commute")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    HopTextField(label: "From", placeholder: "Origin", text: $origin)
                    HopTextField(label: "To",   placeholder: "Destination", text: $dest)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Days")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        HStack(spacing: HopSpacing.xs) {
                            ForEach(days, id: \.0) { day in
                                let id = day.0
                                let label = day.1
                                let isOn = selectedDays.contains(id)
                                Button {
                                    if isOn { selectedDays.remove(id) } else { selectedDays.insert(id) }
                                } label: {
                                    Text(label)
                                        .font(HopFont.labelMedium(weight: .semibold))
                                        .foregroundColor(isOn ? Color.hopSurface : Color.hopAuthTextPrimary)
                                        .frame(width: 36, height: 36)
                                        .background(isOn ? Color.hopPrimaryLime : Color.hopCardSurfaceMuted)
                                        .clipShape(Circle())
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    HopTextField(label: "Departure time (HH:mm)", placeholder: "08:00", text: $time)
                    HopTextField(label: "Distance (km)", placeholder: "20", text: $distanceKm, keyboardType: .numberPad)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Seats")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper("\(seats)", value: $seats, in: 1...4)
                            .padding(HopSpacing.sm)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .foregroundColor(Color.hopAuthTextPrimary)
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Next: Review price",
                        variant: .primary,
                        isEnabled: canSubmit
                    ) {
                        let draft = ModelADraft(
                            originName: origin,
                            destName: dest,
                            recurrenceDays: Array(selectedDays).sorted(),
                            departureTime: time,
                            seatsTotal: Int32(seats),
                            distanceMetres: Int32((Int(distanceKm) ?? 0) * 1000)
                        )
                        wrapper.submitModelADraft(draft)
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
        DriverTopBar(title: "Daily commute", onBack: onBack)
            .background(Color.hopBackground)
    }
    }
}

import SwiftUI
import Shared

// MARK: — DR-02 Car Details (Step 1 of Enable-Driver) ─────────────────────────

struct CarDetailsView: View {

    var onNavigateBack: () -> Void
    var onNavigateNext: () -> Void  // → DR-03 LicenceUpload

    @ObservedObject private var wrapper = DriverViewModelWrapper.shared

    @State private var make:    String = ""
    @State private var model:   String = ""
    @State private var year:    String = ""
    @State private var plate:   String = ""
    @State private var colour:  String = ""
    @State private var seats:   Int    = 4

    private var canSubmit: Bool {
        !make.isEmpty && !model.isEmpty && Int(year) != nil &&
        !plate.isEmpty && !colour.isEmpty && seats >= 1 && seats <= 4
    }

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    Text("Tell us about your car")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    HopTextField(label: "Make", placeholder: "e.g. Toyota", text: $make)
                    HopTextField(label: "Model", placeholder: "e.g. Corolla", text: $model)
                    HopTextField(label: "Year", placeholder: "2020", text: $year, keyboardType: .numberPad)
                    HopTextField(label: "Licence plate", placeholder: "AB 12 345", text: $plate)
                    HopTextField(label: "Colour", placeholder: "e.g. Silver", text: $colour)

                    VStack(alignment: .leading, spacing: HopSpacing.xs) {
                        Text("Available seats (excluding driver)")
                            .font(HopFont.labelSmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Stepper(value: $seats, in: 1...4) {
                            Text("\(seats)")
                                .font(HopFont.bodyLarge(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                        }
                        .padding(HopSpacing.sm)
                        .background(Color.hopCardSurfaceMuted)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    Spacer().frame(height: HopSpacing.lg)

                    HopButton(
                        text: "Save & continue",
                        variant: .primary,
                        isLoading: wrapper.state.isSubmittingOnboarding,
                        isEnabled: canSubmit
                    ) {
                        let car = CarDetails(
                            make: make,
                            model: model,
                            year: Int32(Int(year) ?? 0),
                            licensePlate: plate,
                            colour: colour,
                            seatsAvailable: Int32(seats)
                        )
                        wrapper.saveCarDetails(car)
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .task {
            for await effect in wrapper.effects {
                if effect is DriverEffectNavigateToHome {
                    onNavigateNext()
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Car details", onBack: onNavigateBack)
            .background(Color.hopBackground)
    }
    }
}

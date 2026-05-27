import SwiftUI
import Shared

// MARK: — DR-03 Enable Driver: MobilePay Number ───────────────────────────────
//
// Second driver-onboarding step (after car details).
// Asks the new driver for their 8-digit MobilePay number.
// Saving navigates home; "Skip for now" also goes home.

struct MobilepayOnboardingView: View {

    var onNavigateBack: () -> Void
    var onNavigateNext: () -> Void   // → home after save or skip

    @StateObject private var wrapper = OwnProfileViewModelWrapper()
    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                // Top bar
                HStack(spacing: 0) {
                    Button(action: onNavigateBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 18, weight: .regular))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("Back")

                    Text("Step 2 of 2")
                        .font(HopFont.labelSmall())
                        .foregroundColor(Color.hopAuthTextSecondary)

                    Spacer()
                }
                .padding(.horizontal, HopSpacing.xs)
                .padding(.vertical, HopSpacing.xs)
                .background(Color.hopBackground)

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: HopSpacing.md) {
                        Spacer().frame(height: HopSpacing.lg)

                        Text("Your MobilePay number")
                            .font(HopFont.headlineSmall(weight: .bold))
                            .foregroundColor(Color.hopAuthTextPrimary)

                        Text("Passengers send payment directly to you via MobilePay after each ride. You can update this at any time from your profile.")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)

                        Spacer().frame(height: HopSpacing.sm)

                        HopTextField(
                            label: "MobilePay number",
                            placeholder: "8-digit number, e.g. 20123456",
                            text: Binding(
                                get: { wrapper.state.mobilepayDraft },
                                set: { new in
                                    let digits = new.filter { $0.isNumber }
                                    if digits.count <= 8 {
                                        wrapper.mobilepayDraftChanged(digits)
                                    }
                                }
                            ),
                            keyboardType: .numberPad,
                            isEnabled: !wrapper.state.isSavingMobilepay,
                        )

                        Spacer().frame(height: HopSpacing.xl)

                        HopButton(
                            text: "Save & Finish",
                            variant: .primary,
                            isLoading: wrapper.state.isSavingMobilepay,
                            isEnabled: wrapper.state.mobilepayDraft.count == 8
                        ) {
                            wrapper.saveMobilepay()
                        }

                        HopButton(text: "Skip for now", variant: .ghost, lightSurface: true) {
                            onNavigateNext()
                        }
                        .disabled(wrapper.state.isSavingMobilepay)
                    }
                    .padding(HopSpacing.md)
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let snack as OwnProfileEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                    if snack.message.lowercased().contains("updated") {
                        onNavigateNext()
                    }
                default: break
                }
            }
            wrapper.startEditMobilepay()
        }
    }
}

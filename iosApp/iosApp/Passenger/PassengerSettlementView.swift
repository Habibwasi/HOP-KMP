import SwiftUI
import Shared

// MARK: — SE-01 Passenger Settlement ─────────────────────────────────────────
//
// Passenger pays the driver via MobilePay after the ride.
// Shows the amount to send, the driver's MobilePay number, and buttons to
// open MobilePay and mark the payment as sent.

struct PassengerSettlementView: View {

    let bookingId: String
    var onBack: () -> Void
    var onSettlementComplete: () -> Void

    @StateObject private var wrapper = SettlementViewModelWrapper()
    @State private var toast: String? = nil
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top bar
                HStack(spacing: 0) {
                    Button(action: onBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 18, weight: .regular))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .frame(width: 40, height: 40)
                    }
                    .accessibilityLabel("Back")

                    Text("Pay Your Driver")
                        .font(HopFont.bodyLarge(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)

                    Spacer()
                }
                .padding(.horizontal, HopSpacing.xs)
                .padding(.vertical, HopSpacing.xs)
                .background(Color.hopBackground)

                if wrapper.state.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                        .scaleEffect(1.3)
                    Spacer()
                } else if let settlement = wrapper.state.settlement {
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: HopSpacing.md) {
                            // Amount card
                            VStack(alignment: .leading, spacing: HopSpacing.sm) {
                                Text("Amount to send")
                                    .font(HopFont.labelSmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                Text("DKK \(settlement.suggestedAmountOere / 100)")
                                    .font(.system(size: 32, weight: .bold, design: .monospaced))
                                    .foregroundColor(Color.hopPrimaryLime)
                                Text("SKAT-suggested rate · send directly to driver's MobilePay")
                                    .font(HopFont.bodySmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(HopSpacing.md)
                            .background(Color.hopAuthInputSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 16))

                            // MobilePay number
                            VStack(alignment: .leading, spacing: HopSpacing.xs) {
                                Text("Driver MobilePay")
                                    .font(HopFont.labelSmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                Text(settlement.mobilepayNumber)
                                    .font(.system(size: 22, weight: .bold, design: .monospaced))
                                    .foregroundColor(Color.hopAuthTextPrimary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(HopSpacing.md)
                            .background(Color.hopAuthInputSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 16))

                            if settlement.passengerPaidAt != nil {
                                HStack(spacing: HopSpacing.sm) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(Color.hopPrimaryLime)
                                    Text("You marked this as paid — waiting for driver to confirm")
                                        .font(HopFont.bodyMedium())
                                        .foregroundColor(Color.hopPrimaryLime)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                        .padding(HopSpacing.md)
                        .padding(.bottom, 120)
                    }

                    // Action buttons
                    VStack(spacing: HopSpacing.sm) {
                        if settlement.passengerPaidAt == nil {
                            HopButton(text: "Open MobilePay", variant: .primary) {
                                wrapper.openMobilepay()
                            }
                            HopButton(
                                text: "I Have Paid",
                                variant: .ghost,
                                isLoading: wrapper.state.isMarkingPaid
                            ) {
                                wrapper.markPaid()
                            }
                        } else {
                            Text("Waiting for driver to confirm payment receipt…")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                            if settlement.driverConfirmedAt == nil {
                                HopButton(
                                    text: "I Haven't Paid Yet",
                                    variant: .ghost,
                                    isLoading: wrapper.state.isUnmarkingPaid
                                ) {
                                    wrapper.unmarkPaid()
                                }
                            }
                        }
                    }
                    .padding(HopSpacing.md)
                    .background(Color.hopBackground)
                    .padding(.bottom, HopSpacing.lg)
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                wrapper.refresh()
            }
        }
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is SettlementEffectOpenMobilepayDeeplink:
                    let deeplink = effect as! SettlementEffectOpenMobilepayDeeplink
                    if let url = URL(string: deeplink.uri) {
                        UIApplication.shared.open(url)
                    }
                case is SettlementEffectPaymentMarkedSuccess:
                    withAnimation { toast = "Marked as paid — waiting for driver confirmation" }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                case is SettlementEffectUnmarkPaidSuccess:
                    withAnimation { toast = "Unmarked — you can re-send payment when ready" }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                case is SettlementEffectConfirmReceivedSuccess:
                    onSettlementComplete()
                case let snack as SettlementEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                default: break
                }
            }
            wrapper.load(bookingId: bookingId)
        }
    }
}

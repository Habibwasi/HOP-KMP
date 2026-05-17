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
    @State private var isOpeningMobilepay = false
    @State private var copied = false
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

                            // MobilePay number card with copy button
                            Button(action: {
                                UIPasteboard.general.string = settlement.mobilepayNumber
                                withAnimation { copied = true }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                    withAnimation { copied = false }
                                }
                            }) {
                                VStack(alignment: .leading, spacing: HopSpacing.xs) {
                                    Text("Driver MobilePay")
                                        .font(HopFont.labelSmall())
                                        .foregroundColor(Color.hopAuthTextSecondary)
                                    HStack {
                                        Text(settlement.mobilepayNumber)
                                            .font(.system(size: 22, weight: .bold, design: .monospaced))
                                            .foregroundColor(Color.hopAuthTextPrimary)
                                        Spacer()
                                        HStack(spacing: 4) {
                                            Image(systemName: "doc.on.doc")
                                                .font(.system(size: 14))
                                                .foregroundColor(copied ? Color.hopPrimaryLime : Color.hopAuthTextSecondary)
                                            Text(copied ? "Copied!" : "Copy")
                                                .font(HopFont.labelSmall())
                                                .foregroundColor(copied ? Color.hopPrimaryLime : Color.hopAuthTextSecondary)
                                        }
                                    }
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(HopSpacing.md)
                                .background(Color.hopAuthInputSurface)
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                            }
                            .buttonStyle(.plain)

                            // Instruction hint
                            Text("Open MobilePay → paste the number → send DKK \(settlement.suggestedAmountOere / 100)")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                                .frame(maxWidth: .infinity, alignment: .leading)

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
                                isLoading: wrapper.state.isMarkingPaid,
                                lightSurface: true
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
                                    isLoading: wrapper.state.isUnmarkingPaid,
                                    lightSurface: true
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
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                if isOpeningMobilepay {
                    // Returning from MobilePay — skip this refresh and clear the flag.
                    isOpeningMobilepay = false
                } else {
                    wrapper.refresh()
                }
            }
        }
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is SettlementEffectOpenMobilepayDeeplink:
                    let deeplink = effect as! SettlementEffectOpenMobilepayDeeplink
                    if let url = URL(string: deeplink.uri) {
                        if UIApplication.shared.canOpenURL(url) {
                            isOpeningMobilepay = true
                            UIApplication.shared.open(url)
                        } else {
                            withAnimation { toast = "MobilePay is not installed" }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                                withAnimation { toast = nil }
                            }
                        }
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

import SwiftUI
import Shared

// MARK: — SE-02 Driver Settlement ─────────────────────────────────────────────
//
// Driver confirms or disputes received payment after the passenger marks "paid".
// Shows the expected amount, passenger payment status, and confirm / dispute
// action buttons.

struct DriverSettlementView: View {

    let bookingId: String
    var onBack: () -> Void
    var onSettlementComplete: () -> Void

    @StateObject private var wrapper = SettlementViewModelWrapper()
    @State private var showDisputeInput: Bool = false
    @State private var toast: String? = nil

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

                    Text("Payment Confirmation")
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
                                Text("Expected payment")
                                    .font(HopFont.labelSmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                Text("DKK \(settlement.suggestedAmountOere / 100)")
                                    .font(.system(size: 32, weight: .bold, design: .monospaced))
                                    .foregroundColor(Color.hopPrimaryLime)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(HopSpacing.md)
                            .background(Color.hopAuthInputSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 16))

                            // Payment status
                            if settlement.passengerPaidAt != nil {
                                HStack(spacing: HopSpacing.sm) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(Color.hopPrimaryLime)
                                    Text("Passenger has marked this as paid")
                                        .font(HopFont.bodyMedium(weight: .medium))
                                        .foregroundColor(Color.hopPrimaryLime)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                            } else {
                                Text("Waiting for passenger to send payment via MobilePay…")
                                    .font(HopFont.bodySmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }

                            // Dispute input (shown when toggled)
                            if showDisputeInput {
                                VStack(alignment: .leading, spacing: HopSpacing.sm) {
                                    Text("Dispute reason")
                                        .font(HopFont.labelSmall())
                                        .foregroundColor(Color.hopAuthTextSecondary)

                                    TextEditor(
                                        text: Binding(
                                            get: { wrapper.state.disputeReason },
                                            set: { wrapper.disputeReasonChanged($0) }
                                        )
                                    )
                                    .frame(minHeight: 80)
                                    .padding(HopSpacing.sm)
                                    .background(Color.hopBackground)
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(Color.hopAuthTextSecondary.opacity(0.4), lineWidth: 1)
                                    )

                                    Text("Minimum 10 characters")
                                        .font(.system(size: 11))
                                        .foregroundColor(Color.hopAuthTextSecondary)

                                    HopButton(
                                        text: "Submit Dispute",
                                        variant: .primary,
                                        isLoading: wrapper.state.isDisputing,
                                        isEnabled: wrapper.state.disputeReason.count >= 10
                                    ) {
                                        wrapper.submitDispute()
                                    }
                                }
                                .padding(HopSpacing.md)
                                .background(Color.hopAuthInputSurface)
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                            }
                        }
                        .padding(HopSpacing.md)
                        .padding(.bottom, 120)
                    }

                    // Action buttons
                    if settlement.passengerPaidAt != nil && settlement.driverConfirmedAt == nil {
                        VStack(spacing: HopSpacing.sm) {
                            HopButton(
                                text: "Confirm Received",
                                variant: .primary,
                                isLoading: wrapper.state.isConfirming
                            ) {
                                wrapper.confirmReceived()
                            }
                            HopButton(text: "Dispute", variant: .ghost, lightSurface: true) {
                                withAnimation { showDisputeInput.toggle() }
                            }
                        }
                        .padding(HopSpacing.md)
                        .background(Color.hopBackground)
                        .padding(.bottom, HopSpacing.lg)
                    }
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is SettlementEffectConfirmReceivedSuccess:
                    onSettlementComplete()
                case is SettlementEffectDisputeSubmittedSuccess:
                    withAnimation { toast = "Dispute submitted" }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        onSettlementComplete()
                    }
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

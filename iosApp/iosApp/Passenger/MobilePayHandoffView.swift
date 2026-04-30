import SwiftUI
import Shared

/// PA-05 — MobilePay Handoff. Mirrors composeApp `MobilePayHandoffScreen.kt`.
/// 3 phases: Launching / Waiting / Failed. Deep-link mobilepay://merchant
/// then on scenePhase .active fire ConfirmPaymentSuccess (idempotency guard
/// in shared VM prevents duplicate transitions).
struct MobilePayHandoffView: View {
    let bookingId: String
    let onSuccess: (_ bookingId: String) -> Void
    let onBack: () -> Void

    private enum Phase { case launching, waiting, failed }

    @StateObject private var wrapper = BookingViewModelWrapper()
    @State private var phase: Phase = .launching
    @State private var hasLaunched: Bool = false
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()
            switch phase {
            case .launching: LaunchingContent()
            case .waiting:   WaitingContent()
            case .failed:    FailedContent(onRetry: onBack, onGoBack: onBack)
            }
        }
        .onAppear {
            wrapper.startObserving { effect in
                if let nav = effect as? BookingEffectNavigateToSuccess {
                    onSuccess(nav.bookingId)
                }
            }
            wrapper.beginHandoff(bookingId: bookingId)
            launchMobilePay()
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .active && hasLaunched {
                wrapper.confirmPaymentSuccess(bookingId: bookingId)
            }
        }
        .onChange(of: wrapper.state.paymentState) { _, newState in
            if newState == .failed { phase = .failed }
        }
    }

    private func launchMobilePay() {
        guard !hasLaunched else { return }
        let urlString = "mobilepay://merchant?orderId=\(bookingId)"
        if let url = URL(string: urlString), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
        hasLaunched = true
        phase = .waiting
    }
}

private struct MobilePayLogoBadge: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(hex: 0x5A78FF))
                .frame(width: 64, height: 64)
            Text("M")
                .font(.system(size: 32, weight: .bold))
                .foregroundColor(.white)
        }
    }
}

private struct LaunchingContent: View {
    var body: some View {
        VStack(spacing: HopSpacing.lg) {
            MobilePayLogoBadge()
            ProgressView().tint(Color.hopPrimaryGreen).scaleEffect(1.2)
            Text("Opening MobilePay...")
                .font(HopFont.headlineSmall(weight: .bold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)
            Text("You're being redirected to MobilePay\nto complete your payment.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
        }
        .padding(HopSpacing.xl)
    }
}

private struct WaitingContent: View {
    var body: some View {
        VStack(spacing: HopSpacing.lg) {
            MobilePayLogoBadge()
            ProgressView().tint(Color.hopPrimaryGreen).scaleEffect(1.2)
            Text("Waiting for payment confirmation...")
                .font(HopFont.headlineSmall(weight: .bold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)
            Text("Please complete the payment in MobilePay\nand return to this screen.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
        }
        .padding(HopSpacing.xl)
    }
}

private struct FailedContent: View {
    let onRetry: () -> Void
    let onGoBack: () -> Void

    var body: some View {
        VStack(spacing: HopSpacing.lg) {
            ZStack {
                Circle().fill(Color.hopError.opacity(0.15)).frame(width: 64, height: 64)
                Image(systemName: "xmark")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(Color.hopError)
            }
            Text("Payment failed")
                .font(HopFont.headlineSmall(weight: .bold))
                .foregroundColor(Color.hopAuthTextPrimary)
            Text("We couldn't confirm the payment.\nPlease try again.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
            VStack(spacing: HopSpacing.sm) {
                HopButton(text: "Retry payment", variant: .primary, action: onRetry)
                HopButton(text: "Go back", variant: .ghost, lightSurface: true, action: onGoBack)
            }
        }
        .padding(HopSpacing.xl)
    }
}

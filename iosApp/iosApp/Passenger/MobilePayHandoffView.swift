import SwiftUI
import Shared

// MARK: — PA-05 MobilePay Handoff ─────────────────────────────────────────────
//
// Opens MobilePay via universal link (UIApplication.shared.open).
// Detects return via .onOpenURL and dispatches ConfirmPaymentSuccess.
// Effect BookingEffect.NavigateToSuccess → onSuccess(bookingId).

struct MobilePayHandoffView: View {

    let bookingId:  String
    let redirectURL: String   // universal link returned by backend / CreateBooking

    var onSuccess:  (String) -> Void   // bookingId → PA-06
    var onBack:     () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()

    @State private var didAttemptOpen = false
    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: HopSpacing.xl) {
                Spacer()

                // ── MobilePay logo placeholder ────────────────────────────────
                ZStack {
                    Circle()
                        .fill(Color.hopSurfaceElevated)
                        .frame(width: 100, height: 100)
                    Image(systemName: "creditcard.and.123")
                        .font(.system(size: 42, weight: .light))
                        .foregroundColor(Color.hopPrimaryGreen)
                }
                .scaleEffect(didAttemptOpen ? 1.0 : 0.85)
                .animation(.spring(response: 0.4, dampingFraction: 0.65), value: didAttemptOpen)

                // ── Status text ───────────────────────────────────────────────
                VStack(spacing: HopSpacing.xs) {
                    Text(didAttemptOpen ? "Complete payment in MobilePay" : "Opening MobilePay…")
                        .font(HopFont.headlineSmall())
                        .fontWeight(.semibold)
                        .foregroundColor(Color.hopTextPrimary)
                        .multilineTextAlignment(.center)

                    Text("Your payment is held until you confirm in the MobilePay app.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.xl)
                }

                // ── Spinner while waiting ─────────────────────────────────────
                if didAttemptOpen {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                        .scaleEffect(1.2)
                }

                Spacer()

                // ── Manual fallback CTA ───────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    HopPrimaryButton(title: "Open MobilePay") {
                        openMobilePay()
                    }

                    HopButton(text: "Cancel booking", variant: .ghost) {
                        onBack()
                    }
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xl)
            }

            // ── Toast ─────────────────────────────────────────────────────────
            if let msg = toastMessage {
                VStack {
                    Spacer()
                    HopToast(message: msg)
                        .padding(.bottom, HopSpacing.xl)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .onAppear {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toastMessage = nil }
                    }
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            // Minimal back — in active payment flow we show a cancel-are-you-sure
            // instead of a silent pop.
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color.hopTextSecondary)
                }
                .accessibilityLabel("Cancel")
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                handleEffect(effect)
            }
            wrapper.beginHandoff(bookingId: bookingId)
            // Auto-open on appearance
            try? await Task.sleep(nanoseconds: 400_000_000)
            openMobilePay()
        }
        // Detect app return from MobilePay via universal link callback
        .onOpenURL { url in
            guard url.absoluteString.contains(bookingId) else { return }
            wrapper.confirmPaymentSuccess(bookingId: bookingId)
        }
        // iOS lifecycle: app foregrounded after switching away
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            guard didAttemptOpen else { return }
            // When returning from MobilePay without a deep-link callback,
            // confirm success (MobilePay webhook will settle on backend).
            wrapper.confirmPaymentSuccess(bookingId: bookingId)
        }
    }

    // MARK: — Helpers

    private func openMobilePay() {
        guard let url = URL(string: redirectURL), !redirectURL.isEmpty else {
            // redirectURL not yet available (use-case: screen loaded before URL resolved)
            withAnimation { toastMessage = "Could not open MobilePay. Please try again." }
            return
        }
        UIApplication.shared.open(url)
        withAnimation { didAttemptOpen = true }
    }

    private func handleEffect(_ effect: any BookingEffect) {
        if let nav = effect as? BookingEffectNavigateToSuccess {
            onSuccess(nav.bookingId)
        } else if let snack = effect as? BookingEffectShowSnackbar {
            withAnimation { toastMessage = snack.message }
        }
    }
}

// MARK: — Previews

#Preview("PA-05 MobilePay Handoff — Waiting") {
    NavigationStack {
        MobilePayHandoffView(
            bookingId:   "bk-001",
            redirectURL: "https://example.com/mobilepay",
            onSuccess:   { _ in },
            onBack:      {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-05 MobilePay Handoff — Initial") {
    NavigationStack {
        MobilePayHandoffView(
            bookingId:   "bk-001",
            redirectURL: "",
            onSuccess:   { _ in },
            onBack:      {}
        )
    }
    .preferredColorScheme(.dark)
}

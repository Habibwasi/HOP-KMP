import SwiftUI

// MARK: — PA-10 Cancellation Confirmation ─────────────────────────────────────
//
// Pure information screen shown after a booking is successfully cancelled.
// No ViewModel required — state change was completed upstream.
// CTA: Back to Home (removes the whole nav stack).

struct CancellationConfirmationView: View {

    let bookingId: String
    var onGoHome:  () -> Void

    @State private var circleScale:   CGFloat = 0.5
    @State private var circleOpacity: Double  = 0.0
    @State private var iconScale:     CGFloat = 0.4
    @State private var contentOffset: CGFloat = 30
    @State private var contentOpacity:Double  = 0.0

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {

                Spacer()

                // ── Animated icon ─────────────────────────────────────────────
                ZStack {
                    Circle()
                        .fill(Color.hopError.opacity(0.12))
                        .frame(width: 120, height: 120)
                        .scaleEffect(circleScale)
                        .opacity(circleOpacity)

                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 60, weight: .light))
                        .foregroundColor(Color.hopError)
                        .scaleEffect(iconScale)
                }
                .padding(.bottom, HopSpacing.xl)

                // ── Text ──────────────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    Text("Booking Cancelled")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopTextPrimary)

                    Text("Your booking has been successfully cancelled.")
                        .font(HopFont.bodyLarge()).foregroundColor(Color.hopTextSecondary)
                        .multilineTextAlignment(.center)

                    // Booking reference pill
                    Label("#\(bookingId.prefix(8).uppercased())", systemImage: "ticket")
                        .font(HopFont.bodySmall(weight: .medium))
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.vertical, 6)
                        .padding(.horizontal, 12)
                        .background(Color.hopSurfaceElevated)
                        .clipShape(Capsule())
                        .padding(.top, HopSpacing.xs)
                }
                .padding(.horizontal, HopSpacing.lg)
                .offset(y: contentOffset)
                .opacity(contentOpacity)

                Spacer()

                // ── CTA ───────────────────────────────────────────────────────
                HopPrimaryButton(title: "Back to My Trips") {
                    onGoHome()
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xl)
                .offset(y: contentOffset)
                .opacity(contentOpacity)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .onAppear { animate() }
    }

    // MARK: — Animation

    private func animate() {
        withAnimation(.spring(response: 0.5, dampingFraction: 0.7).delay(0.1)) {
            circleScale   = 1
            circleOpacity = 1
            iconScale     = 1
        }
        withAnimation(.easeOut(duration: 0.45).delay(0.35)) {
            contentOffset  = 0
            contentOpacity = 1
        }
    }

}

// MARK: — Preview

#Preview("PA-10 Cancellation Confirmation") {
    NavigationStack {
        CancellationConfirmationView(bookingId: "bk-abc123", onGoHome: {})
    }
    .preferredColorScheme(.dark)
}

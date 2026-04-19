import SwiftUI
import Shared

// ── ON-01 Splash ──────────────────────────────────────────────────────────────
// Splash is purely navigational — no ViewModel needed here.
// Authentication state is checked per-screen in AuthViewModelWrapper.

struct SplashView: View {

    /// Fired by this view once auth check completes.
    var onNavigateToSignUp: () -> Void
    var onNavigateToHome: () -> Void

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: HopSpacing.lg) {
                Spacer()

                // ── Logotype ──────────────────────────────────────────────────
                VStack(spacing: HopSpacing.xs) {
                    ZStack {
                        Circle()
                            .fill(Color.hopPrimaryLime)
                            .frame(width: 80, height: 80)
                        Image(systemName: "car.fill")
                            .font(.system(size: 36, weight: .semibold))
                            .foregroundColor(Color.hopSurface)
                    }

                    Text("Hop")
                        .font(.system(size: 40, weight: .bold, design: .default))
                        .foregroundColor(Color.hopTextPrimary)

                    Text("Carpooling for Denmark")
                        .font(HopFont.body)
                        .foregroundColor(Color.hopTextSecondary)
                }

                Spacer()

                // ── CTA buttons ────────────────────────────────────────────────
                VStack(spacing: HopSpacing.md) {
                    HopPrimaryButton(title: "Get started") {
                        onNavigateToSignUp()
                    }

                    HopGhostButton(title: "Log in") {
                        onNavigateToHome()
                    }
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xl)
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

#Preview("Default") {
    SplashView(onNavigateToSignUp: {}, onNavigateToHome: {})
}

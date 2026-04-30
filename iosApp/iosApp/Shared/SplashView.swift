import SwiftUI

// MARK: — Splash screen ───────────────────────────────────────────────────────
//
// Shown while the app boots and AuthViewModel performs silent session
// restore.  Brand-faithful lightweight render: the Hop wordmark on the
// brand-lime background with a subtle progress indicator.  The full
// animated brand splash from `SplashScreen.kt` is reserved for a future
// pass — the current parity goal is the colour palette, type, and
// presentation.

struct SplashView: View {
    var body: some View {
        ZStack {
            Color.hopPrimaryLime.ignoresSafeArea()

            VStack(spacing: HopSpacing.xl) {
                HopLogo(width: 220, height: 94)

                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthTextPrimary))
                    .scaleEffect(1.1)
            }
        }
    }
}

#Preview { SplashView() }

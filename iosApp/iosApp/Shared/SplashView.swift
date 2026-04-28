import SwiftUI

// MARK: — Splash screen ───────────────────────────────────────────────────────
//
// Shown while the app boots and AuthViewModel performs silent session restore.
// Mirrors `SplashScreen.kt`.

struct SplashView: View {
    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: HopSpacing.lg) {
                Text("Hop")
                    .font(HopFont.displayLarge())
                    .foregroundColor(Color.hopPrimaryLime)

                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
            }
        }
    }
}

#Preview { SplashView().preferredColorScheme(.dark) }

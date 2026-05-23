import SwiftUI

// MARK: - ON-02b Email Verified ───────────────────────────────────────────────
// Success screen shown briefly after the deep-link callback is processed.
// Auto-advances to Home after 2 seconds; no user input required.

struct EmailVerifiedView: View {
    var onNavigateToHome: () -> Void

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer()

                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 88))
                    .foregroundColor(Color.hopAuthAccent)

                Spacer().frame(height: HopSpacing.lg)

                Text("Email verified!")
                    .font(HopFont.headlineSmall(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: HopSpacing.sm)

                Text("Your account is ready. Taking you home…")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)

                Spacer()
            }
            .padding(.horizontal, HopSpacing.md)
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                onNavigateToHome()
            }
        }
    }
}

#Preview {
    EmailVerifiedView(onNavigateToHome: {})
}

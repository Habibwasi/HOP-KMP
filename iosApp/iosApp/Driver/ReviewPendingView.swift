import SwiftUI

// MARK: — DR-04 Review Pending (Step 3 of Enable-Driver) ──────────────────────

struct ReviewPendingView: View {

    var onNavigateToHome: () -> Void

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: HopSpacing.lg) {
                Spacer()

                ZStack {
                    Circle()
                        .fill(Color.hopPrimaryLime.opacity(0.2))
                        .frame(width: 120, height: 120)
                    Image(systemName: "clock.fill")
                        .font(.system(size: 56))
                        .foregroundColor(Color.hopPrimaryLime)
                }

                Text("Review pending")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)

                Text("Thanks for joining! We're reviewing your details and will email you within 24 hours once approved.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, HopSpacing.lg)

                Spacer()

                HopButton(text: "Back to home", variant: .primary, action: onNavigateToHome)
                    .padding(HopSpacing.md)
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Review pending", onBack: onNavigateToHome)
            .background(Color.hopBackground)
    }
    }
}

import SwiftUI

// ── ON-01 Onboarding ──────────────────────────────────────────────────────────
//
// Three swipeable slides introducing Hop's value proposition.
// Mirrors OnboardingScreen.kt — no ViewModel needed, purely navigational.
//

// MARK: - Data

private struct OnboardingSlideData {
    let illustrationDescription: String
    let headline: String
    let subheadline: String
}

private let onboardingSlides: [OnboardingSlideData] = [
    OnboardingSlideData(
        illustrationDescription: "Two cars side by side",
        headline: "Travel cheaper.\nDrive smarter.",
        subheadline: "Denmark's carpooling platform.\nCost-sharing, not taxi."
    ),
    OnboardingSlideData(
        illustrationDescription: "DKK coin illustration",
        headline: "Earn while\nyou commute.",
        subheadline: "Share your empty seats and\nlet SKAT-compliant earnings\nland straight in your pocket."
    ),
    OnboardingSlideData(
        illustrationDescription: "Driver dashboard illustration",
        headline: "Tax-smart by\ndesign.",
        subheadline: "Every trip is logged and priced\nwithin the SKAT rate of DKK 2.28/km.\nNo surprises at year-end."
    ),
]

// MARK: - OnboardingView

struct OnboardingView: View {

    var onNavigateToSignUp: () -> Void
    var onNavigateToLogin: () -> Void

    @State private var currentPage = 0

    var body: some View {
        ZStack(alignment: .bottom) {
            // ── Slide pager ────────────────────────────────────────────────────
            TabView(selection: $currentPage) {
                ForEach(Array(onboardingSlides.enumerated()), id: \.offset) { index, slide in
                    OnboardingSlideView(slide: slide)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .ignoresSafeArea()

            // ── Bottom chrome: dots + CTAs ─────────────────────────────────────
            VStack(spacing: HopSpacing.md) {
                OnboardingSlideDots(
                    pageCount: onboardingSlides.count,
                    currentPage: currentPage
                )

                Spacer().frame(height: HopSpacing.sm)

                HopPrimaryButton(title: "Get Started") {
                    onNavigateToSignUp()
                }

                HopGhostButton(title: "Log In") {
                    onNavigateToLogin()
                }
            }
            .padding(.horizontal, HopSpacing.lg)
            .padding(.bottom, HopSpacing.xl)
            .background(
                // Fade from transparent to surface so slide content flows into CTA chrome
                LinearGradient(
                    colors: [Color.hopSurface.opacity(0), Color.hopSurface, Color.hopSurface],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea(edges: .bottom)
            )
        }
        .background(Color.hopSurface.ignoresSafeArea())
    }
}

// MARK: - OnboardingSlideView

private struct OnboardingSlideView: View {
    let slide: OnboardingSlideData

    var body: some View {
        VStack(alignment: .center, spacing: 0) {
            Spacer()

            // ── Illustration placeholder ──────────────────────────────────────
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.hopSurfaceElevated)
                    .frame(width: 280, height: 200)

                Text(slide.illustrationDescription)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                    .multilineTextAlignment(.center)
                    .padding(HopSpacing.md)
            }
            .accessibilityLabel(slide.illustrationDescription)

            Spacer().frame(height: HopSpacing.xl)

            // ── Headline ──────────────────────────────────────────────────────
            Text(slide.headline)
                .font(HopFont.headlineLarge())
                .foregroundColor(Color.hopTextPrimary)
                .multilineTextAlignment(.center)

            Spacer().frame(height: HopSpacing.sm)

            // ── Sub-headline ──────────────────────────────────────────────────
            Text(slide.subheadline)
                .font(HopFont.bodyLarge())
                .foregroundColor(Color.hopTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, HopSpacing.lg)

            // Reserve bottom space so content doesn't underlap the CTA chrome.
            // Approx: dots(20) + spacing(16) + button×2(52+52) + gaps + padding
            Spacer().frame(height: 240)
        }
        .padding(.horizontal, HopSpacing.lg)
    }
}

// MARK: - OnboardingSlideDots

private struct OnboardingSlideDots: View {
    let pageCount: Int
    let currentPage: Int

    var body: some View {
        HStack(spacing: HopSpacing.xs) {
            ForEach(0..<pageCount, id: \.self) { index in
                Capsule()
                    .fill(
                        index == currentPage
                            ? Color.hopPrimaryLime
                            : Color.hopTextSecondary.opacity(0.4)
                    )
                    .frame(
                        width: index == currentPage ? 24 : 8,
                        height: 8
                    )
                    .animation(
                        .spring(response: 0.3, dampingFraction: 0.7),
                        value: currentPage
                    )
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Page \(currentPage + 1) of \(pageCount)")
    }
}

// MARK: - Previews

#Preview("Slide 1") {
    OnboardingView(onNavigateToSignUp: {}, onNavigateToLogin: {})
}

#Preview("Slide 1 — dark") {
    OnboardingView(onNavigateToSignUp: {}, onNavigateToLogin: {})
        .preferredColorScheme(.dark)
}

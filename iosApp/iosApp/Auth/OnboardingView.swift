import SwiftUI

// MARK: - ON-01 Onboarding ───────────────────────────────────────────────────
// Mirrors `OnboardingScreen.kt` 1:1.
// Layout (matches Android Box + alignTopStart):
//   • HopLogo pinned top-leading (status-bar safe via safeAreaInset top)
//   • HorizontalPager fills the screen (TabView)
//   • Bottom chrome: slide dots + Get Started + Log In
// Background is white (HopColors.background) for the auth light theme.

private struct OnboardingSlideData {
    let index: Int
    let headline: String
    let subheadline: String
}

private let onboardingSlides: [OnboardingSlideData] = [
    .init(
        index: 0,
        headline: "Fewer cars.\nBetter Journeys.",
        subheadline: "Denmark's carpooling platform.\nShare a ride, shrink your footprint"
    ),
    .init(
        index: 1,
        headline: "Every seat\nfilled matters.",
        subheadline: "One shared trip can cut CO\u{2082}\nemissions in half. Small change,\nbig difference."
    ),
    .init(
        index: 2,
        headline: "Move together.\nLive lighter.",
        subheadline: "Join thousands of Danes choosing\nsmarter, greener travel\none ride at a time."
    ),
]

struct OnboardingView: View {
    var onNavigateToSignUp: () -> Void
    var onNavigateToLogin:  () -> Void

    @State private var currentPage = 0
    @State private var chromeVisible = false

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            TabView(selection: $currentPage) {
                ForEach(onboardingSlides, id: \.index) { slide in
                    OnboardingSlideView(slide: slide)
                        .tag(slide.index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .safeAreaInset(edge: .top, alignment: .leading, spacing: 0) {
            HopLogo()
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.sm)
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            VStack(spacing: HopSpacing.md) {
                OnboardingSlideDots(pageCount: onboardingSlides.count, currentPage: currentPage)
                Spacer().frame(height: HopSpacing.sm)
                HopButton(text: "Get Started", variant: .primary, action: onNavigateToSignUp)
                HopButton(text: "Log In", variant: .ghost, lightSurface: true, action: onNavigateToLogin)
            }
            .padding(.horizontal, HopSpacing.lg)
            .padding(.bottom, HopSpacing.lg)
            .opacity(chromeVisible ? 1 : 0)
            .offset(y: chromeVisible ? 0 : 24)
            .background(Color.hopBackground.ignoresSafeArea(edges: .bottom))
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.5)) { chromeVisible = true }
        }
    }
}

private struct OnboardingSlideView: View {
    let slide: OnboardingSlideData

    @State private var visible = false

    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 0)

            OnboardingIllustration(index: slide.index)
                .frame(width: 280, height: 200)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .scaleEffect(visible ? 1 : 0.85)
                .opacity(visible ? 1 : 0)

            Spacer().frame(height: HopSpacing.xl)

            Text(slide.headline)
                .font(HopFont.headlineLarge())
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)

            Spacer().frame(height: HopSpacing.sm)

            Text(slide.subheadline)
                .font(HopFont.bodyLarge())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, HopSpacing.lg)

            Spacer(minLength: 0)
        }
        .padding(.horizontal, HopSpacing.lg)
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                visible = true
            }
        }
    }
}

private struct OnboardingSlideDots: View {
    let pageCount: Int
    let currentPage: Int

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            ForEach(0..<pageCount, id: \.self) { index in
                let active = index == currentPage
                Capsule()
                    .fill(active ? Color.hopAuthAccent : Color.hopAuthTextSecondary.opacity(0.35))
                    .frame(width: active ? 24 : 8, height: 8)
                    .animation(.spring(response: 0.4, dampingFraction: 0.75), value: currentPage)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Slide \(currentPage + 1) of \(pageCount)")
    }
}

#Preview {
    OnboardingView(onNavigateToSignUp: {}, onNavigateToLogin: {})
}

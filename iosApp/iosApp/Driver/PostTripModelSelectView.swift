import SwiftUI
import Shared

// MARK: — DR-05 Post Trip — Model Select ──────────────────────────────────────

struct PostTripModelSelectView: View {

    var onNavigateToModelA: () -> Void
    var onNavigateToModelB: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = DriverViewModelWrapper()

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: HopSpacing.md) {
                Text("Post a trip")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopTextPrimary)

                Text("Choose the type of trip you want to offer.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)

                Spacer().frame(height: HopSpacing.md)

                ModelCard(
                    title: "Daily Commute",
                    subtitle: "Same route, repeating weekly",
                    icon: "repeat",
                    badge: "Model A",
                    action: { wrapper.selectModelA() }
                )

                ModelCard(
                    title: "One-off Long Distance",
                    subtitle: "Single trip with a minimum-passengers threshold",
                    icon: "calendar",
                    badge: "Model B",
                    action: { wrapper.selectModelB() }
                )

                Spacer()
            }
            .padding(HopSpacing.md)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Post Trip").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is DriverEffectNavigateToModelAForm: onNavigateToModelA()
                case is DriverEffectNavigateToModelBForm: onNavigateToModelB()
                default: break
                }
            }
        }
    }
}

private struct ModelCard: View {
    let title: String
    let subtitle: String
    let icon: String
    let badge: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(alignment: .top, spacing: HopSpacing.sm) {
                Image(systemName: icon)
                    .font(.system(size: 28))
                    .foregroundColor(Color.hopPrimaryLime)
                    .frame(width: 56, height: 56)
                    .background(Color.hopPrimaryLime.opacity(0.15))
                    .clipShape(RoundedRectangle(cornerRadius: 14))

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(title)
                            .font(HopFont.labelLarge(weight: .semibold))
                            .foregroundColor(Color.hopTextPrimary)
                        Spacer()
                        Text(badge)
                            .font(HopFont.bodySmall(weight: .semibold))
                            .foregroundColor(Color.hopSurface)
                            .padding(.horizontal, HopSpacing.xs)
                            .padding(.vertical, 2)
                            .background(Color.hopPrimaryLime)
                            .clipShape(Capsule())
                    }
                    Text(subtitle)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                }
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }
}

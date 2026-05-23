import SwiftUI
import Shared

// MARK: — DR-05 Post Trip — Model Select ──────────────────────────────────────

struct PostTripModelSelectView: View {

    var onNavigateToModelA: () -> Void
    var onNavigateToModelB: () -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: HopSpacing.md) {
                Text("Post a trip")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)

                Text("Choose the type of trip you want to offer.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)

                Spacer().frame(height: HopSpacing.md)

                ModelCard(
                    title: "Daily Commute",
                    icon: "repeat",
                    badge: "Model A",
                    bullets: [
                        "Same route, repeating weekly",
                        "Pick the days you drive",
                        "Passengers book per ride",
                        "Best for commuters"
                    ],
                    action: { wrapper.selectModelA() }
                )

                ModelCard(
                    title: "One-off Long Distance",
                    icon: "calendar",
                    badge: "Model B",
                    bullets: [
                        "Single trip on a chosen date",
                        "Set a minimum passengers threshold",
                        "Trip auto-cancels if not met",
                        "Best for inter-city journeys"
                    ],
                    action: { wrapper.selectModelB() }
                )

                Spacer()
            }
            .padding(HopSpacing.md)
        }
        .task {
            for await effect in wrapper.effects {
                switch effect {
                case is DriverEffectNavigateToModelAForm: onNavigateToModelA()
                case is DriverEffectNavigateToModelBForm: onNavigateToModelB()
                default: break
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Post a trip", onBack: onBack)
            .background(Color.hopBackground)
    }
    }
}

private struct ModelCard: View {
    let title: String
    let icon: String
    let badge: String
    let bullets: [String]
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: HopSpacing.sm) {
                HStack(alignment: .top, spacing: HopSpacing.sm) {
                    Image(systemName: icon)
                        .font(.system(size: 28))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 56, height: 56)
                        .background(Color.hopPrimaryLime.opacity(0.25))
                        .clipShape(RoundedRectangle(cornerRadius: 14))

                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(title)
                                .font(HopFont.labelLarge(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                            Spacer()
                            Text(badge)
                                .font(HopFont.bodySmall(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .padding(.horizontal, HopSpacing.xs)
                                .padding(.vertical, 2)
                                .background(Color.hopPrimaryLime)
                                .clipShape(Capsule())
                        }
                        Text(bullets.first ?? "")
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }
                }

                Divider().background(Color.hopCardBorder)

                VStack(alignment: .leading, spacing: 6) {
                    ForEach(Array(bullets.dropFirst()), id: \.self) { item in
                        HStack(alignment: .top, spacing: HopSpacing.xs) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 14))
                                .foregroundColor(Color.hopPrimaryGreen)
                            Text(item)
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextPrimary)
                        }
                    }
                }

                HStack {
                    Spacer()
                    HStack(spacing: 4) {
                        Text("Choose")
                            .font(HopFont.labelMedium(weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Image(systemName: "chevron.right")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                    }
                }
            }
            .padding(HopSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.hopCardSurface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.hopCardBorder, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

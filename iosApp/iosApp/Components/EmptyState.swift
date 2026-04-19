import SwiftUI

// MARK: - EmptyState

/// Full-screen (or section-filling) empty / zero-data view.
///
/// Usage:
/// ```swift
/// EmptyState(
///     systemImage: "car.2",
///     headline: "No trips yet",
///     subtitle: "Post your first trip and start earning.",
///     ctaLabel: "Post a trip",
///     ctaAction: { navigateToPostTrip() }
/// )
/// ```
struct EmptyState: View {
    /// SF Symbol name for the illustration placeholder.
    let systemImage: String
    let headline: String
    var subtitle: String? = nil
    /// Primary CTA button label. Omit to hide the button.
    var ctaLabel: String? = nil
    var ctaAction: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: HopSpacing.lg) {
            Spacer(minLength: 0)

            // ── Illustration placeholder ───────────────────────────────────────
            ZStack {
                Circle()
                    .fill(Color.hopSurfaceElevated)
                    .frame(width: 120, height: 120)

                Image(systemName: systemImage)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 52, height: 52)
                    .foregroundColor(Color.hopTextSecondary)
            }

            // ── Text block ────────────────────────────────────────────────────
            VStack(spacing: HopSpacing.xs) {
                Text(headline)
                    .font(HopFont.headlineSmall())
                    .fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
                    .multilineTextAlignment(.center)

                if let subtitle {
                    Text(subtitle)
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.xl)
                }
            }

            // ── CTA ───────────────────────────────────────────────────────────
            if let ctaLabel, let ctaAction {
                HopButton(
                    text: ctaLabel,
                    variant: .primary,
                    action: ctaAction
                )
                .frame(maxWidth: 240)
                .padding(.top, HopSpacing.xs)
            }

            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopSurface)
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Previews

#Preview("EmptyState — With CTA") {
    EmptyState(
        systemImage: "car.2",
        headline: "No trips yet",
        subtitle: "Post your first trip and start earning with Hop.",
        ctaLabel: "Post a trip",
        ctaAction: {}
    )
}

#Preview("EmptyState — No CTA") {
    EmptyState(
        systemImage: "magnifyingglass",
        headline: "No results found",
        subtitle: "Try a different route or departure date."
    )
}

#Preview("EmptyState — Bookings") {
    EmptyState(
        systemImage: "ticket",
        headline: "No bookings yet",
        subtitle: "Find a trip and book your first seat.",
        ctaLabel: "Find a trip",
        ctaAction: {}
    )
}

#Preview("EmptyState — Messages") {
    EmptyState(
        systemImage: "bubble.left.and.bubble.right",
        headline: "No messages",
        subtitle: "Messages from drivers and passengers will appear here."
    )
}

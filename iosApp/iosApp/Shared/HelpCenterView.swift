import SwiftUI
import Shared

// MARK: — SH-09 Help Centre ───────────────────────────────────────────────────
//
// Dark-themed full-support hub. Mirrors HelpCenterScreen.kt.
// Sections: Search · How It Works (Passenger / Driver tabs) · FAQ accordion
//           (grouped by topic, filtered by search) · Report a Problem · Contact Us.

struct HelpCenterView: View {

    var onBack: () -> Void

    @StateObject private var wrapper = HelpCenterViewModelWrapper()
    @Environment(\.openURL) private var openURL

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {

                // ── Top bar ───────────────────────────────────────────────
                HelpCenterTopBar(onBack: onBack)

                ScrollView(showsIndicators: false) {
                    LazyVStack(alignment: .leading, spacing: 0, pinnedViews: []) {

                        // ── Search bar ────────────────────────────────────
                        HelpSearchBar(
                            query: Binding(
                                get: { wrapper.state.searchQuery },
                                set: { wrapper.searchChanged($0) }
                            )
                        )
                        .padding(.horizontal, HopSpacing.md)
                        .padding(.vertical, HopSpacing.sm)

                        // ── How It Works (hidden during search) ──────────
                        if wrapper.state.searchQuery.isEmpty {
                            SectionLabel(title: "How It Works")

                            HowItWorksTabs(
                                activeTab: wrapper.state.activeTab,
                                onSelect: { wrapper.selectTab($0) }
                            )
                            .padding(.horizontal, HopSpacing.md)

                            Spacer().frame(height: HopSpacing.sm)

                            HowItWorksSteps(
                                steps: wrapper.state.activeTab == HelpCenterTab.passenger
                                    ? passengerSteps : driverSteps
                            )
                            .padding(.horizontal, HopSpacing.md)

                            Spacer().frame(height: HopSpacing.lg)
                        }

                        // ── FAQ ───────────────────────────────────────────
                        SectionLabel(title: "Frequently Asked Questions")

                        if wrapper.state.isLoading {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                                Spacer()
                            }
                            .padding(.vertical, HopSpacing.xl)

                        } else if let error = wrapper.state.error {
                            ErrorRetryView(message: error, onRetry: wrapper.retry)
                                .padding(.horizontal, HopSpacing.md)
                                .padding(.vertical, HopSpacing.sm)

                        } else if wrapper.state.filteredFaqs.isEmpty {
                            Text("No results for \"\(wrapper.state.searchQuery)\"")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopTextSecondary)
                                .padding(.horizontal, HopSpacing.md)
                                .padding(.vertical, HopSpacing.lg)

                        } else {
                            let grouped = Dictionary(
                                grouping: wrapper.state.filteredFaqs,
                                by: { $0.topic }
                            )
                            // Maintain consistent topic order
                            let topicOrder = ["Bookings", "Payments & pricing", "Driver onboarding",
                                              "Account & profile", "Safety & trust", "App & technical issues"]
                            let sortedTopics = topicOrder.filter { grouped[$0] != nil } +
                                grouped.keys.filter { !topicOrder.contains($0) }.sorted()

                            ForEach(sortedTopics, id: \.self) { topic in
                                if let faqs = grouped[topic] {
                                    FaqTopicHeader(topic: topic)
                                    ForEach(faqs, id: \.id) { faq in
                                        FaqRowView(
                                            faq: faq,
                                            expanded: wrapper.state.expandedFaqId == faq.id,
                                            onToggle: { wrapper.toggleFaq(faq.id) }
                                        )
                                        .padding(.horizontal, HopSpacing.md)
                                        .padding(.bottom, 6)
                                    }
                                    Spacer().frame(height: HopSpacing.sm)
                                }
                            }
                        }

                        // ── Support actions ───────────────────────────────
                        Spacer().frame(height: HopSpacing.lg)
                        SectionLabel(title: "Support")

                        SupportActionRow(
                            systemIcon: "ladybug",
                            label: "Report a problem",
                            description: "Tell us about a bug or unexpected behaviour",
                            action: wrapper.reportProblem
                        )

                        Divider()
                            .background(Color.hopSurfaceElevated)
                            .padding(.horizontal, HopSpacing.md)

                        SupportActionRow(
                            systemIcon: "envelope",
                            label: "Contact us",
                            description: "support@ridly.dk",
                            action: wrapper.contactUs
                        )

                        Spacer().frame(height: HopSpacing.xxl)
                    }
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case is HelpCenterEffectNavigateBack:
                    onBack()
                case let e as HelpCenterEffectOpenUri:
                    if let url = URL(string: e.uri) { openURL(url) }
                default:
                    break
                }
            }
        }
    }
}

// MARK: — Top bar ─────────────────────────────────────────────────────────────

private struct HelpCenterTopBar: View {
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopTextPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Navigate back")

            Text("Help Centre")
                .font(HopFont.bodyLarge(weight: .bold))
                .foregroundColor(Color.hopTextPrimary)

            Spacer()
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopSurface)
    }
}

// MARK: — Search bar ──────────────────────────────────────────────────────────

private struct HelpSearchBar: View {
    @Binding var query: String

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 16))
                .foregroundColor(Color.hopTextSecondary)

            TextField("", text: $query)
                .foregroundColor(Color.hopTextPrimary)
                .font(HopFont.bodyMedium())
                .placeholder(when: query.isEmpty) {
                    Text("Search help articles…")
                        .foregroundColor(Color.hopTextSecondary)
                        .font(HopFont.bodyMedium())
                }
                .tint(Color.hopPrimaryLime)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, 12)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: — Section label ───────────────────────────────────────────────────────

private struct SectionLabel: View {
    let title: String

    var body: some View {
        Text(title.uppercased())
            .font(.system(size: 11, weight: .semibold))
            .tracking(0.8)
            .foregroundColor(Color.hopTextSecondary)
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.md)
            .padding(.bottom, HopSpacing.sm)
    }
}

// MARK: — How It Works tabs ───────────────────────────────────────────────────

private struct HowItWorksTabs: View {
    let activeTab: HelpCenterTab
    let onSelect: (HelpCenterTab) -> Void

    var body: some View {
        HStack(spacing: 0) {
            HowItWorksTabButton(
                systemIcon: "person",
                label: "Passenger",
                selected: activeTab == HelpCenterTab.passenger,
                action: { onSelect(HelpCenterTab.passenger) }
            )
            HowItWorksTabButton(
                systemIcon: "car",
                label: "Driver",
                selected: activeTab == HelpCenterTab.driver,
                action: { onSelect(HelpCenterTab.driver) }
            )
        }
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

private struct HowItWorksTabButton: View {
    let systemIcon: String
    let label: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: systemIcon)
                    .font(.system(size: 14, weight: .regular))
                Text(label)
                    .font(.system(size: 14, weight: .semibold))
            }
            .foregroundColor(selected ? Color.hopSurface : Color.hopTextSecondary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
        }
        .background(selected ? Color.hopPrimaryLime : Color.clear)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .buttonStyle(.plain)
    }
}

// MARK: — How It Works steps ──────────────────────────────────────────────────

private struct HowItWorksStep {
    let number: Int
    let title: String
    let description: String
}

private let passengerSteps = [
    HowItWorksStep(number: 1, title: "Search for a trip",
                   description: "Enter your origin, destination, and travel date to see available rides."),
    HowItWorksStep(number: 2, title: "Pick your seat",
                   description: "Browse trips, check the driver's profile and ratings, then book a seat."),
    HowItWorksStep(number: 3, title: "Ride & settle",
                   description: "Meet the driver at the pickup point. After the trip, pay via MobilePay."),
    HowItWorksStep(number: 4, title: "Leave a rating",
                   description: "Rate your driver so others can trust the community too."),
]

private let driverSteps = [
    HowItWorksStep(number: 1, title: "Set up your profile",
                   description: "Add your car details and MobilePay number to get started."),
    HowItWorksStep(number: 2, title: "Post a trip",
                   description: "Enter your route, departure time, seats, and price before you leave."),
    HowItWorksStep(number: 3, title: "Accept bookings",
                   description: "Review passenger profiles and confirm the riders you are happy to take."),
    HowItWorksStep(number: 4, title: "Collect payment",
                   description: "Send a MobilePay request after the trip and get paid directly."),
]

private struct HowItWorksSteps: View {
    let steps: [HowItWorksStep]

    var body: some View {
        VStack(spacing: HopSpacing.sm) {
            ForEach(steps, id: \.number) { step in
                HStack(alignment: .top, spacing: HopSpacing.md) {
                    Text("\(step.number)")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(Color.hopSurface)
                        .frame(width: 28, height: 28)
                        .background(Color.hopPrimaryLime)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                    VStack(alignment: .leading, spacing: 3) {
                        Text(step.title)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color.hopTextPrimary)
                        Text(step.description)
                            .font(.system(size: 13))
                            .foregroundColor(Color.hopTextSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                            .lineSpacing(4)
                    }
                    Spacer()
                }
                .padding(HopSpacing.md)
                .background(Color.hopSurfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }
}

// MARK: — FAQ ─────────────────────────────────────────────────────────────────

private struct FaqTopicHeader: View {
    let topic: String

    var body: some View {
        Text(topic)
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(Color.hopPrimaryLime)
            .padding(.horizontal, HopSpacing.md)
            .padding(.top, HopSpacing.sm)
            .padding(.bottom, 4)
    }
}

private struct FaqRowView: View {
    let faq: FaqItem
    let expanded: Bool
    let onToggle: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .top, spacing: HopSpacing.sm) {
                    Text(faq.question)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color.hopTextPrimary)
                        .multilineTextAlignment(.leading)
                        .fixedSize(horizontal: false, vertical: true)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Image(systemName: expanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.top, 2)
                }
                .padding(HopSpacing.md)
            }
            .buttonStyle(.plain)

            if expanded {
                Divider()
                    .background(Color.hopSurface)
                    .padding(.horizontal, HopSpacing.md)

                Text(faq.answer)
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopTextSecondary)
                    .lineSpacing(5)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.top, HopSpacing.sm)
                    .padding(.bottom, HopSpacing.md)
            }
        }
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .animation(.easeInOut(duration: 0.2), value: expanded)
    }
}

// MARK: — Support action row ──────────────────────────────────────────────────

private struct SupportActionRow: View {
    let systemIcon: String
    let label: String
    let description: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: HopSpacing.md) {
                Image(systemName: systemIcon)
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopPrimaryLime)
                    .frame(width: 40, height: 40)
                    .background(Color.hopSurfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color.hopTextPrimary)
                    Text(description)
                        .font(.system(size: 12))
                        .foregroundColor(Color.hopTextSecondary)
                }
                Spacer()

                Image(systemName: "envelope")
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopTextSecondary)
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

// MARK: — Error / retry ───────────────────────────────────────────────────────

private struct ErrorRetryView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: HopSpacing.sm) {
            Text("Could not load FAQ")
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(Color.hopTextPrimary)
            Text(message)
                .font(.system(size: 13))
                .foregroundColor(Color.hopTextSecondary)
                .multilineTextAlignment(.center)
            Button(action: onRetry) {
                Label("Try again", systemImage: "arrow.clockwise")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color.hopSurface)
                    .padding(.horizontal, HopSpacing.lg)
                    .padding(.vertical, HopSpacing.sm)
                    .background(Color.hopPrimaryLime)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(HopSpacing.lg)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: — Placeholder extension ───────────────────────────────────────────────

private extension View {
    @ViewBuilder
    func placeholder<Content: View>(
        when shouldShow: Bool,
        @ViewBuilder placeholder: () -> Content
    ) -> some View {
        ZStack(alignment: .leading) {
            if shouldShow { placeholder() }
            self
        }
    }
}

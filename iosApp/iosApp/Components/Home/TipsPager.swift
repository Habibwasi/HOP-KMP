import SwiftUI

struct HomeTip: Identifiable {
    let id = UUID()
    let title: String
    let body: String
}

let DefaultHomeTips: [HomeTip] = [
    HomeTip(
        title: "Save your favourite places",
        body: "Tap Add above the search bar to one-tap your daily destinations."
    ),
    HomeTip(
        title: "Travel greener with Hop",
        body: "Carpooling 100 km saves about 12 kg of CO₂ per seat."
    ),
]

/// PA-01 — Auto-advancing horizontal pager. Loops every 5 s.
/// Mirrors composeApp `TipsPager.kt`.
struct TipsPager: View {
    let tips: [HomeTip]
    var autoAdvanceSeconds: TimeInterval = 5

    @State private var current: Int = 0

    var body: some View {
        if tips.isEmpty { EmptyView() } else {
            VStack(spacing: HopSpacing.sm) {
                TabView(selection: $current) {
                    ForEach(Array(tips.enumerated()), id: \.element.id) { index, tip in
                        TipCard(tip: tip).tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .frame(height: 96)

                HStack(spacing: 6) {
                    ForEach(0..<tips.count, id: \.self) { i in
                        Circle()
                            .fill(i == current ? Color.hopPrimaryLime : Color.hopCardBorder)
                            .frame(width: i == current ? 8 : 6, height: i == current ? 8 : 6)
                    }
                }
            }
            .onReceive(Timer.publish(every: autoAdvanceSeconds, on: .main, in: .common).autoconnect()) { _ in
                guard tips.count > 1 else { return }
                withAnimation(.easeInOut) {
                    current = (current + 1) % tips.count
                }
            }
        }
    }
}

private struct TipCard: View {
    let tip: HomeTip

    var body: some View {
        ZStack(alignment: .leading) {
            LinearGradient(
                colors: [Color.hopGradientDayStart.opacity(0.9), Color.white],
                startPoint: .leading,
                endPoint: .trailing
            )
            VStack(alignment: .leading, spacing: 4) {
                Text(tip.title)
                    .font(HopFont.titleSmall())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Text(tip.body)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .multilineTextAlignment(.leading)
            }
            .padding(HopSpacing.md)
        }
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

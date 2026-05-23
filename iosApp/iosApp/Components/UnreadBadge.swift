import SwiftUI

// MARK: - UnreadBadge

/// Small red counter badge for notifications / chat unread totals.
/// Renders nothing when `count` is 0.
///
/// Mirrors `UnreadBadge` in composeApp.
struct UnreadBadge: View {
    let count: Int

    var body: some View {
        if count > 0 {
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(.white)
                .padding(.horizontal, count > 9 ? 5 : 0)
                .frame(minWidth: 16, minHeight: 16)
                .background(Color.hopError)
                .clipShape(Capsule())
                .accessibilityLabel("\(count) unread")
        }
    }

    private var label: String {
        count > 99 ? "99+" : "\(count)"
    }
}

// MARK: - Previews

#Preview("UnreadBadge") {
    HStack(spacing: 16) {
        UnreadBadge(count: 1)
        UnreadBadge(count: 9)
        UnreadBadge(count: 12)
        UnreadBadge(count: 250)
    }
    .padding()
    .background(Color.hopSurface)
}

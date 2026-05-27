import SwiftUI
import Shared

// MARK: — SH-04b Chat List ────────────────────────────────────────────────────
// Shows all chats the user has had or is currently having.

struct ChatListView: View {

    var onBack: () -> Void
    var onNavigateToChat: (String) -> Void

    @StateObject private var wrapper = ChatListViewModelWrapper()

    var body: some View {
        VStack(spacing: 0) {
            chatListTopBar

            if wrapper.state.isLoading {
                Spacer()
                ProgressView()
                    .tint(Color.hopAuthAccent)
                Spacer()
            } else if let _ = wrapper.state.error {
                Spacer()
                Text("Could not load chats.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
            } else if wrapper.state.threads.isEmpty {
                Spacer()
                VStack(spacing: HopSpacing.sm) {
                    Image(systemName: "bubble.left.and.bubble.right")
                        .font(.system(size: 48))
                        .foregroundColor(Color.hopAuthTextSecondary)
                    Text("No chats yet.\nBook a trip to start chatting!")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .multilineTextAlignment(.center)
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(wrapper.state.threads, id: \.bookingId) { thread in
                            ChatThreadRow(thread: thread) {
                                onNavigateToChat(thread.bookingId)
                            }
                            Divider()
                                .background(Color.hopAuthInputBorder)
                                .padding(.leading, HopSpacing.md + 44 + HopSpacing.md)
                        }
                    }
                }
            }
        }
        .background(Color.hopBackground.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
        .task {
            wrapper.startObserving { _ in }
            wrapper.load()
        }
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private var chatListTopBar: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 44, height: 44)
                }
                Text("My Chats")
                    .font(HopFont.bodyLarge(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.xs)
            .frame(height: 56)

            Divider().background(Color.hopAuthInputBorder)
        }
        .background(Color.hopBackground)
    }
}

// MARK: — Thread row ───────────────────────────────────────────────────────────

private struct ChatThreadRow: View {
    let thread: ChatThread
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: HopSpacing.md) {
                // Avatar placeholder
                ZStack {
                    Circle()
                        .fill(Color.hopAuthAccent.opacity(0.12))
                        .frame(width: 44, height: 44)
                    Image(systemName: "bubble.left.and.bubble.right")
                        .font(.system(size: 18))
                        .foregroundColor(Color.hopAuthAccent)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(thread.otherPartyName)
                        .font(HopFont.bodyMedium(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .lineLimit(1)
                    Text("\(thread.tripOrigin) → \(thread.tripDest)")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .lineLimit(1)
                    Text(formatDeparture(thread.departureAt))
                        .font(.system(size: 11))
                        .foregroundColor(Color.hopAuthTextSecondary)
                }

                Spacer()

                // Role badge
                Text(thread.myRole == "DRIVER" ? "Driver" : "Passenger")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(thread.myRole == "DRIVER" ? Color.hopPrimaryGreen : Color.hopAuthAccent)
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, 3)
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .fill(thread.myRole == "DRIVER"
                                  ? Color.hopPrimaryGreen.opacity(0.15)
                                  : Color.hopAuthAccent.opacity(0.12))
                    )
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.md)
        }
        .buttonStyle(.plain)
    }

    private func formatDeparture(_ iso: String) -> String {
        let fmt = ISO8601DateFormatter()
        guard let date = fmt.date(from: iso) else { return iso }
        let cal = Calendar.current
        let now = Date()
        let df = DateFormatter()
        if cal.isDateInToday(date) {
            df.dateFormat = "'Today · 'HH:mm"
        } else if cal.isDateInTomorrow(date) {
            df.dateFormat = "'Tomorrow · 'HH:mm"
        } else {
            df.dateFormat = "d MMM · HH:mm"
        }
        df.locale = Locale(identifier: "en_GB")
        let _ = now
        return df.string(from: date)
    }
}

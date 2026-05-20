import SwiftUI
import Shared

// MARK: — SH-04 Chat (per booking) ────────────────────────────────────────────
// Mirrors `ChatScreen.kt`.

struct ChatView: View {

    let bookingId: String
    var onBack: () -> Void

    @StateObject private var wrapper     = ChatViewModelWrapper()
    @StateObject private var authWrapper  = AuthViewModelWrapper()
    @State private var inputText: String = ""

    private var myUserId: String { authWrapper.state.currentUser?.id ?? "" }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {
                // ── Custom top bar (replaces .toolbar which is hidden by HopNavigationStack) ──
                chatTopBar

                connectionBanner

                // ── Message list ─────────────────────────────────────────────
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: HopSpacing.xs) {
                            ForEach(wrapper.state.messages, id: \.id) { msg in
                                MessageBubble(message: msg, isMine: msg.senderId == myUserId)
                                    .id(msg.id)
                            }
                        }
                        .padding(HopSpacing.md)
                    }
                    .onChange(of: wrapper.state.messages.count) { _, _ in
                        if let last = wrapper.state.messages.last {
                            withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                        }
                    }
                }

                // ── Composer ─────────────────────────────────────────────────
                HStack(spacing: HopSpacing.sm) {
                    TextField("Type a message…", text: $inputText, axis: .vertical)
                        .lineLimit(1...4)
                        .padding(HopSpacing.sm)
                        .background(Color.hopSurfaceElevated)
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .foregroundColor(Color.hopTextPrimary)
                        .onChange(of: inputText) { _, new in wrapper.inputChanged(new) }

                    Button {
                        wrapper.send()
                        inputText = ""
                    } label: {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(Color.hopSurface)
                            .padding(HopSpacing.sm)
                            .background(inputText.trimmingCharacters(in: .whitespaces).isEmpty
                                        ? Color.hopSurfaceElevated : Color.hopPrimaryLime)
                            .clipShape(Circle())
                    }
                    .disabled(inputText.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(HopSpacing.sm)
                .background(Color.hopSurfaceElevated)
            }
        }
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { _ in }
            authWrapper.startObserving()
            let token = KoinIOSKt.getAccessToken() ?? ""
            wrapper.connect(bookingId: bookingId, token: token)
        }
        .onDisappear { wrapper.disconnect() }
        // The rest of the app is light-themed; chat uses a dark surface.
        // Override here so system controls (TextField, keyboard) render
        // correctly against the dark background.
        .preferredColorScheme(.dark)
    }

    // ── Custom top bar ────────────────────────────────────────────────────────

    private var chatTopBar: some View {
        VStack(spacing: 0) {
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(Color.hopTextPrimary)
                        .frame(width: 44, height: 44)
                }

                Spacer(minLength: 0)

                Text("Chat")
                    .font(HopFont.bodyLarge(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)

                Spacer(minLength: 0)

                // Connection dot — right side, mirrors Android ConnectionIndicator
                connectionDot
                    .frame(width: 44, height: 44, alignment: .center)
            }
            .padding(.horizontal, HopSpacing.xs)
            .frame(height: 56)
            .background(Color.hopSurfaceElevated)

            Divider().background(Color.hopSurface)
        }
        .padding(.top, safeAreaTopPadding)
        .background(Color.hopSurfaceElevated.ignoresSafeArea(edges: .top))
    }

    @ViewBuilder
    private var connectionDot: some View {
        switch wrapper.state.connectionState {
        case is ConnectionStateConnected:
            Circle().fill(Color.hopSuccess).frame(width: 8, height: 8)
        case is ConnectionStateConnecting:
            Circle().fill(Color.hopWarning).frame(width: 8, height: 8)
        default:
            Circle().fill(Color.hopTextSecondary).frame(width: 8, height: 8)
        }
    }

    @ViewBuilder
    private var connectionBanner: some View {
        switch wrapper.state.connectionState {
        case is ConnectionStateConnected:
            EmptyView()
        case is ConnectionStateConnecting:
            banner(text: "Connecting…", color: Color.hopWarning)
        case let err as ConnectionStateError:
            banner(text: "Connection error: \(err.message)", color: Color.hopError)
        default:
            banner(text: "Disconnected", color: Color.hopTextSecondary)
        }
    }

    private func banner(text: String, color: Color) -> some View {
        HStack {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(text).font(HopFont.bodySmall()).foregroundColor(Color.hopTextSecondary)
            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopSurfaceElevated)
    }

    private var safeAreaTopPadding: CGFloat {
        (UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first?.safeAreaInsets.top) ?? 0
    }
}

// MARK: — Message bubble ───────────────────────────────────────────────────────

private struct MessageBubble: View {
    let message: Message
    let isMine: Bool

    var body: some View {
        HStack(alignment: .bottom, spacing: 0) {
            if isMine { Spacer(minLength: 40) }

            VStack(alignment: isMine ? .trailing : .leading, spacing: 2) {
                if !isMine {
                    Text(message.senderName)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                        .padding(.horizontal, HopSpacing.xs)
                }

                Text(message.body)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(isMine ? Color(hex: 0x1A1A1A) : Color.hopTextPrimary)
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)
                    .background(isMine ? Color.hopPrimaryLime : Color.hopSurfaceElevated)
                    .clipShape(
                        isMine
                        ? UnevenRoundedRectangle(topLeadingRadius: 16, bottomLeadingRadius: 16,
                                                 bottomTrailingRadius: 16, topTrailingRadius: 4)
                        : UnevenRoundedRectangle(topLeadingRadius: 4, bottomLeadingRadius: 16,
                                                 bottomTrailingRadius: 16, topTrailingRadius: 16)
                    )

                Text(formatTimestamp(message.timestampMs))
                    .font(HopFont.bodySmall())
                    .foregroundColor(isMine ? Color(hex: 0x1A1A1A, opacity: 0.6) : Color.hopTextSecondary)
                    .padding(.horizontal, HopSpacing.xs)
            }

            if !isMine { Spacer(minLength: 40) }
        }
    }

    private func formatTimestamp(_ epochMs: Int64) -> String {
        let totalMinutes = epochMs / 60_000
        let hours   = (totalMinutes / 60) % 24
        let minutes = totalMinutes % 60
        return String(format: "%02d:%02d", hours, minutes)
    }
}

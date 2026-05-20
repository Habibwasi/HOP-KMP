import SwiftUI
import Shared

// MARK: — SH-04 Chat (per booking) ────────────────────────────────────────────
// Mirrors `ChatScreen.kt`.

struct ChatView: View {

    let bookingId: String
    var onBack: () -> Void

    @StateObject private var wrapper     = ChatViewModelWrapper()
    @StateObject private var authWrapper = AuthViewModelWrapper()
    @State private var inputText: String = ""

    private var myUserId: String { authWrapper.state.currentUser?.id ?? "" }

    var body: some View {
        VStack(spacing: 0) {
            chatTopBar

            connectionBanner

            // ── Message list ──────────────────────────────────────────────────
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

            // ── Composer ──────────────────────────────────────────────────────
            composerBar
        }
        .background(Color.hopSurface.ignoresSafeArea())
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { _ in }
            authWrapper.startObserving()
            let token = KoinIOSKt.getAccessToken() ?? ""
            wrapper.connect(bookingId: bookingId, token: token)
        }
        .onDisappear { wrapper.disconnect() }
        .preferredColorScheme(.dark)
    }

    // ── Custom top bar ────────────────────────────────────────────────────────

    private var chatTopBar: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 44, height: 44)
                }
                Spacer()
                Text("Chat")
                    .font(HopFont.bodyLarge(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
                connectionDot
                    .frame(width: 44, height: 44, alignment: .center)
            }
            .padding(.horizontal, HopSpacing.xs)
            .frame(height: 56)

            Divider().background(Color.hopAuthInputBorder)
        }
        .background(Color.hopBackground)
    }

    // ── Composer bar ──────────────────────────────────────────────────────────

    private var composerBar: some View {
        let sendEnabled = !inputText.trimmingCharacters(in: .whitespaces).isEmpty
        return HStack(spacing: HopSpacing.sm) {
            TextField("Type a message…", text: $inputText, axis: .vertical)
                .lineLimit(1...4)
                .padding(HopSpacing.sm)
                .background(Color.hopAuthInputSurface)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .foregroundColor(Color.hopAuthTextPrimary)
                .onChange(of: inputText) { _, new in wrapper.inputChanged(new) }

            Button {
                wrapper.send()
                inputText = ""
            } label: {
                Image(systemName: "paperplane.fill")
                    .foregroundColor(sendEnabled ? Color(hex: 0x1A1A1A) : Color.hopAuthTextSecondary)
                    .padding(HopSpacing.sm)
                    .background(sendEnabled ? Color.hopPrimaryLime : Color.hopAuthInputSurface)
                    .clipShape(Circle())
            }
            .disabled(!sendEnabled)
        }
        .padding(HopSpacing.sm)
        .background(Color.hopBackground)
        .overlay(Divider().frame(maxWidth: .infinity, maxHeight: 0.5).background(Color.hopAuthInputBorder), alignment: .top)
    }

    // ── Connection dot ────────────────────────────────────────────────────────

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

    // ── Connection banner ─────────────────────────────────────────────────────

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
            Text(text).font(HopFont.bodySmall()).foregroundColor(Color.hopAuthTextSecondary)
            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopAuthInputSurface)
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
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .padding(.horizontal, HopSpacing.xs)
                }

                Text(message.body)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(isMine ? Color(hex: 0x1A1A1A) : Color.hopAuthTextPrimary)
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.sm)
                    .background(isMine ? Color.hopPrimaryLime : Color.hopAuthInputSurface)
                    .clipShape(
                        isMine
                        ? UnevenRoundedRectangle(topLeadingRadius: 16, bottomLeadingRadius: 16,
                                                 bottomTrailingRadius: 16, topTrailingRadius: 4)
                        : UnevenRoundedRectangle(topLeadingRadius: 4, bottomLeadingRadius: 16,
                                                 bottomTrailingRadius: 16, topTrailingRadius: 16)
                    )

                Text(formatTimestamp(message.timestampMs))
                    .font(.system(size: 11))
                    .foregroundColor(isMine ? Color(hex: 0x1A1A1A, opacity: 0.5) : Color.hopAuthTextSecondary)
                    .padding(.horizontal, HopSpacing.xs)
            }

            if !isMine { Spacer(minLength: 40) }
        }
    }

    private func formatTimestamp(_ epochMs: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(epochMs) / 1000.0)
        let df = DateFormatter()
        df.dateFormat = "HH:mm"
        df.locale = Locale(identifier: "en_GB")
        return df.string(from: date)
    }
}

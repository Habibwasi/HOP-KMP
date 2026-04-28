import SwiftUI
import Shared

// MARK: — SH-04 Chat (per booking) ────────────────────────────────────────────
// Mirrors `ChatScreen.kt`.

struct ChatView: View {

    let bookingId: String
    var onBack: () -> Void

    @StateObject private var wrapper = ChatViewModelWrapper()
    @State private var inputText: String = ""

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {
                connectionBanner

                // ── Message list ─────────────────────────────────────────────
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: HopSpacing.xs) {
                            ForEach(wrapper.state.messages, id: \.id) { msg in
                                MessageBubble(message: msg, isMine: false /* TODO: compare senderId with current user */)
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
                            .background(Color.hopPrimaryLime)
                            .clipShape(Circle())
                    }
                    .disabled(inputText.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(HopSpacing.sm)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Chat").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { _ in }
            // Token resolution would normally happen via auth; supply empty for now.
            wrapper.connect(bookingId: bookingId, token: "")
        }
        .onDisappear { wrapper.disconnect() }
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
}

private struct MessageBubble: View {
    let message: Message
    let isMine: Bool

    var body: some View {
        HStack {
            if isMine { Spacer(minLength: 40) }
            VStack(alignment: .leading, spacing: 2) {
                if !isMine {
                    Text(message.senderName)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                }
                Text(message.body)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(isMine ? Color.hopSurface : Color.hopTextPrimary)
            }
            .padding(HopSpacing.sm)
            .background(isMine ? Color.hopPrimaryLime : Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            if !isMine { Spacer(minLength: 40) }
        }
    }
}

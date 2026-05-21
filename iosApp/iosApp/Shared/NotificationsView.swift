import SwiftUI
import Shared

// MARK: — SH-05 Notifications ─────────────────────────────────────────────────
//
// Mirrors `NotificationsScreen.kt` 1:1.  Light theme, white background,
// Material-style top bar, list of typed notification rows with icon chips,
// unread row tint + lime dot, and an empty state with a lime "Go to search"
// CTA.  Loading state is a centered lime CircularProgressIndicator.

struct NotificationsView: View {

    var navigate: (HopRoute) -> Void
    var onBack:   () -> Void

    @StateObject private var wrapper = NotificationsViewModelWrapper()

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                NotificationsTopBar(onBack: onBack)

                if wrapper.state.isLoading {
                    NotificationsLoadingState()
                } else if wrapper.state.notifications.isEmpty {
                    NotificationsEmptyState(onGoToSearch: wrapper.goToSearchTapped)
                } else {
                    NotificationsList(
                        notifications: wrapper.state.notifications,
                        onTap: { n in
                            if !n.isRead { wrapper.markRead(id: n.id) }
                            routeFor(n)
                        }
                    )
                }
            }
        }
        .navigationBarHidden(true)
        .task {
            wrapper.startObserving { effect in
                if effect is NotificationsEffectNavigateToSearch {
                    navigate(.home)
                }
            }
            wrapper.load()
        }
    }

    private func routeFor(_ n: HopNotification) {
        guard let id = n.deepLinkId else { return }
        switch n.type {
        case NotificationType.bookingConfirmed,
             NotificationType.bookingCancelled,
             NotificationType.tripReminder:
            navigate(.tripDetailActive(bookingId: id))
        case NotificationType.thresholdMet:
            navigate(.tripDetail(id: id))
        case NotificationType.chatMessage:
            navigate(.chat(bookingId: id))
        case NotificationType.paymentMarkedPaid:
            navigate(.driverSettlementByBooking(bookingId: id))
        case NotificationType.paymentConfirmed:
            navigate(.passengerSettlement(bookingId: id))
        default:
            break
        }
    }
}

// MARK: — Top bar

private struct NotificationsTopBar: View {
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 40, height: 40)
            }
            .accessibilityLabel("Navigate back")

            Text("Notifications")
                .font(HopFont.bodyLarge(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            Spacer()
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopBackground)
    }
}

// MARK: — List

private struct NotificationsList: View {
    let notifications: [HopNotification]
    let onTap: (HopNotification) -> Void

    var body: some View {
        ScrollView(showsIndicators: false) {
            LazyVStack(spacing: 0) {
                ForEach(notifications, id: \.id) { n in
                    NotificationRow(notification: n) { onTap(n) }
                }
            }
            .padding(.bottom, HopSpacing.lg)
        }
    }
}

// MARK: — Row

private struct NotificationRow: View {
    let notification: HopNotification
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: HopSpacing.sm) {
                NotificationIconChip(type: notification.type)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(notification.title)
                            .font(.system(size: 14, weight: notification.isRead ? .regular : .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .lineLimit(1)
                        Spacer(minLength: HopSpacing.xs)
                        Text(timeLabel(notification.createdAt))
                            .font(.system(size: 11))
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }

                    Text(notification.body)
                        .font(.system(size: 13))
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }

                if !notification.isRead {
                    Circle()
                        .fill(Color.hopPrimaryLime)
                        .frame(width: 8, height: 8)
                        .padding(.top, 4)
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.sm + 2)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(notification.isRead ? Color.hopBackground : Color.hopAuthInputSurface)
        }
        .buttonStyle(.plain)
    }

    private func timeLabel(_ iso: String) -> String {
        // Extract "HH:mm" from "2026-04-18T10:30:00Z" → "10:30"
        if let tIndex = iso.firstIndex(of: "T") {
            let after = iso.index(after: tIndex)
            return String(iso[after...].prefix(5))
        }
        return iso
    }
}

// MARK: — Icon chip

private struct NotificationIconChip: View {
    let type: NotificationType

    var body: some View {
        let (icon, tint) = iconForType()
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.hopBackground)
                .frame(width: 40, height: 40)
            Image(systemName: icon)
                .font(.system(size: 20, weight: .regular))
                .foregroundColor(tint)
        }
    }

    private func iconForType() -> (String, Color) {
        switch type {
        case NotificationType.bookingConfirmed: return ("checkmark.circle", Color.hopSuccess)
        case NotificationType.bookingCancelled: return ("xmark.circle",     Color.hopError)
        case NotificationType.tripReminder:     return ("clock",             Color.hopWarning)
        case NotificationType.newRating:        return ("star",              Color.hopPrimaryLime)
        case NotificationType.thresholdMet:     return ("checkmark.seal",    Color.hopPrimaryGreen)
        case NotificationType.chatMessage:      return ("bubble.left",       Color.hopAuthTextSecondary)
        default:                                return ("bell.badge",        Color.hopAuthTextSecondary)
        }
    }
}

// MARK: — Empty state

private struct NotificationsEmptyState: View {
    let onGoToSearch: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            ZStack {
                Circle()
                    .fill(Color.hopBackground)
                    .frame(width: 80, height: 80)
                Image(systemName: "bell")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 40, height: 40)
                    .foregroundColor(Color.hopPrimaryLime)
            }

            Spacer().frame(height: HopSpacing.lg)

            Text("You're all caught up")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            Spacer().frame(height: HopSpacing.xs)

            Text("No new notifications right now. Explore upcoming rides and book your next trip.")
                .font(.system(size: 14))
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, HopSpacing.xl)

            Spacer().frame(height: HopSpacing.xl)

            Button(action: onGoToSearch) {
                HStack(spacing: 4) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 16, weight: .semibold))
                    Text("Go to search")
                        .font(.system(size: 15, weight: .semibold))
                }
                .foregroundColor(Color.hopBackground)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(Color.hopPrimaryLime)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .accessibilityLabel("Go to search")
            .padding(.horizontal, HopSpacing.xl)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: — Loading state

private struct NotificationsLoadingState: View {
    var body: some View {
        VStack {
            Spacer()
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                .scaleEffect(1.4)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview("Notifications — items") {
    NotificationsView(navigate: { _ in }, onBack: {})
}

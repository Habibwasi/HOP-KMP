import SwiftUI
import Shared

// MARK: — SH-05 Notifications ─────────────────────────────────────────────────
// Mirrors `NotificationsScreen.kt`.

struct NotificationsView: View {

    var navigate: (HopRoute) -> Void
    var onBack:   () -> Void

    @StateObject private var wrapper = NotificationsViewModelWrapper()

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            if wrapper.state.isLoading && wrapper.state.notifications.isEmpty {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                    .scaleEffect(1.3)
            } else if wrapper.state.notifications.isEmpty {
                EmptyState(
                    systemImage: "bell.slash",
                    headline: "No notifications yet",
                    subtitle: "We'll let you know when something happens.",
                    ctaLabel: "Find a ride",
                    ctaAction: wrapper.goToSearchTapped
                )
            } else {
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: HopSpacing.xs) {
                        ForEach(wrapper.state.notifications, id: \.id) { n in
                            NotificationRow(notification: n) {
                                wrapper.markRead(id: n.id)
                                routeFor(n)
                            }
                        }
                    }
                    .padding(HopSpacing.md)
                }
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
                Text("Notifications").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
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
        default:
            break
        }
    }
}

private struct NotificationRow: View {
    let notification: HopNotification
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: HopSpacing.sm) {
                Image(systemName: iconFor(notification.type))
                    .foregroundColor(Color.hopPrimaryLime)
                    .frame(width: 32, height: 32)
                    .background(Color.hopPrimaryLime.opacity(0.15))
                    .clipShape(Circle())

                VStack(alignment: .leading, spacing: 4) {
                    Text(notification.title)
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                    Text(notification.body)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                        .lineLimit(2)
                }

                Spacer()

                if !notification.isRead {
                    Circle().fill(Color.hopPrimaryLime).frame(width: 8, height: 8)
                }
            }
            .padding(HopSpacing.md)
            .background(notification.isRead ? Color.hopSurfaceElevated : Color.hopSurfaceElevated.opacity(0.85))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(notification.isRead ? Color.clear : Color.hopPrimaryLime.opacity(0.4), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    private func iconFor(_ type: NotificationType) -> String {
        switch type {
        case NotificationType.bookingConfirmed: return "checkmark.circle"
        case NotificationType.bookingCancelled: return "xmark.circle"
        case NotificationType.tripReminder:     return "clock"
        case NotificationType.newRating:        return "star"
        case NotificationType.thresholdMet:     return "person.3"
        case NotificationType.chatMessage:      return "bubble.left"
        default:                                return "bell"
        }
    }
}

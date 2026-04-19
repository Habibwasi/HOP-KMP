import SwiftUI

// MARK: - HopBadgeStatus

/// Unified badge status — covers both booking states and trip model types.
enum HopBadgeStatus {
    case confirmed
    case pending
    case cancelled
    /// Model A — daily commute (recurring)
    case modelA
    /// Model B — one-off long distance (threshold-gated)
    case modelB

    var label: String {
        switch self {
        case .confirmed: return "Confirmed"
        case .pending:   return "Pending"
        case .cancelled: return "Cancelled"
        case .modelA:    return "Commute"
        case .modelB:    return "Long Trip"
        }
    }

    var foregroundColor: Color {
        switch self {
        case .confirmed: return Color.hopSuccess
        case .pending:   return Color.hopWarning
        case .cancelled: return Color.hopError
        case .modelA:    return Color.hopPrimaryLime
        case .modelB:    return Color.hopPrimaryGreen
        }
    }

    var backgroundColor: Color {
        foregroundColor.opacity(0.15)
    }
}

// MARK: - StatusBadge

/// Pill-shaped status badge. Self-sizing; embed in HStacks freely.
struct StatusBadge: View {
    let status: HopBadgeStatus

    var body: some View {
        Text(status.label)
            .font(HopFont.labelSmall())
            .fontWeight(.medium)
            .foregroundColor(status.foregroundColor)
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 4)
            .background(status.backgroundColor)
            .clipShape(Capsule())
    }
}

// MARK: - Previews

#Preview("StatusBadge — All") {
    HStack(spacing: HopSpacing.sm) {
        StatusBadge(status: .confirmed)
        StatusBadge(status: .pending)
        StatusBadge(status: .cancelled)
        StatusBadge(status: .modelA)
        StatusBadge(status: .modelB)
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("StatusBadge — Dark card") {
    VStack(alignment: .leading, spacing: HopSpacing.sm) {
        ForEach([HopBadgeStatus.confirmed, .pending, .cancelled, .modelA, .modelB], id: \.label) { s in
            HStack {
                Text(s.label)
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)
                Spacer()
                StatusBadge(status: s)
            }
        }
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurfaceElevated)
    .clipShape(RoundedRectangle(cornerRadius: 16))
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

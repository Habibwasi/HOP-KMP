import SwiftUI

/// PA-01 — Live countdown banner for the soonest upcoming confirmed booking.
/// Mirrors composeApp `ActiveBookingBanner.kt`. Returns `nil` (EmptyView) when
/// `departureIso` is missing, more than 24h away, or older than 6h past.
struct ActiveBookingBanner: View {
    let departureIso: String?
    let origin: String
    let destination: String
    let onTap: () -> Void

    @State private var now: Date = Date()
    @State private var dotAlpha: Double = 1.0

    private static let isoFormatter: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()
    private static let isoFormatterNoFrac: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime]
        return f
    }()

    private func parseDeparture() -> Date? {
        guard let iso = departureIso, !iso.isEmpty else { return nil }
        return Self.isoFormatter.date(from: iso) ?? Self.isoFormatterNoFrac.date(from: iso)
    }

    var body: some View {
        guard let departure = parseDeparture() else { return AnyView(EmptyView()) }
        let delta = departure.timeIntervalSince(now)
        // hide if >24h away or >6h past
        if delta > 24 * 3600 || delta < -6 * 3600 { return AnyView(EmptyView()) }

        let isPast = delta < 0
        let absSec = Int(abs(delta))
        let countdown = formatCountdown(seconds: absSec)

        return AnyView(
            Button(action: onTap) {
                HStack(spacing: 0) {
                    // Lime → green vertical accent stripe
                    LinearGradient(
                        colors: [Color.hopPrimaryLime, Color.hopPrimaryGreen],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(width: 4, height: 72)

                    HStack(spacing: HopSpacing.sm) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.hopPrimaryLime.opacity(0.2))
                                .frame(width: 36, height: 36)
                            Image(systemName: "clock.fill")
                                .font(.system(size: 18))
                                .foregroundColor(Color.hopPrimaryGreen)
                        }
                        VStack(alignment: .leading, spacing: 2) {
                            if isPast {
                                HStack(spacing: 6) {
                                    Circle()
                                        .fill(Color.hopPrimaryGreen.opacity(dotAlpha))
                                        .frame(width: 8, height: 8)
                                    Text("Trip in progress")
                                        .font(HopFont.titleSmall())
                                        .foregroundColor(Color.hopAuthTextPrimary)
                                }
                            } else {
                                Text("Departs in \(countdown)")
                                    .font(HopFont.titleSmall())
                                    .foregroundColor(Color.hopAuthTextPrimary)
                            }
                            Text("\(origin) → \(destination)")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                                .lineLimit(1)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color.hopAuthTextSecondary.opacity(0.6))
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.md)
                }
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
            }
            .buttonStyle(.plain)
            .onAppear {
                withAnimation(.easeInOut(duration: 0.7).repeatForever(autoreverses: true)) {
                    dotAlpha = 0.2
                }
            }
            .onReceive(Timer.publish(every: 1, on: .main, in: .common).autoconnect()) { date in
                self.now = date
            }
        )
    }

    private func formatCountdown(seconds: Int) -> String {
        let h = seconds / 3600
        let m = (seconds % 3600) / 60
        let s = seconds % 60
        if h > 0 { return "\(h)h \(pad(m))m \(pad(s))s" }
        if m > 0 { return "\(m)m \(pad(s))s" }
        return "\(s)s"
    }
    private func pad(_ n: Int) -> String { n < 10 ? "0\(n)" : "\(n)" }
}

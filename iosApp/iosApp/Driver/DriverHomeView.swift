//
//  DriverHomeView.swift
//  iosApp
//
//  DR-01 Driver Home — light-theme mirror of composeApp `DriverHomeScreen.kt`.
//  Rendered inside the unified `HomeView` so it does NOT draw the top bar
//  (HopLogo + RoleTogglePill + bell) — that's owned by `HomeView`.
//
//  Sections:
//    • EarningsHeroCard — lime card, mono DKK total, sparkline, est. tax.
//    • "Become a driver" prompt when `licenceStatus != approved`.
//    • Repost templates row (when there are completed trips).
//    • "My Trips" heading + list / loading / empty.
//    • Floating "Post a Trip" FAB (lime pill, bottom-right).
//

import SwiftUI
import Shared

struct DriverHomeView: View {

    var navigate: (HopRoute) -> Void

    @StateObject  private var wrapper     = DriverViewModelWrapper.shared
    @StateObject  private var authWrapper   = AuthViewModelWrapper()
    @State        private var toast: String? = nil

    /// Mirrors Android: a user is a driver when their `currentUser.roles`
    /// contains DRIVER. Licence verification was dropped — there is no
    /// pending/rejected state to worry about.
    private var hasDriverRole: Bool {
        authWrapper.state.currentUser?.roles.contains(where: {
            ($0 as? UserRole) == UserRole.driver
        }) ?? false
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color.hopBackground.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: HopSpacing.md) {

                    if hasDriverRole {
                        // ── Earnings hero ─────────────────────────────────
                        EarningsHeroCard(
                            monthlyOere: Int(wrapper.state.monthlyEarningsOere),
                            estimatedTaxOere: Int(wrapper.state.estimatedTaxOere),
                            series: synthesiseSeries(Int(wrapper.state.monthlyEarningsOere)),
                            onTap: { wrapper.tapEarningsBanner() }
                        )

                        // ── Repost templates ──────────────────────────────
                        let repostable = wrapper.state.trips
                            .filter { $0.trip.status == TripStatus.completed }
                            .prefix(5)
                        if !repostable.isEmpty {
                            Text("Repost a recent trip")
                                .font(HopFont.titleMedium(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                            RepostRow(
                                templates: Array(repostable),
                                onRepost: { _ in navigate(.postTripModelSelect) }
                            )
                        }
                    } else {
                        BecomeDriverPrompt(
                            onTap: { navigate(.enableDriverStep1) }
                        )
                    }

                    // ── Section heading ──────────────────────────────────
                    Text("My Trips")
                        .font(HopFont.titleMedium(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .padding(.top, HopSpacing.xs)

                    // ── Trips list / loading / empty ─────────────────────
                    if wrapper.state.isLoading && wrapper.state.trips.isEmpty {
                        VStack(spacing: HopSpacing.sm) {
                            ForEach(0..<3, id: \.self) { _ in
                                SkeletonCard()
                            }
                        }
                    } else if wrapper.state.trips.isEmpty {
                        EmptyTripsCard(
                            onPostTrip: {
                                if hasDriverRole {
                                    navigate(.postTripModelSelect)
                                } else {
                                    navigate(.enableDriverStep1)
                                }
                            }
                        )
                    } else {
                        VStack(spacing: HopSpacing.sm) {
                            ForEach(wrapper.state.trips, id: \.id) { trip in
                                Button {
                                    navigate(.tripDetailActiveDriver(tripId: trip.id))
                                } label: {
                                    DriverTripCard(trip: trip)
                                }
                                .buttonStyle(.plain)
                                .opacity(trip.isBroken ? 0.5 : 1.0)
                                .disabled(trip.isBroken)
                            }
                        }
                    }

                    // Bottom padding so last card clears the FAB.
                    Spacer().frame(height: HopSpacing.xxl + 56)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.md)
            }

            // ── Floating "Post a Trip" pill ──────────────────────────────
            Button(action: {
                if hasDriverRole {
                    navigate(.postTripModelSelect)
                } else {
                    navigate(.enableDriverStep1)
                }
            }) {
                HStack(spacing: HopSpacing.xs) {
                    Image(systemName: "plus")
                        .font(.system(size: 16, weight: .semibold))
                    Text("Post a Trip")
                        .font(HopFont.labelMedium(weight: .semibold))
                }
                .foregroundColor(Color.hopAuthTextPrimary)
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.sm + 2)
                .background(Color.hopPrimaryLime)
                .clipShape(Capsule())
                .shadow(color: .black.opacity(0.18), radius: 6, x: 0, y: 3)
            }
            .padding(.trailing, HopSpacing.md)
            .padding(.bottom, HopSpacing.md)

            if let msg = toast {
                ToastBubble(message: msg)
                    .padding(.bottom, HopSpacing.xxl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .task {
            authWrapper.startObserving()
            wrapper.loadDriverHome()
            for await effect in wrapper.effects {
                switch effect {
                case is DriverEffectNavigateToPostTrip:
                    navigate(.postTripModelSelect)
                case is DriverEffectNavigateToTaxDashboard:
                    navigate(.taxDashboard)
                case let snack as DriverEffectShowSnackbar:
                    showToast(snack.message)
                default: break
                }
            }
        }
    }

    private func showToast(_ message: String) {
        withAnimation { toast = message }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            withAnimation { if toast == message { toast = nil } }
        }
    }
}

// MARK: — Synthesised 7-day spark series (until /drivers/me/earnings/series)

private func synthesiseSeries(_ monthlyOere: Int) -> [Int] {
    if monthlyOere <= 0 { return Array(repeating: 0, count: 7) }
    let avg = monthlyOere / 30
    let factors: [Double] = [0.6, 0.7, 1.1, 0.9, 1.3, 1.5, 1.4]
    return factors.map { Int(Double(avg) * $0) }
}

// MARK: — EarningsHeroCard ─────────────────────────────────────────────────

private struct EarningsHeroCard: View {
    let monthlyOere: Int
    let estimatedTaxOere: Int
    let series: [Int]
    let onTap: () -> Void

    @State private var animatedDkk: Int = 0

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Earnings this month")
                    .font(HopFont.labelSmall(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary.opacity(0.7))

                HStack(alignment: .lastTextBaseline) {
                    Text("DKK ")
                        .font(HopFont.headlineSmall(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                    Text("\(animatedDkk)")
                        .font(HopFont.mono(size: 34, weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary.opacity(0.4))
                }

                Text("Est. tax: DKK \(estimatedTaxOere / 100)")
                    .font(HopFont.bodySmall(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary.opacity(0.65))

                Spacer().frame(height: HopSpacing.sm)

                Sparkline(series: series)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)

                Text("Last 7 days")
                    .font(HopFont.labelSmall())
                    .foregroundColor(Color.hopAuthTextPrimary.opacity(0.55))
            }
            .padding(HopSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.hopPrimaryLime)
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
        .onAppear {
            withAnimation(.easeOut(duration: 0.9)) {
                animatedDkk = monthlyOere / 100
            }
        }
        .onChange(of: monthlyOere) { _, new in
            withAnimation(.easeOut(duration: 0.9)) { animatedDkk = new / 100 }
        }
    }
}

private struct Sparkline: View {
    let series: [Int]

    var body: some View {
        Canvas { ctx, size in
            let data: [Int] = {
                if series.isEmpty { return [0, 0] }
                if series.count == 1 { return [series[0], series[0]] }
                return series
            }()
            let maxV = max(data.max() ?? 0, 1)
            let minV = data.min() ?? 0
            let range = max(maxV - minV, 1)
            let stepX = size.width / CGFloat(max(data.count - 1, 1))
            let h = size.height

            let points: [CGPoint] = data.enumerated().map { (i, v) in
                let x = stepX * CGFloat(i)
                let n = CGFloat(v - minV) / CGFloat(range)
                let y = h - (n * (h - 6)) - 3
                return CGPoint(x: x, y: y)
            }

            // Filled area
            var fill = Path()
            fill.move(to: CGPoint(x: points[0].x, y: h))
            for p in points { fill.addLine(to: p) }
            if let lastPoint = points.last {
                fill.addLine(to: CGPoint(x: lastPoint.x, y: h))
            }
            fill.closeSubpath()
            ctx.fill(
                fill,
                with: .linearGradient(
                    Gradient(colors: [
                        Color.hopAuthTextPrimary.opacity(0.18),
                        Color.clear
                    ]),
                    startPoint: CGPoint(x: 0, y: 0),
                    endPoint: CGPoint(x: 0, y: h)
                )
            )
            // Line
            var line = Path()
            line.move(to: points[0])
            for i in 1..<points.count { line.addLine(to: points[i]) }
            ctx.stroke(
                line,
                with: .color(Color.hopAuthTextPrimary),
                style: StrokeStyle(lineWidth: 2.5, lineCap: .round)
            )
        }
    }
}

// MARK: — Become-driver prompt ─────────────────────────────────────────────

private struct BecomeDriverPrompt: View {
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: HopSpacing.xs) {
                Text("Save money driving with Hop")
                    .font(HopFont.titleMedium(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Text("Set your own route, time, and price. Tap below — we'll walk you through the setup.")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextPrimary.opacity(0.7))
                HStack(spacing: 4) {
                    Text("Become a driver")
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
                .padding(.top, HopSpacing.xs)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.lg)
            .background(Color.hopPrimaryLime)
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }
}

// MARK: — Repost row ───────────────────────────────────────────────────────

private struct RepostRow: View {
    let templates: [TripUiModel]
    let onRepost: (TripUiModel) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: HopSpacing.sm) {
                ForEach(templates, id: \.id) { t in
                    Button(action: { onRepost(t) }) {
                        VStack(alignment: .leading, spacing: 4) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 11, weight: .semibold))
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                Text("Repost")
                                    .font(HopFont.labelSmall(weight: .semibold))
                                    .foregroundColor(Color.hopAuthTextSecondary)
                            }
                            Text("\(t.trip.originName) → \(t.trip.destName)")
                                .font(HopFont.labelMedium(weight: .semibold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .lineLimit(1)
                            Text("DKK \(Int(t.priceOerePerSeat) / 100)/seat")
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                        }
                        .padding(HopSpacing.sm)
                        .frame(width: 200, alignment: .leading)
                        .driverCard()
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

// MARK: — Driver trip card ────────────────────────────────────────────────

private struct DriverTripCard: View {
    let trip: TripUiModel

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack(spacing: HopSpacing.xs) {
                modelBadge
                statusBadge
                Spacer()
                if trip.hasRecentBooking {
                    Text("New booking")
                        .font(HopFont.labelSmall(weight: .semibold))
                        .foregroundColor(Color.hopPrimaryGreen)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.hopPrimaryGreen.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                    Spacer().frame(width: HopSpacing.xs)
                }
                Text("\(trip.seatsBooked)/\(trip.seatsTotal) seats")
                    .font(HopFont.labelSmall(weight: .semibold))
                    .foregroundColor(Color(hex: 0x666666))
            }
            Divider().background(Color(hex: 0xF0F0F0))

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: HopSpacing.sm) {
                    Circle().fill(Color.hopPrimaryLime).frame(width: 10, height: 10)
                    Text(trip.trip.originName)
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .lineLimit(1)
                }
                Rectangle()
                    .fill(Color(hex: 0xD0D0D0))
                    .frame(width: 2, height: 12)
                    .padding(.leading, 4)
                HStack(spacing: HopSpacing.sm) {
                    Circle().fill(Color.hopAuthTextPrimary).frame(width: 10, height: 10)
                    Text(trip.trip.destName)
                        .font(HopFont.bodyMedium(weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .lineLimit(1)
                }
            }

            Divider().background(Color(hex: 0xF0F0F0))

            HStack {
                Text("Departs \(trip.trip.departsAt)")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color(hex: 0x666666))
                Spacer()
                Text("DKK \(Int(trip.trip.driverNetOere) / 100)/seat")
                    .font(HopFont.bodyLarge(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity)
        .driverCard()
        .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }

    @ViewBuilder
    private var modelBadge: some View {
        let isB = trip.model == .b
        Text(isB ? "Model B" : "Model A")
            .font(HopFont.labelSmall(weight: .semibold))
            .foregroundColor(isB ? Color.hopPrimaryGreen : Color(hex: 0x1976D2))
            .padding(.horizontal, HopSpacing.xs)
            .padding(.vertical, 3)
            .background((isB ? Color.hopPrimaryGreen : Color(hex: 0x1976D2)).opacity(0.12))
            .clipShape(Capsule())
    }

    @ViewBuilder
    private var statusBadge: some View {
        let (label, color): (String, Color) = {
            switch trip.trip.status {
            case TripStatus.active:    return ("Active",    Color.hopPrimaryGreen)
            case TripStatus.confirmed: return ("Confirmed", Color.hopSuccess)
            case TripStatus.completed: return ("Completed", Color.hopAuthTextSecondary)
            case TripStatus.cancelled: return ("Cancelled", Color.hopError)
            default: return ("Pending", Color.hopWarning)
            }
        }()
        Text(label)
            .font(HopFont.labelSmall(weight: .semibold))
            .foregroundColor(color)
            .padding(.horizontal, HopSpacing.xs)
            .padding(.vertical, 3)
            .background(color.opacity(0.12))
            .clipShape(Capsule())
    }
}

// MARK: — Empty / skeleton helpers ─────────────────────────────────────────

private struct EmptyTripsCard: View {
    let onPostTrip: () -> Void

    var body: some View {
        VStack(spacing: HopSpacing.md) {
            ZStack {
                Circle().fill(Color.hopCardSurfaceMuted).frame(width: 80, height: 80)
                Image(systemName: "car.fill")
                    .font(.system(size: 32))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            Text("Post your first trip to start earning")
                .font(HopFont.titleMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
                .multilineTextAlignment(.center)
            Text("Set a route, pick a time, and let passengers book seats.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, HopSpacing.md)
            HopButton(text: "Post a trip", variant: .primary, action: onPostTrip)
                .frame(maxWidth: 240)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, HopSpacing.xl)
    }
}

private struct SkeletonCard: View {
    @State private var pulse: Double = 0.4
    var body: some View {
        RoundedRectangle(cornerRadius: 12)
            .fill(Color.hopCardSurfaceMuted.opacity(pulse))
            .frame(height: 120)
            .onAppear {
                withAnimation(.easeInOut(duration: 1.0).repeatForever(autoreverses: true)) {
                    pulse = 0.8
                }
            }
    }
}

private struct ToastBubble: View {
    let message: String
    var body: some View {
        Text(message)
            .font(HopFont.bodySmall())
            .foregroundColor(.white)
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.sm)
            .background(Color.black.opacity(0.85))
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

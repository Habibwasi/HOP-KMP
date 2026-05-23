import SwiftUI
import Shared

/// PA-06 — Booking Success. Mirrors composeApp `BookingSuccessScreen.kt`.
/// Animated checkmark + Model A/B headline + trip summary + actions.
struct BookingSuccessView: View {
    let bookingId: String
    let onViewMyTrips: () -> Void
    let onBackToHome: () -> Void

    @StateObject private var wrapper = BookingSuccessViewModelWrapper()

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .center, spacing: HopSpacing.lg) {
                    Spacer().frame(height: HopSpacing.xxl)
                    AnimatedCheckmark(color: headlineColor)

                    Text(headline)
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(headlineColor)
                        .multilineTextAlignment(.center)

                    Text(subhead)
                        .font(HopFont.bodyLarge())
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.lg)

                    if !wrapper.state.isLoading {
                        TripSummaryBox(
                            originName: wrapper.state.originName,
                            destName: wrapper.state.destName,
                            departsAt: wrapper.state.departsAt,
                            driverName: wrapper.state.driverName
                        )
                        .padding(.horizontal, HopSpacing.md)
                    }
                    Spacer().frame(height: HopSpacing.lg)
                }
                .frame(maxWidth: .infinity)
            }

            VStack(spacing: HopSpacing.sm) {
                HopButton(text: "View My Trips", variant: .primary, action: onViewMyTrips)
                HopButton(text: "Back to Home", variant: .ghost, lightSurface: true, action: onBackToHome)
            }
            .padding(HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            wrapper.startObserving()
            wrapper.load(bookingId: bookingId)
        }
    }

    private var isModelB: Bool { wrapper.state.tripModel == .b }
    private var headline: String { isModelB ? "Booking Pending" : "Booking Confirmed!" }
    private var subhead: String {
        isModelB
            ? "We'll confirm your seat once enough passengers join. You'll be notified."
            : "Your seat is reserved. We've sent the details to your inbox."
    }
    private var headlineColor: Color { isModelB ? Color.hopWarning : Color.hopSuccess }
}

private struct AnimatedCheckmark: View {
    let color: Color
    @State private var scale: CGFloat = 0
    @State private var strokeProgress: CGFloat = 0

    var body: some View {
        ZStack {
            Circle()
                .fill(color.opacity(0.15))
                .frame(width: 96, height: 96)
                .scaleEffect(scale)
            Path { p in
                p.move(to: CGPoint(x: 28, y: 50))
                p.addLine(to: CGPoint(x: 44, y: 66))
                p.addLine(to: CGPoint(x: 70, y: 36))
            }
            .trim(from: 0, to: strokeProgress)
            .stroke(color, style: StrokeStyle(lineWidth: 5, lineCap: .round, lineJoin: .round))
            .frame(width: 96, height: 96)
        }
        .frame(width: 96, height: 96)
        .onAppear {
            withAnimation(.easeOut(duration: 0.35)) { scale = 1.1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                withAnimation(.easeInOut(duration: 0.15)) { scale = 1.0 }
                withAnimation(.easeInOut(duration: 0.4)) { strokeProgress = 1.0 }
            }
        }
    }
}

private struct TripSummaryBox: View {
    let originName: String
    let destName: String
    let departsAt: String
    let driverName: String

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack {
                Circle().fill(Color.hopPrimaryLime).frame(width: 10, height: 10)
                Text(originName)
                    .font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack {
                Circle().fill(Color.hopAuthTextPrimary).frame(width: 10, height: 10)
                Text(destName)
                    .font(HopFont.bodyMedium(weight: .medium))
                    .foregroundColor(Color.hopAuthTextPrimary)
            }
            Divider()
            HStack {
                Image(systemName: "calendar").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(departsAt).font(HopFont.bodySmall()).foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
                Image(systemName: "person.fill").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(driverName).font(HopFont.bodySmall()).foregroundColor(Color.hopAuthTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

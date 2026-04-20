import SwiftUI
import Shared

// MARK: — PA-06 Booking Success ────────────────────────────────────────────────
//
// Shown after MobilePay confirms payment.
// Displays booking ID, confirmation animation, CTAs: View My Trips / Back to Home.

struct BookingSuccessView: View {

    let bookingId: String
    var tripModel: TripModel = .a   // .b → "Booking Pending" amber state

    var onViewMyTrips: () -> Void   // → PA-07 My Trips
    var onGoHome:      () -> Void   // → PA-01 Home

    @State private var circleScale:   CGFloat = 0.0
    @State private var checkScale:    CGFloat = 0.0
    @State private var contentOffset: CGFloat = 30

    private var isModelB: Bool { tripModel == .b }
    private var accentColor: Color { isModelB ? Color.hopWarning : Color.hopSuccess }
    private var headline: String { isModelB ? "Booking Pending" : "Booking Confirmed!" }
    private var subtitle: String {
        isModelB
            ? "Your seat is reserved. Payment will be charged when the trip is confirmed."
            : "Your seat is reserved. Have a great trip!"
    }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea(.all, edges: .top)

            VStack(spacing: HopSpacing.xl) {
                Spacer()

                // ── Success animation ─────────────────────────────────────────
                ZStack {
                    Circle()
                        .fill(accentColor.opacity(0.12))
                        .frame(width: 140, height: 140)
                        .scaleEffect(circleScale)

                    Circle()
                        .fill(accentColor.opacity(0.2))
                        .frame(width: 110, height: 110)
                        .scaleEffect(circleScale)

                    Image(systemName: isModelB ? "clock.circle.fill" : "checkmark.circle.fill")
                        .font(.system(size: 68, weight: .regular))
                        .foregroundColor(accentColor)
                        .scaleEffect(checkScale)
                }

                // ── Text block ────────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    Text(headline)
                        .font(HopFont.headlineLarge())
                        .fontWeight(.bold)
                        .foregroundColor(accentColor)
                        .multilineTextAlignment(.center)

                    Text(subtitle)
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)
                        .multilineTextAlignment(.center)

                    // ── Booking ID pill ───────────────────────────────────────
                    HStack(spacing: HopSpacing.xs) {
                        Image(systemName: "ticket")
                            .font(.system(size: 13))
                            .foregroundColor(Color.hopTextSecondary)
                        Text("Booking #\(bookingId.prefix(8).uppercased())")
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopTextSecondary)
                    }
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, HopSpacing.xxs)
                    .background(Color.hopSurfaceElevated)
                    .clipShape(Capsule())
                    .padding(.top, HopSpacing.xs)
                }
                .offset(y: contentOffset)

                Spacer()

                // ── CTAs ──────────────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    HopPrimaryButton(title: "View My Trips", action: onViewMyTrips)
                    HopButton(text: "Back to Home", variant: .ghost, action: onGoHome)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xl)
                .offset(y: contentOffset)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .onAppear {
            // Staggered entrance animation
            withAnimation(.spring(response: 0.5, dampingFraction: 0.7).delay(0.1)) {
                circleScale = 1.0
            }
            withAnimation(.spring(response: 0.45, dampingFraction: 0.65).delay(0.3)) {
                checkScale = 1.0
            }
            withAnimation(.easeOut(duration: 0.4).delay(0.35)) {
                contentOffset = 0
            }
        }
    }
}

// MARK: — Previews

#Preview("PA-06 Booking Success") {
    NavigationStack {
        BookingSuccessView(
            bookingId:   "booking-abc123def456",
            onViewMyTrips: {},
            onGoHome:      {}
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("PA-06 Booking Success — Short ID") {
    NavigationStack {
        BookingSuccessView(
            bookingId:   "bk-001",
            onViewMyTrips: {},
            onGoHome:      {}
        )
    }
    .preferredColorScheme(.dark)
}

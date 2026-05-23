import SwiftUI
import Shared

/// PA-10 — Cancellation Confirmation. Mirrors composeApp `CancellationConfirmationScreen.kt`.
struct CancellationConfirmationView: View {
    let bookingId: String
    let onBackToMyTrips: () -> Void

    @StateObject private var wrapper = CancellationConfirmationViewModelWrapper()

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .center, spacing: HopSpacing.xl) {
                    Spacer().frame(height: HopSpacing.xxl)
                    CancellationIcon()
                    Text("Booking Cancelled")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopError)
                        .multilineTextAlignment(.center)

                    Text(refundText)
                        .font(HopFont.bodyLarge())
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.lg)

                    if wrapper.state.isModelB {
                        Text("Payment hold will be released within 1-2 business days.")
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .multilineTextAlignment(.center)
                            .padding(HopSpacing.md)
                            .frame(maxWidth: .infinity)
                            .background(Color.hopAuthInputSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            .padding(.horizontal, HopSpacing.lg)
                    }
                    Spacer().frame(height: HopSpacing.xxl)
                }
                .frame(maxWidth: .infinity)
            }
            HopButton(text: "Back to My Trips", variant: .primary, action: onBackToMyTrips)
                .padding(HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            wrapper.startObserving()
            wrapper.load(bookingId: bookingId)
        }
    }

    private var refundText: String {
        if let amount = wrapper.state.refundAmountOere?.intValue {
            let dkk = amount / 100
            let ore = amount % 100
            let s = ore == 0 ? "DKK \(dkk)" : String(format: "DKK %d,%02d", dkk, ore)
            return "Your refund of \(s) is on its way."
        }
        return "Your refund is on its way."
    }
}

/// Animated red-circle X. Circle scales 0→1 over 350ms, then strokes draw 250ms.
private struct CancellationIcon: View {
    @State private var circleProgress: Double = 0
    @State private var strokeProgress: Double = 0

    var body: some View {
        Canvas { ctx, size in
            let w = size.width, h = size.height
            let cx = w / 2, cy = h / 2
            let radius = min(w, h) / 2 - 4

            // Circle outline
            let circlePath = Path(ellipseIn: CGRect(
                x: cx - radius * circleProgress,
                y: cy - radius * circleProgress,
                width: radius * 2 * circleProgress,
                height: radius * 2 * circleProgress
            ))
            ctx.stroke(circlePath,
                       with: .color(.red),
                       style: StrokeStyle(lineWidth: 4, lineCap: .round))

            if strokeProgress > 0 {
                let armLen = radius * 0.55 * strokeProgress
                var p1 = Path()
                p1.move(to: CGPoint(x: cx, y: cy))
                p1.addLine(to: CGPoint(x: cx - armLen, y: cy - armLen))
                p1.move(to: CGPoint(x: cx, y: cy))
                p1.addLine(to: CGPoint(x: cx + armLen, y: cy + armLen))
                p1.move(to: CGPoint(x: cx, y: cy))
                p1.addLine(to: CGPoint(x: cx - armLen, y: cy + armLen))
                p1.move(to: CGPoint(x: cx, y: cy))
                p1.addLine(to: CGPoint(x: cx + armLen, y: cy - armLen))
                ctx.stroke(p1,
                           with: .color(.red),
                           style: StrokeStyle(lineWidth: 4, lineCap: .round, lineJoin: .round))
            }
        }
        .frame(width: 96, height: 96)
        .accessibilityLabel("Booking cancelled")
        .onAppear {
            withAnimation(.easeInOut(duration: 0.35)) { circleProgress = 1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                withAnimation(.easeInOut(duration: 0.25)) { strokeProgress = 1 }
            }
        }
    }
}

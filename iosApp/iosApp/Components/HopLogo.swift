import SwiftUI

// MARK: - HopLogo

/// Ridly brand lockup: chevron-in-lime-pill icon mark + "ridly" wordmark.
/// [height] drives all proportions. Variants:
/// - Dark bg  : `HopLogo(textColor: .white)`
/// - Light bg : `HopLogo()` (defaults)
/// - Lime bg  : `HopLogo(markBg: .black, markStroke: Color(hex:0xC5FF45))`
struct HopLogo: View {
    var height: CGFloat = 38
    var markBg: Color = Color(hex: 0xC5FF45)
    var markStroke: Color = Color(hex: 0x0B0B0B)
    var textColor: Color = Color(hex: 0x0B0B0B)

    var body: some View {
        HStack(spacing: height * 0.35) {
            // ── Icon mark: chevron in lime pill ───────────────────────────────
            Canvas { context, size in
                // Lime rounded-square background (cornerRadius ≈ 25%)
                let cr = size.width * 0.25
                context.fill(
                    Path(roundedRect: CGRect(origin: .zero, size: size), cornerRadius: cr),
                    with: .color(markBg)
                )
                // Chevron from 44×44 viewBox: M14 8 L30 22 L14 36
                let s = size.width / 44
                var chevron = Path()
                chevron.move(to: CGPoint(x: 14 * s, y: 8 * s))
                chevron.addLine(to: CGPoint(x: 30 * s, y: 22 * s))
                chevron.addLine(to: CGPoint(x: 14 * s, y: 36 * s))
                context.stroke(
                    chevron,
                    with: .color(markStroke),
                    style: StrokeStyle(lineWidth: 7 * s, lineCap: .round, lineJoin: .round)
                )
            }
            .frame(width: height, height: height)

            // ── "ridly" wordmark ──────────────────────────────────────────────
            Text("ridly")
                .font(.custom("Nunito-Black", size: height * 0.9))
                .foregroundColor(textColor)
                .kerning(height * 0.9 * -0.03)
                .lineLimit(1)
                .fixedSize(horizontal: true, vertical: false)
        }
        .accessibilityLabel("Ridly")
    }
}

// MARK: - Previews

#Preview("HopLogo — dark bg") {
    HopLogo(textColor: .white)
        .padding()
        .background(Color(hex: 0x0B0B0B))
}

#Preview("HopLogo — light bg") {
    HopLogo()
        .padding()
        .background(Color.white)
}

#Preview("HopLogo — lime bg") {
    HopLogo(markBg: Color(hex: 0x0B0B0B), markStroke: Color(hex: 0xC5FF45))
        .padding()
        .background(Color(hex: 0xC5FF45))
}

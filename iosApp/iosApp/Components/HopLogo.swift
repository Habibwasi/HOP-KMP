import SwiftUI

// MARK: - HopLogo

/// Hop brand logotype rendered as a Canvas. Mirrors `HopLogo.kt` 1:1.
///
/// The logo is drawn from a 315×134 viewBox, scaled uniformly to fit the
/// declared (width × height), and centered.
struct HopLogo: View {
    var width: CGFloat = 88
    var height: CGFloat = 38

    var body: some View {
        Canvas { context, size in
            let viewW: CGFloat = 315
            let viewH: CGFloat = 134
            let scale = min(size.width / viewW, size.height / viewH)
            let offsetX = (size.width  - viewW * scale) / 2
            let offsetY = (size.height - viewH * scale) / 2

            context.translateBy(x: offsetX, y: offsetY)
            context.scaleBy(x: scale, y: scale)

            let lime   = Color(hex: 0xC8F135)
            let ink    = Color(hex: 0x1A1A1A)
            let bgMask = lime
            let dash   = Color(hex: 0x167A30)

            // Pill background
            context.fill(
                Path(roundedRect: CGRect(x: 0, y: 0, width: 315, height: 134), cornerRadius: 36),
                with: .color(lime)
            )

            // h — left stem
            context.fill(
                Path(roundedRect: CGRect(x: 25, y: 20, width: 15, height: 84), cornerRadius: 7),
                with: .color(ink)
            )
            // h — right stem
            context.fill(
                Path(roundedRect: CGRect(x: 88, y: 50, width: 15, height: 54), cornerRadius: 7),
                with: .color(ink)
            )
            // h — crossbar
            var crossbar = Path()
            crossbar.move(to: CGPoint(x: 40, y: 74))
            crossbar.addQuadCurve(to: CGPoint(x: 88, y: 58), control: CGPoint(x: 54, y: 40))
            context.stroke(crossbar, with: .color(ink), style: StrokeStyle(lineWidth: 15, lineCap: .round))

            // o — ring
            let ringRect = CGRect(x: 130, y: 49, width: 66, height: 66)
            context.stroke(Path(ellipseIn: ringRect), with: .color(ink), lineWidth: 15)

            // o — road strip mask
            var road = Path()
            road.move(to: CGPoint(x: 131, y: 82))
            road.addLine(to: CGPoint(x: 196, y: 82))
            context.stroke(road, with: .color(bgMask), lineWidth: 5)

            // o — center-line dashes
            for x in stride(from: CGFloat(141), through: 173, by: 16) {
                var d = Path()
                d.move(to: CGPoint(x: x, y: 82))
                d.addLine(to: CGPoint(x: x + 10, y: 82))
                context.stroke(d, with: .color(dash), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
            }

            // p — stem
            context.fill(
                Path(roundedRect: CGRect(x: 217, y: 48, width: 15, height: 76), cornerRadius: 7),
                with: .color(ink)
            )
            // p — bowl
            var bowl = Path()
            bowl.move(to: CGPoint(x: 232, y: 66))
            bowl.addQuadCurve(to: CGPoint(x: 263, y: 48), control: CGPoint(x: 232, y: 35))
            bowl.addQuadCurve(to: CGPoint(x: 279, y: 82), control: CGPoint(x: 290, y: 60))
            bowl.addQuadCurve(to: CGPoint(x: 232, y: 98), control: CGPoint(x: 268, y: 104))
            context.stroke(bowl, with: .color(ink), style: StrokeStyle(lineWidth: 15, lineCap: .round, lineJoin: .round))
        }
        .frame(width: width, height: height)
        .accessibilityLabel("Hop")
    }
}

// MARK: - Previews

#Preview("HopLogo — default") {
    HopLogo()
        .padding()
        .background(Color.white)
}

#Preview("HopLogo — large") {
    HopLogo(width: 200, height: 86)
        .padding()
        .background(Color.white)
}

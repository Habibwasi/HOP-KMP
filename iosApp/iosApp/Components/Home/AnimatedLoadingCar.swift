//
//  AnimatedLoadingCar.swift
//  iosApp
//
//  iOS port of the cartoon car loading indicator from
//  composeApp/.../PassengerHomeScreen.kt → AnimatedLoadingIndicator.
//
//  Drives in from the left, parks in the centre, bobs gently with spinning
//  wheels and a soft ground shadow. "Finding rides..." caption pulses below.
//

import SwiftUI

struct AnimatedLoadingCar: View {

    /// Caption shown below the car (defaults to the Android string).
    var caption: String = "Finding rides..."

    // Reference vp — matches Compose's 240×150 design grid.
    private let designW: CGFloat = 240
    private let designH: CGFloat = 150

    var body: some View {
        TimelineView(.animation) { timeline in
            let t = timeline.date.timeIntervalSinceReferenceDate

            // Drive-in: -1 (off-screen left) → 0 (centre) over 0.9s, ease-out.
            let driveIn: CGFloat = {
                let elapsed = t.truncatingRemainder(dividingBy: 100_000) // never resets in practice
                let progress = min(max(elapsed / 0.9, 0), 1)
                let eased = 1 - pow(1 - progress, 3) // ease-out cubic
                return CGFloat(-1 + eased)
            }()

            // Wheel angle: 360°/0.9s linear.
            let wheelAngle: CGFloat = CGFloat((t / 0.9).truncatingRemainder(dividingBy: 1) * 360)

            // Bob: -4..0 over 0.9s, eased reverse.
            let bobBase: CGFloat = {
                let phase = (t / 0.9).truncatingRemainder(dividingBy: 1)
                let tri = phase < 0.5 ? phase * 2 : (1 - phase) * 2 // 0..1..0
                let eased = tri * tri * (3 - 2 * tri)
                return CGFloat(-4 * eased)
            }()

            // Caption alpha pulse 0.55..1 over 1.4s reverse.
            let textAlpha: Double = {
                let phase = (t / 1.4).truncatingRemainder(dividingBy: 1)
                let tri = phase < 0.5 ? phase * 2 : (1 - phase) * 2
                let eased = tri * tri * (3 - 2 * tri)
                return 0.55 + 0.45 * eased
            }()

            VStack(spacing: HopSpacing.sm) {
                Canvas { ctx, size in
                    drawScene(
                        ctx: ctx,
                        size: size,
                        driveIn: driveIn,
                        wheelAngle: wheelAngle,
                        bobBase: bobBase
                    )
                }
                .frame(width: designW, height: 130)

                Text(caption)
                    .font(HopFont.labelSmall())
                    .foregroundColor(Color.hopAuthTextSecondary.opacity(textAlpha))
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: — Drawing ────────────────────────────────────────────────────────

    private func drawScene(
        ctx: GraphicsContext,
        size: CGSize,
        driveIn: CGFloat,
        wheelAngle: CGFloat,
        bobBase: CGFloat
    ) {
        let sx = size.width / designW
        let sy = size.height / 150
        let sr = min(sx, sy)
        func x(_ v: CGFloat) -> CGFloat { v * sx }
        func y(_ v: CGFloat) -> CGFloat { v * sy }
        func r(_ v: CGFloat) -> CGFloat { v * sr }

        let parked = driveIn >= 0
        let bobFactor: CGFloat = parked ? 1 : 0
        let travelVp = driveIn * 260
        let bobVp = bobBase * bobFactor

        // Soft ground shadow
        let shadowScale: CGFloat = 1 - 0.22 * bobFactor * (-bobBase / 4)
        let shadowW = x(120) * shadowScale
        let shadowH = y(8) * shadowScale
        let shadowRect = CGRect(
            x: x(120 + travelVp) - shadowW / 2,
            y: y(110) - shadowH / 2,
            width: shadowW,
            height: shadowH
        )
        ctx.fill(
            Path(ellipseIn: shadowRect),
            with: .color(Color(red: 0x26/255, green: 0x32/255, blue: 0x38/255).opacity(0.22 * shadowScale))
        )

        // Translate the car body (drive-in + bob).
        var car = ctx
        car.translateBy(x: (travelVp + 20) * sx, y: bobVp * sy)

        // Lower body
        car.fill(
            Path(roundedRect: CGRect(x: x(30), y: y(58), width: x(140), height: y(34)),
                 cornerRadius: r(10)),
            with: .color(Color(red: 0x26/255, green: 0xC6/255, blue: 0xDA/255))
        )
        // Cabin
        car.fill(
            Path(roundedRect: CGRect(x: x(52), y: y(38), width: x(96), height: y(28)),
                 cornerRadius: r(10)),
            with: .color(Color(red: 0x00/255, green: 0xAC/255, blue: 0xC1/255))
        )
        // Windows
        let windowFill: GraphicsContext.Shading = .color(
            Color(red: 0xE0/255, green: 0xF7/255, blue: 0xFA/255).opacity(0.95)
        )
        car.fill(
            Path(roundedRect: CGRect(x: x(58), y: y(43), width: x(36), height: y(20)),
                 cornerRadius: r(5)),
            with: windowFill
        )
        car.fill(
            Path(roundedRect: CGRect(x: x(106), y: y(43), width: x(36), height: y(20)),
                 cornerRadius: r(5)),
            with: windowFill
        )

        // Passenger heads
        car.fill(
            Path(ellipseIn: CGRect(x: x(76) - r(8), y: y(53) - r(8), width: r(16), height: r(16))),
            with: .color(Color(red: 0xFF/255, green: 0xCC/255, blue: 0x80/255))
        )
        car.fill(
            Path(ellipseIn: CGRect(x: x(124) - r(8), y: y(53) - r(8), width: r(16), height: r(16))),
            with: .color(Color(red: 0xFF/255, green: 0xAB/255, blue: 0x91/255))
        )

        // Eyes
        let eyeR = r(1.3)
        let eyeColor: GraphicsContext.Shading = .color(
            Color(red: 0x3E/255, green: 0x27/255, blue: 0x23/255)
        )
        for (cx, cy) in [(73.5, 51.5), (78.5, 51.5), (121.5, 51.5), (126.5, 51.5)] {
            car.fill(
                Path(ellipseIn: CGRect(
                    x: x(CGFloat(cx)) - eyeR,
                    y: y(CGFloat(cy)) - eyeR,
                    width: eyeR * 2, height: eyeR * 2
                )),
                with: eyeColor
            )
        }

        // Smiles (quadratic curves)
        let smileColor: GraphicsContext.Shading = .color(
            Color(red: 0xE6/255, green: 0x4A/255, blue: 0x19/255)
        )
        let smileStroke = StrokeStyle(lineWidth: r(1.2), lineCap: .round)
        for cx in [76.0, 124.0] {
            var p = Path()
            p.move(to: CGPoint(x: x(CGFloat(cx) - 3), y: y(55.5)))
            p.addQuadCurve(
                to: CGPoint(x: x(CGFloat(cx) + 3), y: y(55.5)),
                control: CGPoint(x: x(CGFloat(cx)), y: y(58))
            )
            car.stroke(p, with: smileColor, style: smileStroke)
        }

        // Bumper stripe
        car.fill(
            Path(roundedRect: CGRect(x: x(30), y: y(82), width: x(140), height: y(6)),
                 cornerRadius: r(3)),
            with: .color(Color(red: 0x00/255, green: 0x97/255, blue: 0xA7/255))
        )
        // Front + rear lights
        car.fill(
            Path(roundedRect: CGRect(x: x(162), y: y(66), width: x(8), height: y(7)),
                 cornerRadius: r(2.5)),
            with: .color(Color(red: 0xFF/255, green: 0xF5/255, blue: 0x9D/255))
        )
        car.fill(
            Path(roundedRect: CGRect(x: x(30), y: y(66), width: x(8), height: y(7)),
                 cornerRadius: r(2.5)),
            with: .color(Color(red: 0xEF/255, green: 0x9A/255, blue: 0x9A/255))
        )

        // Wheels
        drawSpinningWheel(ctx: &car, center: CGPoint(x: x(60), y: y(92)), radius: r(13), angleDeg: wheelAngle)
        drawSpinningWheel(ctx: &car, center: CGPoint(x: x(140), y: y(92)), radius: r(13), angleDeg: wheelAngle)
    }

    private func drawSpinningWheel(
        ctx: inout GraphicsContext,
        center: CGPoint,
        radius: CGFloat,
        angleDeg: CGFloat
    ) {
        // Outer
        ctx.fill(
            Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius,
                                   width: radius * 2, height: radius * 2)),
            with: .color(Color(red: 0x37/255, green: 0x47/255, blue: 0x4F/255))
        )
        // Mid
        let midR = radius * 0.65
        ctx.fill(
            Path(ellipseIn: CGRect(x: center.x - midR, y: center.y - midR,
                                   width: midR * 2, height: midR * 2)),
            with: .color(Color(red: 0x78/255, green: 0x90/255, blue: 0x9C/255))
        )
        // Spokes — rotate around centre
        var spokeCtx = ctx
        spokeCtx.translateBy(x: center.x, y: center.y)
        spokeCtx.rotate(by: .degrees(Double(angleDeg)))
        let spoke = radius * 0.85
        let w = radius * 0.18
        let spokeColor: GraphicsContext.Shading = .color(
            Color(red: 0x54/255, green: 0x6E/255, blue: 0x7A/255)
        )
        var v = Path()
        v.move(to: CGPoint(x: 0, y: -spoke))
        v.addLine(to: CGPoint(x: 0, y: spoke))
        spokeCtx.stroke(v, with: spokeColor,
                        style: StrokeStyle(lineWidth: w, lineCap: .round))
        var h = Path()
        h.move(to: CGPoint(x: -spoke, y: 0))
        h.addLine(to: CGPoint(x: spoke, y: 0))
        spokeCtx.stroke(h, with: spokeColor,
                        style: StrokeStyle(lineWidth: w, lineCap: .round))

        // Hub
        let hubR = radius * 0.28
        ctx.fill(
            Path(ellipseIn: CGRect(x: center.x - hubR, y: center.y - hubR,
                                   width: hubR * 2, height: hubR * 2)),
            with: .color(Color(red: 0xB0/255, green: 0xBE/255, blue: 0xC5/255))
        )
    }
}

#Preview {
    AnimatedLoadingCar()
        .padding()
        .background(Color.white)
}

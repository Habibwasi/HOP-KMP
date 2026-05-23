import SwiftUI

// MARK: - OnboardingIllustration ──────────────────────────────────────────────
// Faithful port of `OnboardingIllustrations.kt`. Each slide is a TimelineView-
// driven Canvas that produces continuous animations matching Android's
// `rememberInfiniteTransition` semantics (Reverse = sine wave, Restart = sawtooth).

struct OnboardingIllustration: View {
    let index: Int

    var body: some View {
        switch index {
        case 0:  Slide1Illustration()
        case 1:  Slide2Illustration()
        default: Slide3Illustration()
        }
    }
}

// MARK: - Animation phase helpers

/// 0..1 sine oscillation (matches Android `RepeatMode.Reverse`).
private func sineWave(_ t: Double, periodSec: Double, phaseOffset: Double = 0) -> CGFloat {
    let p = (t / periodSec + phaseOffset).truncatingRemainder(dividingBy: 1)
    // Map 0..1 → 0→1→0 via a cosine
    return CGFloat((1 - cos(p * 2 * .pi)) / 2)
}

/// 0..1 sawtooth (matches Android `RepeatMode.Restart`, e.g. wheels, dashes).
private func sawtooth(_ t: Double, periodSec: Double, phaseOffset: Double = 0) -> CGFloat {
    let p = (t / periodSec + phaseOffset).truncatingRemainder(dividingBy: 1)
    return CGFloat(p < 0 ? p + 1 : p)
}

// MARK: - Slide 1 — Day scene ────────────────────────────────────────────────

private struct Slide1Illustration: View {
    var body: some View {
        TimelineView(.animation) { context in
            let t = context.date.timeIntervalSinceReferenceDate

            let carY      = -6 * sineWave(t, periodSec: 3.0)
            let wheelDeg  = sawtooth(t, periodSec: 1.2) * 360
            let sunScale  = 1 + 0.07 * sineWave(t, periodSec: 3.0)
            let cloud1X   = 8 * sineWave(t, periodSec: 4.0)
            let cloud2X   = -8 * sineWave(t, periodSec: 5.0)
            let leaf1     = sineWave(t, periodSec: 2.4)
            let leaf2     = sineWave(t, periodSec: 2.8, phaseOffset: 0.14)
            let leaf3     = sineWave(t, periodSec: 2.2, phaseOffset: 0.41)

            Canvas { ctx, size in
                let sX = size.width / 280
                let sY = size.height / 200
                let sR = min(sX, sY)
                func X(_ v: CGFloat) -> CGFloat { v * sX }
                func Y(_ v: CGFloat) -> CGFloat { v * sY }
                func R(_ v: CGFloat) -> CGFloat { v * sR }
                func rect(_ x: CGFloat,_ y: CGFloat,_ w: CGFloat,_ h: CGFloat) -> CGRect {
                    CGRect(x: X(x), y: Y(y), width: X(w), height: Y(h))
                }
                func ellipse(_ x: CGFloat,_ y: CGFloat,_ w: CGFloat,_ h: CGFloat) -> CGRect {
                    CGRect(x: X(x), y: Y(y), width: X(w), height: Y(h))
                }
                func circ(_ cx: CGFloat,_ cy: CGFloat,_ r: CGFloat) -> CGRect {
                    CGRect(x: X(cx) - R(r), y: Y(cy) - R(r), width: 2 * R(r), height: 2 * R(r))
                }

                // Sky
                ctx.fill(Path(CGRect(origin: .zero, size: size)), with: .color(Color(hex: 0xE8F5E9)))

                // Sun (pulsing)
                let sunC = CGPoint(x: X(240), y: Y(38))
                ctx.drawLayer { layer in
                    layer.translateBy(x: sunC.x, y: sunC.y)
                    layer.scaleBy(x: sunScale, y: sunScale)
                    layer.translateBy(x: -sunC.x, y: -sunC.y)
                    layer.fill(Path(ellipseIn: circ(240, 38, 22)), with: .color(Color(hex: 0xFFF9C4)))
                    layer.fill(Path(ellipseIn: circ(240, 38, 15)), with: .color(Color(hex: 0xFFF176)))
                }

                // Cloud 1 (slides right)
                ctx.drawLayer { layer in
                    layer.translateBy(x: cloud1X * sX, y: 0)
                    layer.fill(Path(ellipseIn: ellipse(32, 23, 56, 24)), with: .color(.white.opacity(0.85)))
                    layer.fill(Path(ellipseIn: ellipse(62, 19, 36, 22)), with: .color(.white.opacity(0.85)))
                    layer.fill(Path(ellipseIn: ellipse(28, 23, 28, 18)), with: .color(.white.opacity(0.85)))
                }
                // Cloud 2 (slides left)
                ctx.drawLayer { layer in
                    layer.translateBy(x: cloud2X * sX, y: 0)
                    layer.fill(Path(ellipseIn: ellipse(138, 18, 44, 20)), with: .color(.white.opacity(0.70)))
                    layer.fill(Path(ellipseIn: ellipse(161, 15, 28, 18)), with: .color(.white.opacity(0.70)))
                    layer.fill(Path(ellipseIn: ellipse(136, 18, 24, 16)), with: .color(.white.opacity(0.70)))
                }

                // Ground + road
                ctx.fill(Path(CGRect(x: 0, y: Y(158), width: size.width, height: Y(42))), with: .color(Color(hex: 0xA5D6A7)))
                ctx.fill(Path(CGRect(x: 0, y: Y(146), width: size.width, height: Y(20))), with: .color(Color(hex: 0x78909C)))
                for dx in [CGFloat(20), 75, 130, 185, 240] {
                    ctx.fill(Path(roundedRect: rect(dx, 154, 30, 4), cornerRadius: R(2)),
                             with: .color(Color(hex: 0xECEFF1).opacity(0.7)))
                }

                // Trees — left
                ctx.fill(Path(rect(18, 112, 7, 36)), with: .color(Color(hex: 0x6D4C41)))
                ctx.fill(Path(ellipseIn: ellipse(3, 78, 36, 44)), with: .color(Color(hex: 0x388E3C)))
                ctx.fill(Path(ellipseIn: ellipse(8, 76, 26, 32)), with: .color(Color(hex: 0x43A047)))
                ctx.fill(Path(rect(45, 120, 6, 28)), with: .color(Color(hex: 0x6D4C41)))
                ctx.fill(Path(ellipseIn: ellipse(34, 92, 28, 36)), with: .color(Color(hex: 0x2E7D32)))
                ctx.fill(Path(ellipseIn: ellipse(38, 92, 20, 24)), with: .color(Color(hex: 0x388E3C)))

                // Trees — right
                ctx.fill(Path(rect(232, 114, 7, 34)), with: .color(Color(hex: 0x6D4C41)))
                ctx.fill(Path(ellipseIn: ellipse(217, 80, 36, 44)), with: .color(Color(hex: 0x388E3C)))
                ctx.fill(Path(ellipseIn: ellipse(222, 78, 26, 32)), with: .color(Color(hex: 0x43A047)))
                ctx.fill(Path(rect(256, 120, 6, 28)), with: .color(Color(hex: 0x6D4C41)))
                ctx.fill(Path(ellipseIn: ellipse(245, 92, 28, 36)), with: .color(Color(hex: 0x2E7D32)))

                // Car (floating)
                ctx.drawLayer { layer in
                    layer.translateBy(x: 0, y: carY * sY)
                    // Body + cabin
                    layer.fill(Path(roundedRect: rect(62, 112, 130, 34), cornerRadius: R(8)),
                               with: .color(Color(hex: 0x26C6DA)))
                    layer.fill(Path(roundedRect: rect(82, 94, 90, 28), cornerRadius: R(8)),
                               with: .color(Color(hex: 0x00ACC1)))
                    // Windows
                    layer.fill(Path(roundedRect: rect(88,  99, 32, 18), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xE0F7FA).opacity(0.9)))
                    layer.fill(Path(roundedRect: rect(126, 99, 32, 18), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xE0F7FA).opacity(0.9)))
                    // Heads
                    layer.fill(Path(ellipseIn: circ(104, 106, 7)), with: .color(Color(hex: 0xFFCC80)))
                    layer.fill(Path(ellipseIn: circ(142, 106, 7)), with: .color(Color(hex: 0xFFAB91)))
                    // Smiles
                    drawSmile(in: &layer, cx: X(104), cy: Y(108), r: R(3), color: Color(hex: 0xE64A19))
                    drawSmile(in: &layer, cx: X(142), cy: Y(108), r: R(3), color: Color(hex: 0xE64A19))
                    // Bumper + lights
                    layer.fill(Path(roundedRect: rect(62,  138, 130, 8), cornerRadius: R(4)),
                               with: .color(Color(hex: 0x0097A7)))
                    layer.fill(Path(roundedRect: rect(184, 120, 10, 8), cornerRadius: R(3)),
                               with: .color(Color(hex: 0xFFF9C4)))
                    layer.fill(Path(roundedRect: rect(64,  120, 8, 8), cornerRadius: R(3)),
                               with: .color(Color(hex: 0xEF9A9A)))
                    // Wheels — 8-spoke
                    drawWheel8(in: &layer, cx: X(92),  cy: Y(146), outer: R(14), middle: R(9), hub: R(4), angleDeg: wheelDeg)
                    drawWheel8(in: &layer, cx: X(168), cy: Y(146), outer: R(14), middle: R(9), hub: R(4), angleDeg: wheelDeg)
                }

                // Floating leaves
                drawLeaf(in: &ctx, cx: X(30),  cy: Y(60), w: R(20), h: R(12), color: Color(hex: 0x66BB6A),
                         dx: leaf1 * X(4),  dy: -leaf1 * Y(8),
                         rotDeg: -10 + leaf1 * 20)
                drawLeaf(in: &ctx, cx: X(240), cy: Y(70), w: R(18), h: R(10), color: Color(hex: 0x81C784),
                         dx: -leaf2 * X(5), dy: -leaf2 * Y(6),
                         rotDeg: 5 + leaf2 * -13)
                drawLeaf(in: &ctx, cx: X(255), cy: Y(50), w: R(14), h: R(8),  color: Color(hex: 0xA5D6A7),
                         dx: leaf3 * X(4),  dy: -leaf3 * Y(8),
                         rotDeg: -10 + leaf3 * 20)
            }
        }
    }
}

// MARK: - Slide 2 — CO₂ + leaf + carpooling car ─────────────────────────────

private struct Slide2Illustration: View {
    var body: some View {
        TimelineView(.animation) { context in
            let t = context.date.timeIntervalSinceReferenceDate

            let carBob     = -5  * sineWave(t, periodSec: 2.8)
            let wheelDeg   = sawtooth(t, periodSec: 1.0) * 360
            let leafScale  = 1 + 0.15 * sineWave(t, periodSec: 2.0)
            let smokeProg  = sawtooth(t, periodSec: 2.5)
            let smokeAlpha = 0.7 * (1 - smokeProg)
            let sp1        = sineWave(t, periodSec: 1.8)
            let sp2        = sineWave(t, periodSec: 1.8, phaseOffset: 0.33)
            let sp3        = sineWave(t, periodSec: 1.8, phaseOffset: 0.66)

            Canvas { ctx, size in
                let sX = size.width / 280
                let sY = size.height / 200
                let sR = min(sX, sY)
                func X(_ v: CGFloat) -> CGFloat { v * sX }
                func Y(_ v: CGFloat) -> CGFloat { v * sY }
                func R(_ v: CGFloat) -> CGFloat { v * sR }
                func rect(_ x: CGFloat,_ y: CGFloat,_ w: CGFloat,_ h: CGFloat) -> CGRect {
                    CGRect(x: X(x), y: Y(y), width: X(w), height: Y(h))
                }
                func circ(_ cx: CGFloat,_ cy: CGFloat,_ r: CGFloat) -> CGRect {
                    CGRect(x: X(cx) - R(r), y: Y(cy) - R(r), width: 2 * R(r), height: 2 * R(r))
                }

                // Sky
                ctx.fill(Path(CGRect(origin: .zero, size: size)), with: .color(Color(hex: 0xF1F8E9)))

                // Ground + road
                ctx.fill(Path(CGRect(x: 0, y: Y(158), width: size.width, height: Y(42))), with: .color(Color(hex: 0xC8E6C9)))
                ctx.fill(Path(CGRect(x: 0, y: Y(146), width: size.width, height: Y(18))), with: .color(Color(hex: 0x90A4AE)))
                for dx in [CGFloat(10), 60, 115, 170, 225] {
                    ctx.fill(Path(roundedRect: rect(dx, 153, 25, 3), cornerRadius: R(1.5)),
                             with: .color(.white.opacity(0.6)))
                }

                // CO₂ static cloud
                ctx.fill(Path(ellipseIn: rect(10, 46, 56, 32)), with: .color(Color(hex: 0xCFD8DC).opacity(0.55)))
                ctx.fill(Path(ellipseIn: rect(6,  46, 32, 24)), with: .color(Color(hex: 0xCFD8DC).opacity(0.55)))
                ctx.fill(Path(ellipseIn: rect(38, 44, 28, 22)), with: .color(Color(hex: 0xCFD8DC).opacity(0.55)))

                // Rising smoke particles
                ctx.fill(Path(ellipseIn: CGRect(x: X(38) - R(6), y: Y(38) - smokeProg * 30 * sY - R(6), width: 2 * R(6), height: 2 * R(6))),
                         with: .color(Color(hex: 0xB0BEC5).opacity(smokeAlpha * 0.5)))
                ctx.fill(Path(ellipseIn: CGRect(x: X(44) - R(5), y: Y(34) - smokeProg * 30 * sY - R(5), width: 2 * R(5), height: 2 * R(5))),
                         with: .color(Color(hex: 0xB0BEC5).opacity(smokeAlpha * 0.4)))
                ctx.fill(Path(ellipseIn: CGRect(x: X(32) - R(4), y: Y(36) - smokeProg * 30 * sY - R(4), width: 2 * R(4), height: 2 * R(4))),
                         with: .color(Color(hex: 0xB0BEC5).opacity(smokeAlpha * 0.3)))

                // Pulsing leaf
                let leafC = CGPoint(x: X(140), y: Y(48))
                ctx.drawLayer { layer in
                    layer.translateBy(x: leafC.x, y: leafC.y)
                    layer.scaleBy(x: leafScale, y: leafScale)
                    layer.translateBy(x: -leafC.x, y: -leafC.y)
                    layer.fill(Path(ellipseIn: rect(102, 24, 76, 48)), with: .color(Color(hex: 0x66BB6A)))
                    layer.fill(Path(ellipseIn: rect(112, 32, 56, 32)), with: .color(Color(hex: 0x81C784)))
                    var v1 = Path()
                    v1.move(to: CGPoint(x: X(110), y: Y(48)))
                    v1.addLine(to: CGPoint(x: X(170), y: Y(48)))
                    layer.stroke(v1, with: .color(Color(hex: 0x388E3C)),
                                 style: StrokeStyle(lineWidth: R(1.5), lineCap: .round))
                    var v2 = Path()
                    v2.move(to: CGPoint(x: X(140), y: Y(30)))
                    v2.addLine(to: CGPoint(x: X(140), y: Y(66)))
                    layer.stroke(v2, with: .color(Color(hex: 0x388E3C)),
                                 style: StrokeStyle(lineWidth: R(1.5), lineCap: .round))
                }

                // Sparkles
                drawSparkle(in: &ctx, cx: X(200), cy: Y(35), r: R(4), color: Color(hex: 0xFFF176), alpha: sp1)
                drawSparkle(in: &ctx, cx: X(220), cy: Y(55), r: R(3), color: Color(hex: 0xA5D6A7), alpha: sp2)
                drawSparkle(in: &ctx, cx: X(178), cy: Y(28), r: R(3), color: Color(hex: 0x80DEEA), alpha: sp3)

                // Car (bobbing)
                ctx.drawLayer { layer in
                    layer.translateBy(x: 0, y: carBob * sY)
                    layer.fill(Path(roundedRect: rect(52, 110, 148, 36), cornerRadius: R(9)),
                               with: .color(Color(hex: 0x42A5F5)))
                    layer.fill(Path(roundedRect: rect(74, 91, 104, 30), cornerRadius: R(9)),
                               with: .color(Color(hex: 0x1E88E5)))
                    layer.fill(Path(roundedRect: rect(80,  97, 26, 18), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xE3F2FD).opacity(0.92)))
                    layer.fill(Path(roundedRect: rect(112, 97, 26, 18), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xE3F2FD).opacity(0.92)))
                    layer.fill(Path(roundedRect: rect(144, 97, 26, 18), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xE3F2FD).opacity(0.92)))
                    layer.fill(Path(ellipseIn: circ(93,  104, 7)), with: .color(Color(hex: 0xFFCC80)))
                    layer.fill(Path(ellipseIn: circ(125, 104, 7)), with: .color(Color(hex: 0xFFAB91)))
                    layer.fill(Path(ellipseIn: circ(157, 104, 7)), with: .color(Color(hex: 0xCE93D8)))
                    drawSmile(in: &layer, cx: X(93),  cy: Y(106), r: R(3), color: Color(hex: 0xBF360C))
                    drawSmile(in: &layer, cx: X(125), cy: Y(106), r: R(3), color: Color(hex: 0xBF360C))
                    drawSmile(in: &layer, cx: X(157), cy: Y(106), r: R(3), color: Color(hex: 0x6A1B9A))
                    layer.fill(Path(roundedRect: rect(52,  138, 148, 8), cornerRadius: R(4)),
                               with: .color(Color(hex: 0x1565C0)))
                    layer.fill(Path(roundedRect: rect(194, 118, 8, 7), cornerRadius: R(3)),
                               with: .color(Color(hex: 0xFFF9C4)))
                    drawWheelCross(in: &layer, cx: X(90),  cy: Y(148), outer: R(13), middle: R(8), hub: R(3.5), angleDeg: wheelDeg)
                    drawWheelCross(in: &layer, cx: X(166), cy: Y(148), outer: R(13), middle: R(8), hub: R(3.5), angleDeg: wheelDeg)
                }
            }
        }
    }
}

// MARK: - Slide 3 — Night drive ─────────────────────────────────────────────

private struct Slide3Illustration: View {
    var body: some View {
        TimelineView(.animation) { context in
            let t = context.date.timeIntervalSinceReferenceDate

            let carFloat   = -4 * sineWave(t, periodSec: 3.0)
            let wheelDeg   = sawtooth(t, periodSec: 1.1) * 360
            let dashOffset = sawtooth(t, periodSec: 1.8) * 60
            let moonGlow   = 0.9 + 0.1 * sineWave(t, periodSec: 4.0)
            let s1         = 0.2 + 0.8 * sineWave(t, periodSec: 2.0)
            let s2         = 0.2 + 0.8 * sineWave(t, periodSec: 2.0, phaseOffset: 0.20)
            let s3         = 0.2 + 0.8 * sineWave(t, periodSec: 2.0, phaseOffset: 0.40)
            let s4         = 0.2 + 0.8 * sineWave(t, periodSec: 2.0, phaseOffset: 0.60)
            let s5         = 0.2 + 0.8 * sineWave(t, periodSec: 2.0, phaseOffset: 0.80)

            Canvas { ctx, size in
                let sX = size.width / 280
                let sY = size.height / 200
                let sR = min(sX, sY)
                func X(_ v: CGFloat) -> CGFloat { v * sX }
                func Y(_ v: CGFloat) -> CGFloat { v * sY }
                func R(_ v: CGFloat) -> CGFloat { v * sR }
                func rect(_ x: CGFloat,_ y: CGFloat,_ w: CGFloat,_ h: CGFloat) -> CGRect {
                    CGRect(x: X(x), y: Y(y), width: X(w), height: Y(h))
                }
                func circ(_ cx: CGFloat,_ cy: CGFloat,_ r: CGFloat) -> CGRect {
                    CGRect(x: X(cx) - R(r), y: Y(cy) - R(r), width: 2 * R(r), height: 2 * R(r))
                }

                // Night sky
                ctx.fill(Path(CGRect(origin: .zero, size: size)), with: .color(Color(hex: 0x1A237E)))
                ctx.fill(Path(CGRect(x: 0, y: Y(100), width: size.width, height: Y(100))),
                         with: .color(Color(hex: 0x283593)))

                // Horizon glow
                ctx.fill(Path(ellipseIn: rect(-20, 100, 320, 80)),
                         with: .color(Color(hex: 0x00796B).opacity(0.35)))

                // Moon
                ctx.fill(Path(ellipseIn: circ(230, 40, 24)), with: .color(Color(hex: 0xFFF9C4).opacity(0.15 * moonGlow)))
                ctx.fill(Path(ellipseIn: circ(230, 40, 18)), with: .color(Color(hex: 0xFFF9C4).opacity(0.20 * moonGlow)))
                ctx.fill(Path(ellipseIn: circ(230, 40, 13)), with: .color(Color(hex: 0xFFFDE7)))

                // Stars
                let stars: [(CGFloat, CGFloat, CGFloat, CGFloat)] = [
                    (30, 22, 2, s1), (68, 14, 1.5, s2),
                    (110, 30, 2, s3), (155, 18, 1.5, s4),
                    (185, 35, 2, s5), (50, 45, 1.5, s1),
                    (200, 20, 1.5, s2),
                ]
                for (cx, cy, r, alpha) in stars {
                    ctx.fill(Path(ellipseIn: circ(cx, cy, r)), with: .color(.white.opacity(alpha)))
                }

                // Hill silhouettes
                ctx.fill(Path(ellipseIn: rect(-15, 110, 110, 70)), with: .color(Color(hex: 0x1B5E20)))
                ctx.fill(Path(ellipseIn: rect(185, 118, 110, 60)), with: .color(Color(hex: 0x1B5E20)))
                ctx.fill(Path(ellipseIn: rect(60,  124, 160, 56)), with: .color(Color(hex: 0x1B5E20)))

                // Trees — left
                ctx.fill(Path(rect(12, 110, 6, 30)), with: .color(Color(hex: 0x0D3B0D)))
                var triL = Path()
                triL.move(to: CGPoint(x: X(15), y: Y(85)))
                triL.addLine(to: CGPoint(x: X(3), y: Y(118)))
                triL.addLine(to: CGPoint(x: X(27), y: Y(118)))
                triL.closeSubpath()
                ctx.fill(triL, with: .color(Color(hex: 0x0D3B0D)))

                // Trees — right
                ctx.fill(Path(rect(255, 112, 6, 28)), with: .color(Color(hex: 0x0D3B0D)))
                var triR = Path()
                triR.move(to: CGPoint(x: X(258), y: Y(88)))
                triR.addLine(to: CGPoint(x: X(246), y: Y(120)))
                triR.addLine(to: CGPoint(x: X(270), y: Y(120)))
                triR.closeSubpath()
                ctx.fill(triR, with: .color(Color(hex: 0x0D3B0D)))

                // Road
                ctx.fill(Path(CGRect(x: 0, y: Y(148), width: size.width, height: Y(28))),
                         with: .color(Color(hex: 0x37474F)))

                // Animated road dashes
                for startX in stride(from: CGFloat(-60), through: 300, by: 60) {
                    let ox = (startX - dashOffset) * sX
                    if ox + X(40) > 0 && ox < size.width {
                        ctx.fill(Path(roundedRect: CGRect(x: ox, y: Y(159), width: X(40), height: Y(4)), cornerRadius: R(2)),
                                 with: .color(Color(hex: 0xECEFF1).opacity(0.5)))
                    }
                }

                // Car (floating)
                ctx.drawLayer { layer in
                    layer.translateBy(x: 0, y: carFloat * sY)
                    layer.fill(Path(roundedRect: rect(68, 118, 130, 34), cornerRadius: R(9)),
                               with: .color(Color(hex: 0x00897B)))
                    layer.fill(Path(roundedRect: rect(88, 100, 90, 28), cornerRadius: R(9)),
                               with: .color(Color(hex: 0x00695C)))
                    layer.fill(Path(roundedRect: rect(94,  105, 30, 17), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xB2EBF2).opacity(0.85)))
                    layer.fill(Path(roundedRect: rect(130, 105, 30, 17), cornerRadius: R(4)),
                               with: .color(Color(hex: 0xB2EBF2).opacity(0.85)))
                    layer.fill(Path(ellipseIn: circ(109, 112, 7)), with: .color(Color(hex: 0xFFCC80)))
                    layer.fill(Path(ellipseIn: circ(145, 112, 7)), with: .color(Color(hex: 0xFFAB91)))
                    drawSmile(in: &layer, cx: X(109), cy: Y(114), r: R(3), color: Color(hex: 0xE64A19))
                    drawSmile(in: &layer, cx: X(145), cy: Y(114), r: R(3), color: Color(hex: 0xE64A19))
                    layer.fill(Path(roundedRect: rect(68,  144, 130, 8), cornerRadius: R(4)),
                               with: .color(Color(hex: 0x004D40)))
                    layer.fill(Path(roundedRect: rect(192, 124, 10, 8), cornerRadius: R(3)),
                               with: .color(Color(hex: 0xFFF9C4)))
                    layer.fill(Path(roundedRect: rect(68,  124, 8, 8), cornerRadius: R(3)),
                               with: .color(Color(hex: 0xEF5350)))
                    drawWheelCross(in: &layer, cx: X(98),  cy: Y(152), outer: R(12), middle: R(7), hub: R(3), angleDeg: wheelDeg)
                    drawWheelCross(in: &layer, cx: X(170), cy: Y(152), outer: R(12), middle: R(7), hub: R(3), angleDeg: wheelDeg)
                }
            }
        }
    }
}

// MARK: - Drawing helpers ────────────────────────────────────────────────────

private func drawSmile(in ctx: inout GraphicsContext, cx: CGFloat, cy: CGFloat, r: CGFloat, color: Color) {
    var p = Path()
    p.move(to: CGPoint(x: cx - r, y: cy))
    p.addQuadCurve(to: CGPoint(x: cx + r, y: cy), control: CGPoint(x: cx, y: cy + r))
    ctx.stroke(p, with: .color(color), style: StrokeStyle(lineWidth: r * 0.4, lineCap: .round))
}

private func drawSparkle(in ctx: inout GraphicsContext, cx: CGFloat, cy: CGFloat, r: CGFloat, color: Color, alpha: CGFloat) {
    let c = color.opacity(alpha)
    let arm = r * 2.2
    let sw = r * 0.55
    var v = Path()
    v.move(to: CGPoint(x: cx, y: cy - arm))
    v.addLine(to: CGPoint(x: cx, y: cy + arm))
    ctx.stroke(v, with: .color(c), style: StrokeStyle(lineWidth: sw, lineCap: .round))
    var h = Path()
    h.move(to: CGPoint(x: cx - arm, y: cy))
    h.addLine(to: CGPoint(x: cx + arm, y: cy))
    ctx.stroke(h, with: .color(c), style: StrokeStyle(lineWidth: sw, lineCap: .round))
    ctx.fill(Path(ellipseIn: CGRect(x: cx - r, y: cy - r, width: 2 * r, height: 2 * r)), with: .color(c))
}

private func drawWheel8(in ctx: inout GraphicsContext, cx: CGFloat, cy: CGFloat, outer: CGFloat, middle: CGFloat, hub: CGFloat, angleDeg: CGFloat) {
    let outerColor = Color(hex: 0x37474F)
    let middleColor = Color(hex: 0x546E7A)
    let hubColor = Color(hex: 0xB0BEC5)
    ctx.fill(Path(ellipseIn: CGRect(x: cx - outer, y: cy - outer, width: 2 * outer, height: 2 * outer)),
             with: .color(outerColor))
    let sw2 = outer * 0.14
    let sw15 = outer * 0.10
    for i in 0..<2 {
        let a = (angleDeg + CGFloat(i) * 90) * .pi / 180
        let dx = outer * cos(a), dy = outer * sin(a)
        var p = Path()
        p.move(to: CGPoint(x: cx - dx, y: cy - dy))
        p.addLine(to: CGPoint(x: cx + dx, y: cy + dy))
        ctx.stroke(p, with: .color(outerColor), style: StrokeStyle(lineWidth: sw2, lineCap: .round))
    }
    for i in 0..<2 {
        let a = (angleDeg + 45 + CGFloat(i) * 90) * .pi / 180
        let dx = outer * cos(a), dy = outer * sin(a)
        var p = Path()
        p.move(to: CGPoint(x: cx - dx, y: cy - dy))
        p.addLine(to: CGPoint(x: cx + dx, y: cy + dy))
        ctx.stroke(p, with: .color(outerColor), style: StrokeStyle(lineWidth: sw15, lineCap: .round))
    }
    ctx.fill(Path(ellipseIn: CGRect(x: cx - middle, y: cy - middle, width: 2 * middle, height: 2 * middle)),
             with: .color(middleColor))
    ctx.fill(Path(ellipseIn: CGRect(x: cx - hub, y: cy - hub, width: 2 * hub, height: 2 * hub)),
             with: .color(hubColor))
}

private func drawWheelCross(in ctx: inout GraphicsContext, cx: CGFloat, cy: CGFloat, outer: CGFloat, middle: CGFloat, hub: CGFloat, angleDeg: CGFloat) {
    let outerColor = Color(hex: 0x37474F)
    let middleColor = Color(hex: 0x546E7A)
    let hubColor = Color(hex: 0xB0BEC5)
    ctx.fill(Path(ellipseIn: CGRect(x: cx - outer, y: cy - outer, width: 2 * outer, height: 2 * outer)),
             with: .color(outerColor))
    let sw = outer * 0.15
    for i in 0..<2 {
        let a = (angleDeg + CGFloat(i) * 90) * .pi / 180
        let dx = outer * cos(a), dy = outer * sin(a)
        var p = Path()
        p.move(to: CGPoint(x: cx - dx, y: cy - dy))
        p.addLine(to: CGPoint(x: cx + dx, y: cy + dy))
        ctx.stroke(p, with: .color(outerColor), style: StrokeStyle(lineWidth: sw, lineCap: .round))
    }
    ctx.fill(Path(ellipseIn: CGRect(x: cx - middle, y: cy - middle, width: 2 * middle, height: 2 * middle)),
             with: .color(middleColor))
    ctx.fill(Path(ellipseIn: CGRect(x: cx - hub, y: cy - hub, width: 2 * hub, height: 2 * hub)),
             with: .color(hubColor))
}

private func drawLeaf(in ctx: inout GraphicsContext, cx: CGFloat, cy: CGFloat, w: CGFloat, h: CGFloat, color: Color, dx: CGFloat, dy: CGFloat, rotDeg: CGFloat) {
    ctx.drawLayer { layer in
        layer.translateBy(x: dx, y: dy)
        layer.translateBy(x: cx, y: cy)
        layer.rotate(by: .degrees(Double(rotDeg)))
        layer.translateBy(x: -cx, y: -cy)
        layer.fill(Path(ellipseIn: CGRect(x: cx - w / 2, y: cy - h / 2, width: w, height: h)),
                   with: .color(color))
    }
}

// MARK: - Previews

#Preview("Slide 1") { OnboardingIllustration(index: 0).frame(width: 280, height: 200).background(Color.white) }
#Preview("Slide 2") { OnboardingIllustration(index: 1).frame(width: 280, height: 200).background(Color.white) }
#Preview("Slide 3") { OnboardingIllustration(index: 2).frame(width: 280, height: 200).background(Color.white) }

import SwiftUI

// MARK: — Splash screen ───────────────────────────────────────────────────────
//
// Shown for ~3 seconds on cold start while AuthViewModel performs silent
// session restore. Mirrors `SplashScreen.kt` 1:1 — the same animated
// scene (sun, clouds, bird, buildings, road with dashes, swaying trees,
// rolling car with passengers, dark logo card, fading wordmark + taglines
// + pulsing dot). All drawing is done with `Canvas` driven by a
// `TimelineView(.animation)` clock so animations run continuously.

// ── Colour palette (matches SplashScreen.kt exactly) ──────────────────────────
private let SkyBlue     = Color(red: 0xE3/255, green: 0xF2/255, blue: 0xFD/255)
private let SunOuter    = Color(red: 0xFF/255, green: 0xF9/255, blue: 0xC4/255)
private let SunInner    = Color(red: 0xFF/255, green: 0xF1/255, blue: 0x76/255)
private let BirdGrey    = Color(red: 0x78/255, green: 0x90/255, blue: 0x9C/255)
private let BldBg1      = Color(red: 0xB2/255, green: 0xDF/255, blue: 0xDB/255)
private let BldBg2      = Color(red: 0xA5/255, green: 0xD6/255, blue: 0xA7/255)
private let BldMid1     = Color(red: 0x80/255, green: 0xCB/255, blue: 0xC4/255)
private let BldMid2     = Color(red: 0x4D/255, green: 0xB6/255, blue: 0xAC/255)
private let Ground1     = Color(red: 0xA5/255, green: 0xD6/255, blue: 0xA7/255)
private let GroundEll   = Color(red: 0x81/255, green: 0xC7/255, blue: 0x84/255)
private let RoadColor   = Color(red: 0x54/255, green: 0x6E/255, blue: 0x7A/255)
private let DashColor   = Color(red: 0xEC/255, green: 0xEF/255, blue: 0xF1/255)
private let TrunkColor  = Color(red: 0x5D/255, green: 0x40/255, blue: 0x37/255)
private let Leaf1       = Color(red: 0x38/255, green: 0x8E/255, blue: 0x3C/255)
private let Leaf2       = Color(red: 0x43/255, green: 0xA0/255, blue: 0x47/255)
private let Leaf3       = Color(red: 0x2E/255, green: 0x7D/255, blue: 0x32/255)
private let CarBody1    = Color(red: 0x26/255, green: 0xC6/255, blue: 0xDA/255)
private let CarBody2    = Color(red: 0x00/255, green: 0xAC/255, blue: 0xC1/255)
private let CarWindow   = Color(red: 0xE0/255, green: 0xF7/255, blue: 0xFA/255)
private let CarStripe   = Color(red: 0x00/255, green: 0x97/255, blue: 0xA7/255)
private let CarLightF   = Color(red: 0xFF/255, green: 0xF9/255, blue: 0xC4/255)
private let CarLightR   = Color(red: 0xEF/255, green: 0x9A/255, blue: 0x9A/255)
private let PassL       = Color(red: 0xFF/255, green: 0xCC/255, blue: 0x80/255)
private let PassR       = Color(red: 0xFF/255, green: 0xAB/255, blue: 0x91/255)
private let SmileColor  = Color(red: 0xE6/255, green: 0x4A/255, blue: 0x19/255)
private let WheelOut    = Color(red: 0x37/255, green: 0x47/255, blue: 0x4F/255)
private let WheelMid    = Color(red: 0x78/255, green: 0x90/255, blue: 0x9C/255)
private let WheelHub    = Color(red: 0xB0/255, green: 0xBE/255, blue: 0xC5/255)
private let WheelSpk    = Color(red: 0x54/255, green: 0x6E/255, blue: 0x7A/255)
private let Grass1      = Color(red: 0x66/255, green: 0xBB/255, blue: 0x6A/255)
private let Grass2      = Color(red: 0x81/255, green: 0xC7/255, blue: 0x84/255)
private let FlowerYel   = Color(red: 0xFF/255, green: 0xF9/255, blue: 0xC4/255)
private let Overlay     = Color(red: 0x1B/255, green: 0x43/255, blue: 0x32/255)
private let LogoWhite   = Color.white
private let LogoAccent  = Color(red: 0x52/255, green: 0xB7/255, blue: 0x88/255)
private let Tag1Color   = Color(red: 0x95/255, green: 0xD5/255, blue: 0xB2/255)
private let Tag2Color   = Color(red: 0x52/255, green: 0xB7/255, blue: 0x88/255)
private let DotColor    = Color(red: 0x52/255, green: 0xB7/255, blue: 0x88/255)

// SVG reference dimensions (matches Android `VW`/`VH`).
private let VW: CGFloat = 320
private let VH: CGFloat = 703

// MARK: — Easing helpers ──────────────────────────────────────────────────────

/// Approximation of Material's FastOutSlowIn (cubic-bezier 0.4, 0, 0.2, 1).
/// Standard easeInOutCubic is visually indistinguishable for these animations.
private func fastOutSlowIn(_ t: Double) -> Double {
    let x = max(0.0, min(1.0, t))
    return x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2
}

private func linearEase(_ t: Double) -> Double { max(0, min(1, t)) }

/// Reverse-repeating triangle wave. Returns eased value in [0,1] that
/// goes 0→1 over `duration` then 1→0 over `duration` and loops forever.
private func triangle(_ time: Double, duration: Double, easing: (Double) -> Double) -> Double {
    let period = duration * 2
    let t = time.truncatingRemainder(dividingBy: period) / duration
    return t < 1 ? easing(t) : easing(2 - t)
}

/// Linear loop 0→1 over `duration`, restarting (no reverse).
private func loop(_ time: Double, duration: Double) -> Double {
    return time.truncatingRemainder(dividingBy: duration) / duration
}

/// One-shot eased ramp 0→1 starting at `delay`, lasting `duration`.
private func oneShot(elapsed: Double, delay: Double, duration: Double,
                     easing: (Double) -> Double = fastOutSlowIn) -> Double {
    let t = (elapsed - delay) / duration
    if t <= 0 { return 0 }
    if t >= 1 { return 1 }
    return easing(t)
}

// MARK: — Splash view ─────────────────────────────────────────────────────────

struct SplashView: View {
    /// Optional callback invoked after ~3 seconds (parity with `SplashRoute`).
    var onComplete: (() -> Void)? = nil

    @State private var startDate = Date()
    @State private var didComplete = false

    var body: some View {
        TimelineView(.animation) { timeline in
            let elapsed = timeline.date.timeIntervalSince(startDate)

            Canvas { context, size in
                drawScene(context: context, size: size, elapsed: elapsed)
            }
            .ignoresSafeArea()
        }
        .background(SkyBlue.ignoresSafeArea())
        .onAppear {
            startDate = Date()
            didComplete = false
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                guard !didComplete else { return }
                didComplete = true
                onComplete?()
            }
        }
    }

    // MARK: — Scene drawing ────────────────────────────────────────────────────

    private func drawScene(context: GraphicsContext, size: CGSize, elapsed: Double) {
        let sw = size.width
        let sh = size.height
        let sx = sw / VW
        let sy = sh / VH

        @inline(__always) func f(_ x: CGFloat) -> CGFloat { x * sx }
        @inline(__always) func g(_ y: CGFloat) -> CGFloat { y * sy }

        // ── Animated values ───────────────────────────────────────────────────
        let cloudLX    = lerp(0,  18, triangle(elapsed, duration: 6.0, easing: fastOutSlowIn))
        let cloudRX    = lerp(0, -14, triangle(elapsed, duration: 7.0, easing: fastOutSlowIn))
        let carBob     = lerp(0,  -3, triangle(elapsed, duration: 0.8, easing: fastOutSlowIn))
        let wheelAngle = loop(elapsed, duration: 0.9) * 360.0
        let roadOff    = lerp(0, -80, loop(elapsed, duration: 1.0))
        let treeLAngle = lerp(-2,  2, triangle(elapsed, duration: 3.0, easing: fastOutSlowIn))
        let treeRAngle = lerp( 2, -2, triangle(elapsed, duration: 3.4, easing: fastOutSlowIn))
        let sunScale   = lerp(1, 1.05, triangle(elapsed, duration: 4.0, easing: fastOutSlowIn))
        let dotScale   = lerp(1, 1.4, triangle(elapsed, duration: 0.7, easing: fastOutSlowIn))
        let dotAlpha   = lerp(0.6, 1, triangle(elapsed, duration: 0.7, easing: fastOutSlowIn))
        let birdX      = lerp(0,  60, triangle(elapsed, duration: 5.0, easing: fastOutSlowIn))

        // One-shot entry animations (start fires immediately on first frame).
        let logoAlpha = oneShot(elapsed: elapsed, delay: 0.4, duration: 1.2)
        let logoOffY  = lerp(12, 0, oneShot(elapsed: elapsed, delay: 0.4, duration: 1.2))
        let tag1Alpha = oneShot(elapsed: elapsed, delay: 1.2, duration: 1.0, easing: linearEase)
        let tag2Alpha = oneShot(elapsed: elapsed, delay: 1.6, duration: 1.0, easing: linearEase)

        // ── Sky ───────────────────────────────────────────────────────────────
        context.fill(Path(CGRect(origin: .zero, size: size)), with: .color(SkyBlue))

        // ── Sun ───────────────────────────────────────────────────────────────
        drawSun(context: context, cx: f(260), cy: g(90), sx: sx, scale: sunScale)

        // ── Bird ──────────────────────────────────────────────────────────────
        var birdCtx = context
        birdCtx.translateBy(x: f(CGFloat(birdX)), y: 0)
        drawBird(context: birdCtx, sx: sx, sy: sy)

        // ── Cloud left ────────────────────────────────────────────────────────
        var cloudL = context
        cloudL.translateBy(x: f(CGFloat(cloudLX)), y: 0)
        cloudL.fill(Path(ellipseIn: CGRect(x: f(32), y: g(50),  width: f(76), height: g(36))),
                    with: .color(.white.opacity(0.92)))
        cloudL.fill(Path(ellipseIn: CGRect(x: f(74), y: g(44),  width: f(52), height: g(32))),
                    with: .color(.white.opacity(0.92)))
        cloudL.fill(Path(ellipseIn: CGRect(x: f(26), y: g(51),  width: f(40), height: g(26))),
                    with: .color(.white.opacity(0.92)))

        // ── Cloud right ───────────────────────────────────────────────────────
        var cloudR = context
        cloudR.translateBy(x: f(CGFloat(cloudRX)), y: 0)
        cloudR.fill(Path(ellipseIn: CGRect(x: f(168), y: g(37), width: f(64), height: g(30))),
                    with: .color(.white.opacity(0.75)))
        cloudR.fill(Path(ellipseIn: CGRect(x: f(200), y: g(31), width: f(44), height: g(26))),
                    with: .color(.white.opacity(0.75)))
        cloudR.fill(Path(ellipseIn: CGRect(x: f(162), y: g(40), width: f(32), height: g(20))),
                    with: .color(.white.opacity(0.75)))

        // ── Background buildings ──────────────────────────────────────────────
        let bgRects: [(CGFloat, CGFloat, CGFloat, CGFloat, Color)] = [
            (10,  210, 28, 120, BldBg1.opacity(0.55)),
            (42,  195, 22, 135, BldBg2.opacity(0.50)),
            (68,  220, 18, 110, BldBg1.opacity(0.50)),
            (240, 200, 24, 130, BldBg2.opacity(0.50)),
            (268, 215, 30, 115, BldBg1.opacity(0.50)),
            (292, 205, 22, 125, BldBg2.opacity(0.50)),
        ]
        for (x, y, w, h, c) in bgRects {
            let rect = CGRect(x: f(x), y: g(y), width: f(w), height: g(h))
            context.fill(Path(roundedRect: rect, cornerRadius: f(3)), with: .color(c))
        }
        for (wx, wy) in [(16.0,222.0),(26.0,222.0),(16.0,234.0),(246.0,212.0),(256.0,212.0)] {
            let rect = CGRect(x: f(CGFloat(wx)), y: g(CGFloat(wy)), width: f(6), height: g(5))
            context.fill(Path(roundedRect: rect, cornerRadius: f(1)),
                         with: .color(.white.opacity(0.45)))
        }

        // ── Mid buildings ─────────────────────────────────────────────────────
        let midRects: [(CGFloat, CGFloat, CGFloat, CGFloat, Color)] = [
            (0,   258, 40, 112, BldMid1),
            (44,  246, 34, 124, BldMid2),
            (82,  266, 28, 104, BldMid1),
            (210, 253, 36, 117, BldMid2),
            (250, 263, 30, 107, BldMid1),
            (284, 248, 36, 122, BldMid2),
        ]
        for (x, y, w, h, c) in midRects {
            let rect = CGRect(x: f(x), y: g(y), width: f(w), height: g(h))
            context.fill(Path(roundedRect: rect, cornerRadius: f(4)), with: .color(c))
        }
        for (wx, wy) in [(6.0,270.0),(18.0,270.0),(6.0,284.0),(18.0,284.0),
                         (50.0,258.0),(64.0,258.0),(216.0,264.0),(230.0,264.0)] {
            let rect = CGRect(x: f(CGFloat(wx)), y: g(CGFloat(wy)), width: f(8), height: g(7))
            context.fill(Path(roundedRect: rect, cornerRadius: f(1)),
                         with: .color(.white.opacity(0.55)))
        }

        // ── Ground ────────────────────────────────────────────────────────────
        context.fill(Path(CGRect(x: 0, y: g(358), width: sw, height: sh - g(358))),
                     with: .color(Ground1))
        context.fill(Path(ellipseIn: CGRect(x: f(-60), y: g(322),
                                            width: f(440), height: g(76))),
                     with: .color(GroundEll))

        // ── Road ──────────────────────────────────────────────────────────────
        context.fill(Path(CGRect(x: 0, y: g(390), width: sw, height: g(36))),
                     with: .color(RoadColor))

        // Scrolling centre dashes (clipped to road band).
        var dashCtx = context
        dashCtx.clip(to: Path(CGRect(x: 0, y: g(390), width: sw, height: g(36))))
        for i in -1...6 {
            let dx = f(CGFloat(i) * 80) + f(CGFloat(roadOff))
            let rect = CGRect(x: dx, y: g(405), width: f(44), height: g(5))
            dashCtx.fill(Path(roundedRect: rect, cornerRadius: g(2.5)),
                         with: .color(DashColor.opacity(0.65)))
        }

        // ── Trees left ────────────────────────────────────────────────────────
        var treeL = context
        treeL.translateBy(x: f(18), y: g(370))
        treeL.rotate(by: .degrees(treeLAngle))
        treeL.translateBy(x: -f(18), y: -g(370))
        treeL.fill(Path(CGRect(x: f(14),  y: g(338), width: f(9),  height: g(54))), with: .color(TrunkColor))
        treeL.fill(Path(ellipseIn: CGRect(x: f(-6),  y: g(292), width: f(48), height: g(56))), with: .color(Leaf1))
        treeL.fill(Path(ellipseIn: CGRect(x: f(1),   y: g(288), width: f(34), height: g(40))), with: .color(Leaf2))
        treeL.fill(Path(CGRect(x: f(50),  y: g(350), width: f(8),  height: g(42))), with: .color(TrunkColor))
        treeL.fill(Path(ellipseIn: CGRect(x: f(35),  y: g(311), width: f(38), height: g(46))), with: .color(Leaf3))
        treeL.fill(Path(ellipseIn: CGRect(x: f(40),  y: g(307), width: f(28), height: g(34))), with: .color(Leaf1))

        // ── Trees right ───────────────────────────────────────────────────────
        var treeR = context
        treeR.translateBy(x: f(294), y: g(370))
        treeR.rotate(by: .degrees(treeRAngle))
        treeR.translateBy(x: -f(294), y: -g(370))
        treeR.fill(Path(CGRect(x: f(290), y: g(340), width: f(9),  height: g(52))), with: .color(TrunkColor))
        treeR.fill(Path(ellipseIn: CGRect(x: f(270), y: g(294), width: f(48), height: g(56))), with: .color(Leaf1))
        treeR.fill(Path(ellipseIn: CGRect(x: f(277), y: g(290), width: f(34), height: g(40))), with: .color(Leaf2))
        treeR.fill(Path(CGRect(x: f(257), y: g(348), width: f(8),  height: g(44))), with: .color(TrunkColor))
        treeR.fill(Path(ellipseIn: CGRect(x: f(242), y: g(309), width: f(38), height: g(46))), with: .color(Leaf3))
        treeR.fill(Path(ellipseIn: CGRect(x: f(247), y: g(305), width: f(28), height: g(34))), with: .color(Leaf1))

        // ── Car (with bob) ────────────────────────────────────────────────────
        var carCtx = context
        carCtx.translateBy(x: 0, y: g(CGFloat(carBob)))
        drawCar(context: carCtx, sx: sx, sy: sy, wheelAngle: wheelAngle)

        // ── Foreground grass ──────────────────────────────────────────────────
        context.fill(Path(CGRect(x: 0, y: g(422), width: sw, height: sh - g(422))),
                     with: .color(Grass1))
        context.fill(Path(ellipseIn: CGRect(x: f(-60), y: g(400),
                                            width: f(440), height: g(44))),
                     with: .color(Grass2))

        // ── Small flowers ─────────────────────────────────────────────────────
        let flowers: [(CGFloat, CGFloat, CGFloat, Color)] = [
            (40,  436, 4, FlowerYel),
            (280, 440, 3, FlowerYel),
            (72,  446, 3, .white.opacity(0.7)),
            (248, 432, 4, .white.opacity(0.7)),
        ]
        for (cx, cy, r, c) in flowers {
            let rr = f(r)
            let rect = CGRect(x: f(cx) - rr, y: g(cy) - rr, width: rr * 2, height: rr * 2)
            context.fill(Path(ellipseIn: rect), with: .color(c))
        }

        // ── Dark overlay (logo card) ──────────────────────────────────────────
        context.fill(Path(CGRect(x: 0, y: g(472), width: sw, height: sh - g(472))),
                     with: .color(Overlay.opacity(0.94)))

        // ── Logo (fade + slide in) ────────────────────────────────────────────
        var logoCtx = context
        logoCtx.translateBy(x: 0, y: g(CGFloat(logoOffY)))
        drawLogo(context: logoCtx, sx: sx, sy: sy, alpha: logoAlpha)

        // ── Tagline 1 ─────────────────────────────────────────────────────────
        let tag1 = Text("SHARE THE RIDE")
            .font(.system(size: 11, weight: .light))
            .kerning(2)
            .foregroundColor(Tag1Color.opacity(tag1Alpha))
        context.draw(tag1, at: CGPoint(x: f(160), y: g(584)), anchor: .center)

        // ── Tagline 2 ─────────────────────────────────────────────────────────
        let tag2 = Text("less carbon · more journey")
            .font(.system(size: 9, weight: .light))
            .kerning(1)
            .foregroundColor(Tag2Color.opacity(tag2Alpha))
        context.draw(tag2, at: CGPoint(x: f(160), y: g(606)), anchor: .center)

        // ── Pulsing loading dot ───────────────────────────────────────────────
        let dotR = f(4) * CGFloat(dotScale)
        let dotRect = CGRect(x: f(160) - dotR, y: g(648) - dotR,
                             width: dotR * 2, height: dotR * 2)
        context.fill(Path(ellipseIn: dotRect), with: .color(DotColor.opacity(dotAlpha)))
    }

    // MARK: — Drawing helpers ─────────────────────────────────────────────────

    private func drawSun(context: GraphicsContext, cx: CGFloat, cy: CGFloat,
                         sx: CGFloat, scale: Double) {
        let r1 = 38 * sx * CGFloat(scale)
        let r2 = 26 * sx * CGFloat(scale)
        context.fill(Path(ellipseIn: CGRect(x: cx - r1, y: cy - r1, width: r1 * 2, height: r1 * 2)),
                     with: .color(SunOuter))
        context.fill(Path(ellipseIn: CGRect(x: cx - r2, y: cy - r2, width: r2 * 2, height: r2 * 2)),
                     with: .color(SunInner))
    }

    private func drawBird(context: GraphicsContext, sx: CGFloat, sy: CGFloat) {
        @inline(__always) func f(_ x: CGFloat) -> CGFloat { x * sx }
        @inline(__always) func g(_ y: CGFloat) -> CGFloat { y * sy }

        var arc1 = Path()
        arc1.move(to: CGPoint(x: f(50), y: g(120)))
        arc1.addQuadCurve(to: CGPoint(x: f(58), y: g(120)),
                          control: CGPoint(x: f(54), y: g(116)))
        var arc2 = Path()
        arc2.move(to: CGPoint(x: f(62), y: g(118)))
        arc2.addQuadCurve(to: CGPoint(x: f(70), y: g(118)),
                          control: CGPoint(x: f(66), y: g(114)))
        let style = StrokeStyle(lineWidth: f(1.5), lineCap: .round)
        context.stroke(arc1, with: .color(BirdGrey.opacity(0.6)), style: style)
        context.stroke(arc2, with: .color(BirdGrey.opacity(0.6)), style: style)
    }

    private func drawCar(context: GraphicsContext, sx: CGFloat, sy: CGFloat,
                         wheelAngle: Double) {
        @inline(__always) func f(_ x: CGFloat) -> CGFloat { x * sx }
        @inline(__always) func g(_ y: CGFloat) -> CGFloat { y * sy }

        // Body + roof
        context.fill(Path(roundedRect: CGRect(x: f(88),  y: g(356), width: f(144), height: g(38)),
                          cornerRadius: f(10)), with: .color(CarBody1))
        context.fill(Path(roundedRect: CGRect(x: f(106), y: g(333), width: f(100), height: g(34)),
                          cornerRadius: f(10)), with: .color(CarBody2))

        // Windows
        context.fill(Path(roundedRect: CGRect(x: f(114), y: g(339), width: f(36), height: g(22)),
                          cornerRadius: f(5)), with: .color(CarWindow.opacity(0.9)))
        context.fill(Path(roundedRect: CGRect(x: f(156), y: g(339), width: f(36), height: g(22)),
                          cornerRadius: f(5)), with: .color(CarWindow.opacity(0.9)))

        // Passengers
        let r = f(9)
        context.fill(Path(ellipseIn: CGRect(x: f(132) - r, y: g(348) - r, width: r * 2, height: r * 2)),
                     with: .color(PassL))
        context.fill(Path(ellipseIn: CGRect(x: f(174) - r, y: g(348) - r, width: r * 2, height: r * 2)),
                     with: .color(PassR))

        // Smiles
        let smileStyle = StrokeStyle(lineWidth: f(1.3), lineCap: .round)
        var smile1 = Path()
        smile1.move(to: CGPoint(x: f(129), y: g(350)))
        smile1.addQuadCurve(to: CGPoint(x: f(135), y: g(350)),
                            control: CGPoint(x: f(132), y: g(354)))
        var smile2 = Path()
        smile2.move(to: CGPoint(x: f(171), y: g(350)))
        smile2.addQuadCurve(to: CGPoint(x: f(177), y: g(350)),
                            control: CGPoint(x: f(174), y: g(354)))
        context.stroke(smile1, with: .color(SmileColor), style: smileStyle)
        context.stroke(smile2, with: .color(SmileColor), style: smileStyle)

        // Under-body stripe + lights
        context.fill(Path(roundedRect: CGRect(x: f(88),  y: g(386), width: f(144), height: g(8)),
                          cornerRadius: f(4)), with: .color(CarStripe))
        context.fill(Path(roundedRect: CGRect(x: f(224), y: g(364), width: f(10),  height: g(9)),
                          cornerRadius: f(3)), with: .color(CarLightF))
        context.fill(Path(roundedRect: CGRect(x: f(90),  y: g(364), width: f(8),   height: g(9)),
                          cornerRadius: f(3)), with: .color(CarLightR))

        // Wheels (rotated independently)
        drawWheel(context: context, cx: f(116), cy: g(394), sx: sx, angle: wheelAngle)
        drawWheel(context: context, cx: f(204), cy: g(394), sx: sx, angle: wheelAngle)
    }

    private func drawWheel(context: GraphicsContext, cx: CGFloat, cy: CGFloat,
                           sx: CGFloat, angle: Double) {
        let rOut = sx * 16
        let rMid = sx * 9
        let rHub = sx * 4
        context.fill(Path(ellipseIn: CGRect(x: cx - rOut, y: cy - rOut, width: rOut * 2, height: rOut * 2)),
                     with: .color(WheelOut))
        context.fill(Path(ellipseIn: CGRect(x: cx - rMid, y: cy - rMid, width: rMid * 2, height: rMid * 2)),
                     with: .color(WheelMid))
        context.fill(Path(ellipseIn: CGRect(x: cx - rHub, y: cy - rHub, width: rHub * 2, height: rHub * 2)),
                     with: .color(WheelHub))

        // Spokes (rotated)
        var spokeCtx = context
        spokeCtx.translateBy(x: cx, y: cy)
        spokeCtx.rotate(by: .degrees(angle))
        let len = sx * 11
        let stroke = StrokeStyle(lineWidth: sx * 2.5)
        var v = Path()
        v.move(to: CGPoint(x: 0, y: -len))
        v.addLine(to: CGPoint(x: 0, y: len))
        var h = Path()
        h.move(to: CGPoint(x: -len, y: 0))
        h.addLine(to: CGPoint(x: len, y: 0))
        spokeCtx.stroke(v, with: .color(WheelSpk), style: stroke)
        spokeCtx.stroke(h, with: .color(WheelSpk), style: stroke)
    }

    private func drawLogo(context: GraphicsContext, sx: CGFloat, sy: CGFloat, alpha: Double) {
        @inline(__always) func f(_ x: CGFloat) -> CGFloat { x * sx }
        @inline(__always) func g(_ y: CGFloat) -> CGFloat { y * sy }

        let lime  = Color(hex: 0xC5FF45).opacity(alpha)
        let dark  = Color(hex: 0x0B0B0B).opacity(alpha)
        let white = LogoWhite.opacity(alpha)

        // Icon: lime rounded-square pill, 44×44 SVG units at (88, 508)
        let iconSize = min(f(44), g(44))
        let iconX    = f(88)
        let iconY    = g(508)
        let s        = iconSize / 44

        // Lime pill background
        let cr = iconSize * 0.25
        context.fill(
            Path(roundedRect: CGRect(x: iconX, y: iconY, width: iconSize, height: iconSize), cornerRadius: cr),
            with: .color(lime)
        )

        // Chevron: M14 8 L30 22 L14 36
        var chevron = Path()
        chevron.move(to:    CGPoint(x: iconX + 14 * s, y: iconY + 8  * s))
        chevron.addLine(to: CGPoint(x: iconX + 30 * s, y: iconY + 22 * s))
        chevron.addLine(to: CGPoint(x: iconX + 14 * s, y: iconY + 36 * s))
        context.stroke(chevron, with: .color(dark),
                       style: StrokeStyle(lineWidth: 7 * s, lineCap: .round, lineJoin: .round))

        // "ridly" wordmark — Syne Bold, vertically centred to icon
        let fontSize = iconSize * 0.9
        let wordmark = Text("ridly")
            .font(.custom("Nunito-Black", size: fontSize))
            .foregroundStyle(white)
        let textX = iconX + iconSize + f(10)
        let textY = iconY + iconSize / 2
        context.draw(wordmark, at: CGPoint(x: textX, y: textY), anchor: .leading)
    }
}

// MARK: — Math helper ─────────────────────────────────────────────────────────

private func lerp(_ a: Double, _ b: Double, _ t: Double) -> Double {
    a + (b - a) * t
}

// MARK: — Preview ─────────────────────────────────────────────────────────────

#Preview { SplashView() }


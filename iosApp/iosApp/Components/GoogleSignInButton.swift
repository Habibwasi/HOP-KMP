import SwiftUI

// MARK: - Native Google Sign-In Button ────────────────────────────────────────

/// Google-branded sign-in button per Google's brand guidelines:
/// white background, multicolor G logo, dark label, subtle grey border.
struct GoogleSignInButton: View {
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                GoogleGLogo()
                    .frame(width: 20, height: 20)
                Text("Continue with Google")
                    .font(HopFont.labelMedium(weight: .medium))
                    .foregroundColor(isEnabled ? Color(hex: 0x1F1F1F) : Color(hex: 0x1F1F1F).opacity(0.38))
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: 0xDADADA).opacity(isEnabled ? 1 : 0.5), lineWidth: 1)
            )
            .opacity(isEnabled ? 1 : 0.6)
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
    }
}

// MARK: - Google G Logo ───────────────────────────────────────────────────────

/// Draws the standard 4-colour Google "G" logo using SwiftUI paths.
struct GoogleGLogo: View {
    var body: some View {
        GeometryReader { geo in
            let s = geo.size.width / 24
            ZStack {
                // Blue (right bar)
                Path { p in
                    p.move(to: .init(x: 22.56*s, y: 12.25*s))
                    p.addLine(to: .init(x: 22.56*s, y: 11.47*s))
                    p.addCurve(to: .init(x: 22.36*s, y: 10*s),
                               control1: .init(x: 22.56*s, y: 10.72*s),
                               control2: .init(x: 22.49*s, y: 10*s))
                    p.addLine(to: .init(x: 12*s, y: 10*s))
                    p.addLine(to: .init(x: 12*s, y: 14.26*s))
                    p.addLine(to: .init(x: 17.92*s, y: 14.26*s))
                    p.addCurve(to: .init(x: 15.71*s, y: 17.57*s),
                               control1: .init(x: 17.66*s, y: 15.63*s),
                               control2: .init(x: 16.88*s, y: 16.79*s))
                    p.addLine(to: .init(x: 19.28*s, y: 20.34*s))
                    p.addCurve(to: .init(x: 22.56*s, y: 12.25*s),
                               control1: .init(x: 21.36*s, y: 18.42*s),
                               control2: .init(x: 22.56*s, y: 15.6*s))
                }.fill(Color(hex: 0x4285F4))

                // Green (bottom right)
                Path { p in
                    p.move(to: .init(x: 12*s, y: 23*s))
                    p.addCurve(to: .init(x: 19.28*s, y: 20.34*s),
                               control1: .init(x: 14.97*s, y: 23*s),
                               control2: .init(x: 17.46*s, y: 22.02*s))
                    p.addLine(to: .init(x: 15.71*s, y: 17.57*s))
                    p.addCurve(to: .init(x: 12*s, y: 18.63*s),
                               control1: .init(x: 14.73*s, y: 18.23*s),
                               control2: .init(x: 13.48*s, y: 18.63*s))
                    p.addCurve(to: .init(x: 5.82*s, y: 14.1*s),
                               control1: .init(x: 9.13*s, y: 18.63*s),
                               control2: .init(x: 6.69*s, y: 16.7*s))
                    p.addLine(to: .init(x: 2.15*s, y: 16.95*s))
                    p.addCurve(to: .init(x: 12*s, y: 23*s),
                               control1: .init(x: 3.96*s, y: 20.54*s),
                               control2: .init(x: 7.69*s, y: 23*s))
                }.fill(Color(hex: 0x34A853))

                // Yellow (bottom left)
                Path { p in
                    p.move(to: .init(x: 5.82*s, y: 14.1*s))
                    p.addCurve(to: .init(x: 5.48*s, y: 12*s),
                               control1: .init(x: 5.6*s, y: 13.44*s),
                               control2: .init(x: 5.48*s, y: 12.73*s))
                    p.addCurve(to: .init(x: 5.82*s, y: 9.9*s),
                               control1: .init(x: 5.48*s, y: 11.27*s),
                               control2: .init(x: 5.6*s, y: 10.56*s))
                    p.addLine(to: .init(x: 2.15*s, y: 7.05*s))
                    p.addCurve(to: .init(x: 2.15*s, y: 16.95*s),
                               control1: .init(x: 1.42*s, y: 8.5*s),
                               control2: .init(x: 1*s, y: 13.8*s))
                    p.addLine(to: .init(x: 5.82*s, y: 14.1*s))
                }.fill(Color(hex: 0xFBBC05))

                // Red (top left)
                Path { p in
                    p.move(to: .init(x: 12*s, y: 5.37*s))
                    p.addCurve(to: .init(x: 16.19*s, y: 7.02*s),
                               control1: .init(x: 13.63*s, y: 5.37*s),
                               control2: .init(x: 15.06*s, y: 5.93*s))
                    p.addLine(to: .init(x: 19.37*s, y: 3.84*s))
                    p.addCurve(to: .init(x: 12*s, y: 1*s),
                               control1: .init(x: 17.45*s, y: 2.06*s),
                               control2: .init(x: 14.97*s, y: 1*s))
                    p.addCurve(to: .init(x: 5.82*s, y: 9.9*s),
                               control1: .init(x: 7.69*s, y: 1*s),
                               control2: .init(x: 3.96*s, y: 3.46*s))
                    p.addLine(to: .init(x: 5.82*s, y: 9.9*s))
                    p.addCurve(to: .init(x: 12*s, y: 5.37*s),
                               control1: .init(x: 6.69*s, y: 7.3*s),
                               control2: .init(x: 9.13*s, y: 5.37*s))
                }.fill(Color(hex: 0xEA4335))
            }
        }
    }
}

#Preview("Google Sign-In Button") {
    VStack(spacing: 16) {
        GoogleSignInButton(action: {})
        GoogleSignInButton(isEnabled: false, action: {})
    }
    .padding()
    .background(Color.hopBackground)
}

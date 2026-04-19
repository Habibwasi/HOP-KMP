import SwiftUI

// MARK: - Size

enum HopAvatarSize {
    case small   // 32 pt — inline in lists
    case medium  // 44 pt — trip cards, chat
    case large   // 60 pt — profile header
    case xlarge  // 80 pt — profile top of screen

    var diameter: CGFloat {
        switch self {
        case .small:  return 32
        case .medium: return 44
        case .large:  return 60
        case .xlarge: return 80
        }
    }

    var fontSize: CGFloat {
        switch self {
        case .small:  return 12
        case .medium: return 16
        case .large:  return 22
        case .xlarge: return 30
        }
    }

    /// Diameter of the verified tick badge
    var tickDiameter: CGFloat {
        switch self {
        case .small:  return 12
        case .medium: return 16
        case .large:  return 20
        case .xlarge: return 24
        }
    }
}

// MARK: - HopAvatar

/// Circle avatar with:
/// - Remote image via AsyncImage (shimmer during load)
/// - Fallback to initials on error or when no URL supplied
/// - Optional verified tick overlay (bottom-trailing)
struct HopAvatar: View {
    let name: String
    var imageURL: URL? = nil
    var size: HopAvatarSize = .medium
    var isVerified: Bool = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            avatarCircle

            if isVerified {
                Image(systemName: "checkmark.seal.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: size.tickDiameter, height: size.tickDiameter)
                    .foregroundColor(Color.hopPrimaryLime)
                    .background(
                        Circle()
                            .fill(Color.hopSurface)
                            .frame(
                                width: size.tickDiameter + 3,
                                height: size.tickDiameter + 3
                            )
                    )
                    .offset(x: 2, y: 2)
            }
        }
        .accessibilityLabel("\(name)\(isVerified ? ", verified" : "")")
    }

    // MARK: - Sub-views

    @ViewBuilder
    private var avatarCircle: some View {
        Group {
            if let url = imageURL {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                    case .failure:
                        initialsView
                    case .empty:
                        // Shimmer placeholder
                        Color.hopSurfaceElevated
                            .overlay(
                                ProgressView()
                                    .progressViewStyle(
                                        CircularProgressViewStyle(tint: Color.hopTextSecondary)
                                    )
                                    .scaleEffect(0.6)
                            )
                    @unknown default:
                        initialsView
                    }
                }
            } else {
                initialsView
            }
        }
        .frame(width: size.diameter, height: size.diameter)
        .clipShape(Circle())
    }

    private var initialsView: some View {
        ZStack {
            Color.hopSurfaceElevated
            Text(initials)
                .font(.system(size: size.fontSize, weight: .semibold, design: .rounded))
                .foregroundColor(Color.hopTextPrimary)
        }
    }

    // MARK: - Helpers

    /// Up to two initials: first letter of first word + first letter of last word.
    private var initials: String {
        let words = name
            .trimmingCharacters(in: .whitespaces)
            .split(separator: " ")
            .map { String($0) }
        let first = words.first?.first.map(String.init) ?? "?"
        let last  = words.count > 1 ? words.last?.first.map(String.init) ?? "" : ""
        return (first + last).uppercased()
    }
}

// MARK: - Previews

#Preview("HopAvatar — Sizes & States") {
    VStack(spacing: HopSpacing.xl) {
        // All sizes without verified
        HStack(spacing: HopSpacing.lg) {
            HopAvatar(name: "Jane Doe",       size: .small)
            HopAvatar(name: "Anders Nielsen", size: .medium)
            HopAvatar(name: "Maria Larsen",   size: .large)
            HopAvatar(name: "K",              size: .xlarge)
        }

        // With verified badge
        HStack(spacing: HopSpacing.lg) {
            HopAvatar(name: "Jane Doe",       size: .small,  isVerified: true)
            HopAvatar(name: "Anders Nielsen", size: .medium, isVerified: true)
            HopAvatar(name: "Maria Larsen",   size: .large,  isVerified: true)
            HopAvatar(name: "K",              size: .xlarge, isVerified: true)
        }

        // Single-word name fallback
        HStack(spacing: HopSpacing.md) {
            HopAvatar(name: "Anders", size: .medium)
            HopAvatar(name: "A",      size: .medium)
        }
    }
    .padding(HopSpacing.xl)
    .background(Color.hopSurface)
}

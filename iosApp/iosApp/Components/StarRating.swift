import SwiftUI

// MARK: - StarRatingDisplay (read-only)

/// Read-only star row. Supports half-stars. Optionally shows review count.
struct StarRatingDisplay: View {
    /// Rating value 0.0–5.0. Values are clamped.
    let rating: Double
    /// Optional review count shown in parentheses.
    var count: Int? = nil
    /// Star size in points.
    var starSize: CGFloat = 14
    /// Pass `true` when the card/surface is light (white/gray) so stars use
    /// hopWarning (amber) instead of lime — avoids near-zero contrast on white.
    var lightSurface: Bool = false

    var body: some View {
        HStack(spacing: 2) {
            ForEach(0..<5, id: \.self) { index in
                starImage(for: index)
                    .resizable()
                    .scaledToFit()
                    .frame(width: starSize, height: starSize)
                    .foregroundColor(lightSurface ? Color.hopWarning : Color.hopPrimaryLime)
            }
            if let count {
                Text("(\(count))")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
                    .padding(.leading, 2)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityText)
    }

    private func starImage(for index: Int) -> Image {
        let clamped = min(max(rating, 0), 5)
        let threshold = Double(index + 1)
        if clamped >= threshold {
            return Image(systemName: "star.fill")
        } else if clamped >= Double(index) + 0.5 {
            return Image(systemName: "star.leadinghalf.filled")
        } else {
            return Image(systemName: "star")
        }
    }

    private var accessibilityText: String {
        let formatted = String(format: "%.1f", min(max(rating, 0), 5))
        if let count {
            return "\(formatted) out of 5 stars, \(count) reviews"
        }
        return "\(formatted) out of 5 stars"
    }
}

// MARK: - StarRatingInput (interactive)

/// Interactive 5-star rating input.
/// Each star has a 44 pt tap target per HIG guidelines.
struct StarRatingInput: View {
    /// Selected star count 0–5. 0 means no rating set yet.
    @Binding var rating: Int
    /// Pass `true` when rendered on a light/white background so filled stars
    /// use hopWarning (amber) instead of lime — avoids near-zero contrast.
    var lightSurface: Bool = false

    var body: some View {
        HStack(spacing: 0) {
            ForEach(1...5, id: \.self) { star in
                Image(systemName: star <= rating ? "star.fill" : "star")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 28, height: 28)
                    .foregroundColor(star <= rating
                        ? (lightSurface ? Color.hopWarning : Color.hopPrimaryLime)
                        : Color.hopTextSecondary)
                    // 44 pt touch target
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        withAnimation(.spring(response: 0.25, dampingFraction: 0.6)) {
                            // Tapping the current rating again clears it
                            rating = (rating == star) ? 0 : star
                        }
                    }
                    .accessibilityLabel("\(star) star\(star == 1 ? "" : "s")")
                    .accessibilityAddTraits(star == rating ? .isSelected : [])
                    .accessibilityHint(star == rating ? "Double-tap to clear" : "Double-tap to rate \(star) star\(star == 1 ? "" : "s")")
            }
        }
    }
}

// MARK: - Previews

#Preview("StarRatingDisplay") {
    VStack(alignment: .leading, spacing: HopSpacing.md) {
        StarRatingDisplay(rating: 5.0, count: 128)
        StarRatingDisplay(rating: 4.5, count: 38)
        StarRatingDisplay(rating: 3.7, count: 7)
        StarRatingDisplay(rating: 1.0)
        StarRatingDisplay(rating: 0.0)
        // Larger stars
        StarRatingDisplay(rating: 4.8, count: 92, starSize: 20)
    }
    .padding(HopSpacing.md)
    .background(Color.hopSurface)
}

#Preview("StarRatingInput — Interactive") {
    @Previewable @State var rating: Int = 3

    VStack(spacing: HopSpacing.lg) {
        StarRatingInput(rating: $rating)
        Text(rating == 0 ? "Tap to rate" : "\(rating) star\(rating == 1 ? "" : "s")")
            .font(HopFont.bodyMedium())
            .foregroundColor(Color.hopTextSecondary)
            .animation(.easeInOut(duration: 0.15), value: rating)
    }
    .padding(HopSpacing.xl)
    .background(Color.hopSurface)
}

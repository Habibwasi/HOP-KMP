import SwiftUI
import Shared

// MARK: — Legacy profile components ───────────────────────────────────────────
//
// Used by `OtherProfileView` (SH-03).  The Own Profile (SH-02) uses the
// Android-faithful inline rows defined in `OwnProfileView.swift`.

struct SectionHeader: View {
    let title: String
    var body: some View {
        HStack {
            Text(title)
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopTextSecondary)
            Spacer()
        }
        .padding(.top, HopSpacing.md)
    }
}

struct InfoRow: View {
    let title: String
    let value: String
    let icon: String
    var action: (() -> Void)? = nil

    var body: some View {
        Button(action: { action?() }) {
            HStack(spacing: HopSpacing.sm) {
                Image(systemName: icon)
                    .foregroundColor(Color.hopTextSecondary)
                    .frame(width: 24)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                    Text(value)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                }
                Spacer()
                if action != nil {
                    Image(systemName: "chevron.right")
                        .foregroundColor(Color.hopTextSecondary)
                        .font(.system(size: 12))
                }
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(action == nil)
    }
}

struct RatingBadge: View {
    let label: String
    let rating: Double?

    var body: some View {
        VStack(spacing: 2) {
            HStack(spacing: 4) {
                Image(systemName: "star.fill")
                    .foregroundColor(Color.hopPrimaryLime)
                    .font(.system(size: 14))
                Text(rating.map { String(format: "%.1f", $0) } ?? "—")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
            }
            Text(label)
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopSurfaceElevated)
        .clipShape(Capsule())
    }
}

struct ReviewRow: View {
    let review: UserReview

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            HStack {
                Text(review.raterName)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                HStack(spacing: 2) {
                    ForEach(0..<5, id: \.self) { i in
                        Image(systemName: i < Int(review.stars) ? "star.fill" : "star")
                            .foregroundColor(Color.hopPrimaryLime)
                            .font(.system(size: 12))
                    }
                }
            }
            if let comment = review.comment, !comment.isEmpty {
                Text(comment)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

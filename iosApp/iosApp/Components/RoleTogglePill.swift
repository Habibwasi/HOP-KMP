import SwiftUI

// MARK: - HopRole

enum HopRole: CaseIterable {
    case passenger
    case driver

    var label: String {
        switch self {
        case .passenger: return "Passenger"
        case .driver:    return "Driver"
        }
    }
}

// MARK: - RoleTogglePill

/// Segmented Passenger/Driver selector — 160×36 pt animated pill.
///
/// The sliding indicator animates with a spring; label colours cross-fade.
struct RoleTogglePill: View {
    @Binding var selectedRole: HopRole

    // Fixed dimensions per spec
    private let totalWidth:   CGFloat = 160
    private let totalHeight:  CGFloat = 36
    private var segmentWidth: CGFloat { totalWidth / 2 }
    private let padding:      CGFloat = 3

    var body: some View {
        ZStack(alignment: .leading) {
            // ── Track ─────────────────────────────────────────────────────────
            Capsule()
                .fill(Color.hopSurfaceElevated)
                .frame(width: totalWidth, height: totalHeight)

            // ── Sliding indicator ─────────────────────────────────────────────
            Capsule()
                .fill(Color.hopPrimaryLime)
                .frame(width: segmentWidth - padding * 2, height: totalHeight - padding * 2)
                .padding(.leading, indicatorOffset)
                .animation(.spring(response: 0.3, dampingFraction: 0.72), value: selectedRole)

            // ── Labels ────────────────────────────────────────────────────────
            HStack(spacing: 0) {
                ForEach(HopRole.allCases, id: \.label) { role in
                    Text(role.label)
                        .font(HopFont.labelSmall())
                        .fontWeight(.semibold)
                        .foregroundColor(selectedRole == role ? Color.hopSurface : Color.hopTextSecondary)
                        .frame(width: segmentWidth, height: totalHeight)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            guard selectedRole != role else { return }
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.72)) {
                                selectedRole = role
                            }
                        }
                        .animation(.easeInOut(duration: 0.2), value: selectedRole)
                        .accessibilityAddTraits(selectedRole == role ? [.isSelected, .isButton] : .isButton)
                        .accessibilityLabel(role.label)
                }
            }
        }
        .frame(width: totalWidth, height: totalHeight)
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Role selector")
    }

    private var indicatorOffset: CGFloat {
        switch selectedRole {
        case .passenger: return padding
        case .driver:    return segmentWidth + padding
        }
    }
}

// MARK: - Previews

#Preview("RoleTogglePill") {
    @Previewable @State var role: HopRole = .passenger

    VStack(spacing: HopSpacing.lg) {
        RoleTogglePill(selectedRole: $role)

        Text("Selected: \(role.label)")
            .font(HopFont.bodyMedium())
            .foregroundColor(Color.hopTextSecondary)
    }
    .padding(HopSpacing.xl)
    .background(Color.hopSurface)
}

#Preview("RoleTogglePill — Driver selected") {
    @Previewable @State var role: HopRole = .driver

    RoleTogglePill(selectedRole: $role)
        .padding(HopSpacing.xl)
        .background(Color.hopSurface)
}

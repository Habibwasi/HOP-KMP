//
//  DriverChrome.swift
//  iosApp
//
//  Shared chrome used across all driver screens. Mirrors the auth/passenger
//  custom top-bar pattern (the system NavigationStack toolbar is hidden
//  globally in HopNavigationStack) — every detail screen owns its own
//  back-arrow + title row.
//

import SwiftUI

/// Standard light-theme top bar with optional leading back arrow.
struct DriverTopBar: View {
    let title: String
    var onBack: (() -> Void)? = nil
    /// Optional trailing action (e.g. month picker chevron).
    var trailing: AnyView? = nil

    var body: some View {
        ZStack {
            Text(title)
                .font(HopFont.titleMedium(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            HStack {
                if let onBack {
                    Button(action: onBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .frame(width: 44, height: 44)
                    }
                    .buttonStyle(.plain)
                } else {
                    Spacer().frame(width: 44, height: 44)
                }
                Spacer()
                if let trailing {
                    trailing
                } else {
                    Spacer().frame(width: 44, height: 44)
                }
            }
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.top, HopSpacing.xs)
    }
}

/// Light card surface used for trip rows, passenger rows, summary boxes, etc.
extension View {
    func driverCard(corner: CGFloat = 12) -> some View {
        self
            .background(Color.hopCardSurface)
            .clipShape(RoundedRectangle(cornerRadius: corner))
            .overlay(
                RoundedRectangle(cornerRadius: corner)
                    .stroke(Color.hopCardBorder, lineWidth: 1)
            )
    }
}

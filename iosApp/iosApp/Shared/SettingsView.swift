import SwiftUI
import Shared

// MARK: — SH-06 Settings ──────────────────────────────────────────────────────
//
// Mirrors `SettingsScreen.kt` 1:1.  Light theme with grouped sections
// (Account / Notifications / Support / Legal), grouped-card style rows,
// destructive Log Out button, log-out confirmation dialog, and footer
// version text.

struct SettingsView: View {

    var onBack: () -> Void
    var onLoggedOut: () -> Void

    @StateObject private var wrapper = SettingsViewModelWrapper()
    @StateObject private var auth    = AuthViewModelWrapper()

    private let appVersion = "1.0"

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                SettingsTopBar(onBack: onBack)

                ScrollView(showsIndicators: false) {
                    VStack(spacing: HopSpacing.md) {
                        Spacer().frame(height: HopSpacing.sm)

                        SettingsSection(title: "Account") {
                            SettingsLinkRow(icon: "person",      label: "Edit profile",      action: wrapper.editProfile)
                            SettingsDivider()
                            SettingsLinkRow(icon: "lock",        label: "Change password",   action: wrapper.changePassword)
                        }

                        SettingsSection(title: "Notifications") {
                            SettingsToggleRow(
                                icon: "bell",
                                label: "Push notifications",
                                checked: wrapper.state.pushNotificationsEnabled,
                                onToggle: wrapper.togglePushNotifications
                            )
                        }

                        SettingsSection(title: "Support") {
                            SettingsLinkRow(icon: "questionmark.circle", label: "Help centre", action: wrapper.helpCentre)
                            SettingsDivider()
                            SettingsLinkRow(icon: "envelope",            label: "Contact us",  action: wrapper.contactUs)
                        }

                        SettingsSection(title: "Legal") {
                            SettingsLinkRow(icon: "doc.text",    label: "Terms of Service", action: wrapper.termsOfService)
                            SettingsDivider()
                            SettingsLinkRow(icon: "lock.shield", label: "Privacy Policy",   action: wrapper.privacyPolicy)
                        }

                        Spacer().frame(height: HopSpacing.xs)

                        // ── Log out ──────────────────────────────────────────
                        Button(action: wrapper.logoutTapped) {
                            HStack(spacing: HopSpacing.sm) {
                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                    .font(.system(size: 18, weight: .semibold))
                                Text("Log Out")
                                    .font(.system(size: 16, weight: .semibold))
                            }
                            .foregroundColor(Color.hopError)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(Color.hopError.opacity(0.12))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .accessibilityLabel("Log out of your account")

                        // ── App version ─────────────────────────────────────
                        Text("Version \(appVersion)")
                            .font(.system(size: 12))
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, HopSpacing.xl)
                    }
                    .padding(.horizontal, HopSpacing.md)
                }
            }
        }
        .navigationBarHidden(true)
        .alert("Log out?", isPresented: Binding(
            get: { wrapper.state.showLogoutDialog },
            set: { if !$0 { wrapper.logoutDismissed() } }
        )) {
            Button("Cancel", role: .cancel) { wrapper.logoutDismissed() }
            Button("Log Out", role: .destructive) { wrapper.logoutConfirmed() }
        } message: {
            Text("You'll need to sign in again to use Hop.")
        }
        .task {
            wrapper.startObserving { effect in
                if effect is SettingsEffectLogout {
                    auth.logout()
                    onLoggedOut()
                }
            }
        }
    }
}

// MARK: — Top bar

private struct SettingsTopBar: View {
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 40, height: 40)
            }
            .accessibilityLabel("Navigate back")

            Text("Settings")
                .font(HopFont.bodyLarge(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            Spacer()
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopBackground)
    }
}

// MARK: — Section card

private struct SettingsSection<Content: View>: View {
    let title: String
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            Text(title.uppercased())
                .font(.system(size: 11, weight: .medium))
                .tracking(0.8)
                .foregroundColor(Color.hopAuthTextSecondary)
                .padding(.leading, HopSpacing.xs)

            VStack(spacing: 0) { content }
                .background(Color.hopAuthInputSurface)
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

// MARK: — Rows

private struct SettingsLinkRow: View {
    let icon: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: HopSpacing.md) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .frame(width: 20, height: 20)
                Text(label)
                    .font(.system(size: 15))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundColor(Color.hopAuthTextSecondary)
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

private struct SettingsToggleRow: View {
    let icon: String
    let label: String
    let checked: Bool
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: HopSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .regular))
                .foregroundColor(Color.hopAuthTextSecondary)
                .frame(width: 20, height: 20)
            Text(label)
                .font(.system(size: 15))
                .foregroundColor(Color.hopAuthTextPrimary)
            Spacer()
            Toggle("", isOn: Binding(get: { checked }, set: { _ in onToggle() }))
                .labelsHidden()
                .tint(Color.hopPrimaryLime)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.sm)
        .contentShape(Rectangle())
        .onTapGesture { onToggle() }
        .accessibilityLabel("\(label) toggle, \(checked ? "on" : "off")")
    }
}

private struct SettingsDivider: View {
    var body: some View {
        Rectangle()
            .fill(Color.hopAuthInputBorder)
            .frame(height: 0.5)
            .padding(.leading, 52)
    }
}

#Preview {
    SettingsView(onBack: {}, onLoggedOut: {})
}

import SwiftUI
import Shared

// MARK: — SH-06 Settings ──────────────────────────────────────────────────────
// Mirrors `SettingsScreen.kt`.

struct SettingsView: View {

    var onBack: () -> Void
    var onLoggedOut: () -> Void

    @StateObject private var wrapper = SettingsViewModelWrapper()
    @StateObject private var auth    = AuthViewModelWrapper()

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: HopSpacing.md) {
                    SectionHeader(title: "Account")
                    SettingsRow(icon: "person", title: "Edit profile",        action: wrapper.editProfile)
                    SettingsRow(icon: "lock",   title: "Change password",     action: wrapper.changePassword)

                    SectionHeader(title: "Preferences")
                    HStack {
                        Image(systemName: "bell").foregroundColor(Color.hopTextSecondary).frame(width: 24)
                        Text("Push notifications")
                            .font(HopFont.labelMedium(weight: .semibold))
                            .foregroundColor(Color.hopTextPrimary)
                        Spacer()
                        Toggle("", isOn: Binding(
                            get: { wrapper.state.pushNotificationsEnabled },
                            set: { _ in wrapper.togglePushNotifications() }
                        ))
                        .labelsHidden()
                        .tint(Color.hopPrimaryLime)
                    }
                    .padding(HopSpacing.md)
                    .background(Color.hopSurfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    SectionHeader(title: "Support")
                    SettingsRow(icon: "questionmark.circle", title: "Help centre",      action: wrapper.helpCentre)
                    SettingsRow(icon: "envelope",            title: "Contact us",        action: wrapper.contactUs)

                    SectionHeader(title: "Legal")
                    SettingsRow(icon: "doc.text", title: "Terms of service", action: wrapper.termsOfService)
                    SettingsRow(icon: "lock.shield", title: "Privacy policy", action: wrapper.privacyPolicy)

                    HopButton(text: "Log out", variant: .destructive, action: wrapper.logoutTapped)
                        .padding(.top, HopSpacing.lg)

                    Spacer().frame(height: HopSpacing.xxl)
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.md)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Settings").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .alert("Log out?", isPresented: Binding(
            get: { wrapper.state.showLogoutDialog },
            set: { if !$0 { wrapper.logoutDismissed() } }
        )) {
            Button("Cancel", role: .cancel) { wrapper.logoutDismissed() }
            Button("Log out", role: .destructive) {
                wrapper.logoutConfirmed()
            }
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

private struct SettingsRow: View {
    let icon: String
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: HopSpacing.sm) {
                Image(systemName: icon).foregroundColor(Color.hopTextSecondary).frame(width: 24)
                Text(title)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundColor(Color.hopTextSecondary).font(.system(size: 12))
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}

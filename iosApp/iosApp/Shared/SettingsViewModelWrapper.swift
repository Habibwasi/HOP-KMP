import Combine
import Shared

@MainActor
final class SettingsViewModelWrapper: ObservableObject {

    let viewModel: SettingsViewModel

    @Published var state: SettingsUiState

    init() {
        let vm = KoinIOSKt.getSettingsViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any SettingsEffect) -> Void) {
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
        Task {
            for await effect in viewModel.effect {
                onEffect(effect)
            }
        }
    }

    func togglePushNotifications() { viewModel.onEvent(event: SettingsEventTogglePushNotifications.shared) }
    func logoutTapped()      { viewModel.onEvent(event: SettingsEventLogoutTapped.shared) }
    func logoutConfirmed()   { viewModel.onEvent(event: SettingsEventLogoutConfirmed.shared) }
    func logoutDismissed()   { viewModel.onEvent(event: SettingsEventLogoutDismissed.shared) }
    func editProfile()       { viewModel.onEvent(event: SettingsEventEditProfileTapped.shared) }
    func changePassword()    { viewModel.onEvent(event: SettingsEventChangePasswordTapped.shared) }
    func helpCentre()        { viewModel.onEvent(event: SettingsEventHelpCentreTapped.shared) }
    func contactUs()         { viewModel.onEvent(event: SettingsEventContactUsTapped.shared) }
    func termsOfService()    { viewModel.onEvent(event: SettingsEventTermsOfServiceTapped.shared) }
    func privacyPolicy()     { viewModel.onEvent(event: SettingsEventPrivacyPolicyTapped.shared) }
}

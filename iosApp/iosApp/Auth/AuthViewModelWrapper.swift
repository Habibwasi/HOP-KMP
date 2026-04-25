import Combine
import Shared

// ── AuthViewModelWrapper ──────────────────────────────────────────────────────
//
// Canonical @StateObject bridge from shared AuthViewModel (KMP/SKIE) to SwiftUI.
// Use @StateObject — NEVER @ObservedObject (would cancel the flow on re-render).
//
// SKIE automatically converts:
//  • StateFlow<AuthUiState>  → AsyncSequence  (for await in viewModel.state)
//  • Channel<AuthEffect>     → AsyncSequence  (for await in viewModel.effect)
//
// AuthEvent is a sealed interface → SKIE exposes each case as its own class:
//   AuthEventRegister, AuthEventLogin, AuthEventClearError, AuthEventLogout …
//

@MainActor
final class AuthViewModelWrapper: ObservableObject {

    let viewModel: AuthViewModel

    @Published var state: AuthUiState

    init() {
        // Resolved via the typed helper in KoinIOS.kt — KoinIOSKt.getAuthViewModel().
        // This avoids the need for a generic get<T>() call, which is not expressible
        // from Swift without a concrete overload.
        let vm = KoinIOSKt.getAuthViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving() {
        // Collect StateFlow<AuthUiState>
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
    }

    // ── Convenience event dispatchers ─────────────────────────────────────────
    // AuthEvent is a sealed interface → SKIE exposes each case as its own class.
    // AuthEvent.Register  → AuthEventRegister(email:password:firstName:lastName:phone:)
    // AuthEvent.Login     → AuthEventLogin(email:password:)

    func register(firstName: String, lastName: String, email: String, phone: String? = nil, password: String) {
        viewModel.onEvent(event: AuthEventRegister(
            email: email,
            password: password,
            firstName: firstName,
            lastName: lastName,
            phone: phone
        ))
    }

    func login(email: String, password: String) {
        viewModel.onEvent(event: AuthEventLogin(email: email, password: password))
    }

    func requestPasswordReset(email: String) {
        viewModel.onEvent(event: AuthEventRequestPasswordReset(email: email))
    }

    func clearError() {
        // data object ClearError → Kotlin singleton → Swift .shared
        viewModel.onEvent(event: AuthEventClearError.shared)
    }

    func logout() {
        // data object Logout → Kotlin singleton → Swift .shared
        viewModel.onEvent(event: AuthEventLogout.shared)
    }
}


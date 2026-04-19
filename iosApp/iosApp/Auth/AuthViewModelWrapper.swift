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

@MainActor
final class AuthViewModelWrapper: ObservableObject {

    let viewModel: AuthViewModel

    @Published var state: AuthUiState

    init() {
        // Resolve AuthViewModel from the Koin container.
        // KoinKt.get() is the SKIE-generated Swift entry point for koin-core's `get<T>()`.
        // Do NOT use objCClass: variant — it is not part of the public koin-core iOS API.
        let vm = KoinKt.get() as AuthViewModel
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

    // ── Convenience event dispatcher ──────────────────────────────────────────

    func register(fullName: String, email: String, phone: String, password: String) {
        viewModel.onEvent(event: AuthEvent.Register(
            fullName: fullName,
            email: email,
            phone: phone,
            password: password
        ))
    }

    func login(email: String, password: String) {
        viewModel.onEvent(event: AuthEvent.Login(email: email, password: password))
    }

    func sendOtp(phone: String) {
        viewModel.onEvent(event: AuthEvent.SendOtp(phone: phone))
    }

    func verifyOtp(phone: String, code: String) {
        viewModel.onEvent(event: AuthEvent.VerifyOtp(phone: phone, code: code))
    }

    func clearError() {
        viewModel.onEvent(event: AuthEvent.ClearError())
    }
}

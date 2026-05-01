import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        let supabaseUrl = Bundle.main.infoDictionary?["SUPABASE_URL"] as? String ?? ""
        let supabaseAnonKey = Bundle.main.infoDictionary?["SUPABASE_ANON_KEY"] as? String ?? ""
        KoinIOSKt.doInitKoin(supabaseUrl: supabaseUrl, supabaseAnonKey: supabaseAnonKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    guard url.scheme == "hop", url.host == "auth" else { return }
                    let vm = KoinIOSKt.getAuthViewModel()
                    // HandleDeepLink internally detects `type=recovery` and emits
                    // AuthEffect.NavigateToSetNewPassword so the nav stack can push
                    // SetNewPasswordView for both email-confirmation and password-reset links.
                    vm.onEvent(event: AuthEventHandleDeepLink(url: url.absoluteString))
                }
        }
    }
}
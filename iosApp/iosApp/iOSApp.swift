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
                    // Handle hop://auth/callback email-confirmation deep links.
                    guard url.scheme == "hop", url.host == "auth" else { return }
                    let vm = KoinIOSKt.getAuthViewModel()
                    vm.onEvent(event: AuthEventHandleDeepLink(url: url.absoluteString))
                }
        }
    }
}